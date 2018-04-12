package graphql.annotations.connection.simple;

import graphql.relay.Relay;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;

import java.util.List;

import static graphql.Scalars.GraphQLInt;
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
}
