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
package graphql.annotations.processor.retrievers.fieldBuilders;

import graphql.annotations.annotationTypes.GraphQLDeprecate;

import java.lang.reflect.AccessibleObject;

public class DeprecateBuilder implements Builder<String> {
    private AccessibleObject object;
    private final String DEFAULT_DEPRECATION_DESCRIPTION = "Deprecated";

    public DeprecateBuilder(AccessibleObject object) {
        this.object = object;
    }

    @Override
    public String build() {
        GraphQLDeprecate deprecate = object.getAnnotation(GraphQLDeprecate.class);
        if (deprecate != null) {
            return deprecate.value().isEmpty() ? DEFAULT_DEPRECATION_DESCRIPTION : deprecate.value();
        }
        if (object.getAnnotation(Deprecated.class) != null) {
            return DEFAULT_DEPRECATION_DESCRIPTION;
        }
        return null;
    }
}
