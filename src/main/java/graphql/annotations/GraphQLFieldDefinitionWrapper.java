/**
 * Copyright 2016 Yurii Rashkovskii
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations;

import graphql.schema.GraphQLFieldDefinition;

public class GraphQLFieldDefinitionWrapper extends GraphQLFieldDefinition {

    public GraphQLFieldDefinitionWrapper(GraphQLFieldDefinition fieldDefinition) {
        super(fieldDefinition.getName(), fieldDefinition.getDescription(), fieldDefinition.getType(),
                fieldDefinition.getDataFetcher(), fieldDefinition.getArguments(), fieldDefinition.getDeprecationReason());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GraphQLFieldDefinition &&
                ((GraphQLFieldDefinition) obj).getName().contentEquals(getName());
    }
}
