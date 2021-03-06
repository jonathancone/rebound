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

package indo.sql.test;

import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.RunScript;

import javax.sql.DataSource;
import java.nio.charset.Charset;

/**
 * Configuration engine for H2 database-driven unit tests.
 *
 * @author Jonathan Cone
 */
public class H2Configuration extends DatabaseConfiguration {

    @Override
    protected DataSource doCreateDataSource() {
        return JdbcConnectionPool.create(getUrl(), getUser(), getPassword());
    }

    @Override
    protected void doCreateSchema() throws Exception {
        RunScript.execute(getUrl(), getUser(), getPassword(), getFullSchemaSetupSqlPath(), Charset.forName("UTF-8"), false);
    }

    @Override
    protected DefaultDataTypeFactory getDataTypeFactory() {
        return new H2DataTypeFactory();
    }

}