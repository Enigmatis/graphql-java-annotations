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
                .name(name + "Connection")
                .field(newFieldDefinition()
                        .name("totalCount")
                        .description("The total number of the elements")
                        .type(GraphQLInt))
                .field(newFieldDefinition()
                        .name("data")
                        .description("The data itself")
                        .type(nodeType))
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
