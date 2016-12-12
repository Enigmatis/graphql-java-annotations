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
package graphql.annotations;

import graphql.schema.GraphQLObjectType;

public class GraphQLObjectTypeWrapper extends GraphQLObjectType implements GraphQLObjectBackedByClass {

    private final Class<?> objectClass;

    public GraphQLObjectTypeWrapper(Class<?> objectClass, GraphQLObjectType objectType) {
        super(objectType.getName(), objectType.getDescription(), objectType.getFieldDefinitions(),
                objectType.getInterfaces());
        this.objectClass = objectClass;
    }

    @Override
    public Class<?> getObjectClass() {
        return objectClass;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GraphQLObjectType &&
               ((GraphQLObjectType) obj).getName().contentEquals(getName()) &&
               ((GraphQLObjectType) obj).getFieldDefinitions().equals(getFieldDefinitions());
    }
}
