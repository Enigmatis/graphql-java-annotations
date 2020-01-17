package graphql.annotations.processor.util;

import graphql.schema.*;

public class GraphQLTypeNameResolver {
    public static String getName(GraphQLSchemaElement graphQLSchemaElement) {
        try {
            return ((GraphQLNamedSchemaElement) graphQLSchemaElement).getName();
        } catch (Exception exception) {
            if (graphQLSchemaElement instanceof GraphQLNonNull) {
                return getName(((GraphQLNonNull) graphQLSchemaElement).getWrappedType());
            } else if (graphQLSchemaElement instanceof GraphQLList) {
                GraphQLType iterator = (GraphQLType) graphQLSchemaElement;
                do {
                    iterator = ((GraphQLList) iterator).getWrappedType();
                } while (iterator instanceof GraphQLList);
                return getName(iterator);
            } else {
                throw new RuntimeException("Cannot determine name for schema element");
            }
        }
    }
}
