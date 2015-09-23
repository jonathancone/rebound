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

package indo.sql;

import java.lang.reflect.Array;
import java.util.Optional;

/**
 * Created by jcone on 8/3/15.
 */
public class ArrayBindingResolver extends AbstractBindingResolver {

    @Override
    public String resolve(int nextIndex, Parameter parameter) {

        int length = 0;

        Optional<Object> value = parameter.getValue();

        if (value.isPresent()) {

            if (value.get() instanceof Array) {
                length = Array.getLength(value.get());
                parameter.addIndexes(nextIndex, length);
            }
        }

        return generateBindingPlaceholders(length);
    }
}
