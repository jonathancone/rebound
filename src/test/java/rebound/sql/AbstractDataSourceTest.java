/*
 * Copyright (c) 2015 Rebound Contributors
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

package rebound.sql;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public abstract class AbstractDataSourceTest {

    @Parameterized.Parameter(0)
    public AbstractDataSourceConfigurer dataSourceConfig;

    @Parameterized.Parameters
    public static List<Object[]> dataSourceConfigurations() {
        Object[][] configs = new Object[][]{
                {new H2Configurer("sa", "sa", "jdbc:h2:mem:test", "default-schema.sql", "org.h2.Driver")}
        };

        return Arrays.asList(configs);
    }

    @Before
    public void setupDataSource() {
        dataSourceConfig.createDataSource();
        dataSourceConfig.createSchema();
        dataSourceConfig.populateSchema("dataset.xml");
    }


    protected DataSource getDataSource() {
        return dataSourceConfig.getDataSource();
    }
}
