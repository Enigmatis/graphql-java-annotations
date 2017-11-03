package graphql.annotations.processor.util;

import graphql.annotations.annotationTypes.GraphQLConnection;
import graphql.schema.*;

import java.lang.reflect.AccessibleObject;
import java.util.Arrays;
import java.util.List;

public class ConnectionUtil {
    private static final List<Class> TYPES_FOR_CONNECTION = Arrays.asList(GraphQLObjectType.class, GraphQLInterfaceType.class, GraphQLUnionType.class, GraphQLTypeReference.class);

    public static boolean isConnection(AccessibleObject obj, GraphQLOutputType type) {
        if (type instanceof graphql.schema.GraphQLNonNull) {
            type = (GraphQLOutputType) ((GraphQLNonNull) type).getWrappedType();
        }
        final GraphQLOutputType actualType = type;
        return obj.isAnnotationPresent(GraphQLConnection.class) &&
                actualType instanceof GraphQLList && TYPES_FOR_CONNECTION.stream().anyMatch(aClass -> aClass.isInstance(((GraphQLList) actualType).getWrappedType()));
    }

}
