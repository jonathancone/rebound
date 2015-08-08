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

/**
 * Created by jcone on 8/7/15.
 */
public class UnexpectedSqlException extends RuntimeException {
    public UnexpectedSqlException() {
    }

    public UnexpectedSqlException(String message) {
        super(message);
    }

    public UnexpectedSqlException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedSqlException(Throwable cause) {
        super(cause);
    }

    public UnexpectedSqlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}