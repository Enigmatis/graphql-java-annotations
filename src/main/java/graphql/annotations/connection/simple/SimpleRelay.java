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
package graphql.annotations.connection.simple;

import graphql.relay.Relay;
import graphql.schema.*;

import java.util.ArrayList;
import java.util.List;

import static graphql.Scalars.GraphQLInt;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class SimpleRelay extends Relay {

    @Override
    public GraphQLObjectType connectionType(String name, GraphQLObjectType edgeType, List<GraphQLFieldDefinition> connectionFields) {
        return edgeType;
    }

    @Override
    public GraphQLObjectType edgeType(String name, GraphQLOutputType nodeType, GraphQLInterfaceType nodeInterface, List<GraphQLFieldDefinition> edgeFields) {
        return newObject()
                .name(name + "Chunk")
                .field(newFieldDefinition()
                        .name("totalCount")
                        .description("The total number of the elements")
                        .type(GraphQLInt))
                .field(newFieldDefinition()
                        .name("data")
                        .description("The data itself")
                        .type(new GraphQLList(nodeType)))
                .build();
    }

    @Override
    public List<GraphQLArgument> getConnectionFieldArguments() {
        List<GraphQLArgument> args = new ArrayList<>();
        args.add(newArgument()
                .name("before")
                .description("fetching only nodes before this node (exclusive)")
                .type(GraphQLInt)
                .build());
        args.add(newArgument()
                .name("after")
                .description("fetching only nodes after this node (exclusive)")
                .type(GraphQLInt)
                .build());
        args.add(newArgument()
                .name("first")
                .description("fetching only the first certain number of nodes")
                .type(GraphQLInt)
                .build());
        args.add(newArgument()
                .name("last")
                .description("fetching only the last certain number of nodes")
                .type(GraphQLInt)
                .build());
        return args;
    }
}
