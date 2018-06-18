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
package graphql.annotations.directives;

public class DirectiveArgument {
    private String name;
    private String defaultValue;
    private String description;
    private Class<?> type;

    public DirectiveArgument(String name, Class<?> type, String defaultValue, String description) {
        assert (name != null);
        assert (type != null);
        this.name = name;
        this.defaultValue = defaultValue;
        this.description = description;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public Class<?> getType() {
        return type;
    }
}
