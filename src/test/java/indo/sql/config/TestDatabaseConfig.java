/*
 * Copyright 2015 Indo Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package indo.sql.config;

import indo.jdbc.DataSources;
import indo.util.Unchecked;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.SQLExec;
import org.dbunit.DefaultDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.IOperationListener;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.util.Properties;

import static indo.log.Logger.debug;
import static indo.log.Logger.info;
import static org.dbunit.Assertion.assertEquals;

/**
 * This base class can be used to implement specific database configurations for
 * use with DBUnit tests.
 *
 * @author Jonathan Cone
 */
public abstract class TestDatabaseConfig {

    private DataSource dataSource;
    private IDatabaseTester databaseTester;

    private String driver;
    private String password;
    private String schemaFileName;
    private String url;
    private String user;

    private Boolean caseSensitive;

    public TestDatabaseConfig(String configFile) throws Exception {

        Properties properties = new Properties();
        properties.load(new FileInputStream(configFile));

        this.user = properties.getProperty("user");
        this.password = properties.getProperty("password");
        this.url = properties.getProperty("url");
        this.schemaFileName = properties.getProperty("schema.file.name");
        this.caseSensitive = "true".equalsIgnoreCase(properties.getProperty("case.sensitive"));
    }

    public TestDatabaseConfig(String user, String password, String url, String schemaFileName, String driver, Boolean caseSensitive) {
        this.user = user;
        this.password = password;
        this.url = url;
        this.schemaFileName = schemaFileName;
        this.driver = driver;
        this.caseSensitive = caseSensitive;
        this.dataSource = createDataSource();
        createSchema();
    }

    protected Boolean isCaseSensitive() {
        return caseSensitive;
    }

    protected abstract DefaultDataTypeFactory getDataTypeFactory();

    protected abstract DataSource doCreateDataSource() throws Exception;

    protected final DataSource createDataSource() {
        info(this, "Creating %s data source configuration...", getClass().getName());
        try {
            return doCreateDataSource();
        } catch (Exception e) {
            throw Unchecked.exception(e);
        }
    }

    protected void doCreateSchema() throws Exception {

        SQLExec sqlExec = new SQLExec();

        sqlExec.setTaskType("sql");
        sqlExec.setTaskName("sql");

        Project project = new Project();
        project.init();
        sqlExec.setProject(project);

        sqlExec.setDriver(getDriver());
        sqlExec.setPassword(getPassword());
        sqlExec.setSrc(new File(getFullSchemaSetupSqlPath()));
        sqlExec.setUrl(getUrl());
        sqlExec.setUserid(getUser());

        sqlExec.execute();
    }

    protected final void createSchema() {
        debug(this, "Creating %s schema...", getClass().getName());
        try {
            doCreateSchema();
        } catch (Exception e) {
            throw Unchecked.exception(e);
        }
    }

    private ReplacementDataSet createDataSet(String dataSetName) throws Exception {
        ReplacementDataSet dataSet = new ReplacementDataSet(
                new FlatXmlDataSetBuilder()
                        .setCaseSensitiveTableNames(false)
                        .build(new URL(dataSetName)));
        dataSet.addReplacementObject("[null]", null);
        return dataSet;
    }

    public void populateSchema(String classDataSetName, String methodDataSetName) {
        // Attempt to populate the schema with a data set specific to this method.
        boolean populated = populateSchema(methodDataSetName);

        // Only populate the schema with the class level data set if a method-specific
        // override didn't exist.
        if (!populated) {
            populateSchema(classDataSetName);
        }
    }

    protected boolean populateSchema(String dataSetName) {
        boolean populated = false;
        try {
            ReplacementDataSet dataSet = createDataSet(dataSetName);

            IDatabaseConnection connection = new DatabaseConnection(getConnection());

            databaseTester = new DefaultDatabaseTester(connection);

            DatabaseConfig config = connection.getConfig();

            config.setProperty(DatabaseConfig.FEATURE_CASE_SENSITIVE_TABLE_NAMES, isCaseSensitive());
            config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, getDataTypeFactory());

            databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
            databaseTester.setDataSet(dataSet);
            databaseTester.setOperationListener(IOperationListener.NO_OP_OPERATION_LISTENER);
            databaseTester.onSetup();

            populated = true;
        } catch (MalformedURLException m) {
            debug(this, "Skipping schema population since %s was not found.", dataSetName);
        } catch (Exception e) {
            throw Unchecked.exception(e);
        }
        return populated;
    }

    public void assertSchema(String classDataSetName, String methodDataSetName) {
        boolean populated = assertSchema(methodDataSetName);

        if (!populated) {
            assertSchema(classDataSetName);
        }
    }

    protected boolean assertSchema(String dataSetName) {
        boolean populated = false;
        try {
            IDataSet expectedDataSet = createDataSet(dataSetName);
            IDataSet actualDataSet = databaseTester.getConnection().createDataSet();

            for (String table : expectedDataSet.getTableNames()) {
                assertEquals(expectedDataSet.getTable(table), actualDataSet.getTable(table));
            }
            databaseTester.setTearDownOperation(DatabaseOperation.CLOSE_CONNECTION(DatabaseOperation.NONE));
            databaseTester.onTearDown();

            populated = true;
        } catch (MalformedURLException m) {
            debug(this, "Skipping schema assertion since %s was not found.", dataSetName);
        } catch (Exception e) {
            throw Unchecked.exception(e);
        }
        return populated;
    }

    public Object getValue(String tableName, String column, int row) {
        try {
            return databaseTester
                    .getConnection()
                    .createDataSet()
                    .getTable(tableName)
                    .getValue(row, column);
        } catch (Exception e) {
            throw Unchecked.exception(e);
        }
    }

    public String getDriver() {
        return driver;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        return url;
    }

    public String getSchemaFileName() {
        return schemaFileName;
    }

    public DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = createDataSource();
        }
        return dataSource;
    }

    public Connection getConnection() {
        return DataSources.getConnection(getDataSource());
    }

    protected String getFullSchemaSetupSqlPath() {
        return new File(getClass().getResource(getSchemaFileName()).getFile()).getAbsolutePath();
    }
}