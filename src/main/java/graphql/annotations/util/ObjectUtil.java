/**
 * Copyright 2016 Yurii Rashkovskii
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
 */
package graphql.annotations.util;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

public  class ObjectUtil {

    public static Map<String, Field> getAllFields(Class c) {
        Map<String, Field> fields;

        if (c.getSuperclass() != null) {
            fields = getAllFields(c.getSuperclass());
        } else {
            fields = new TreeMap<>();
        }

        for (Field f : c.getDeclaredFields()) {
            fields.put(f.getName(), f);
        }

        return fields;
    }
}
