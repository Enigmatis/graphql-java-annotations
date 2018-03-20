package graphql.annotations.connection;

import graphql.annotations.annotationTypes.GraphQLDataFetcher;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SimplePaginatedDataConnectionTypeValidator implements ConnectionValidator{

    @Override
    public void validate(AccessibleObject field) {
        if (field instanceof Field) {
            if (field.isAnnotationPresent(GraphQLSimpleConnection.class) && !field.isAnnotationPresent(GraphQLDataFetcher.class)) {
                throw new GraphQLConnectionException("Please don't use @GraphQLConnection on" + ((Field) field).getName() +
                        " without @GraphQLDataFetcher, because " +
                        "neither PropertyDataFetcher nor FieldDataFetcher know how to handle connection");
            }

            if (!SimplePaginatedData.class.isAssignableFrom(((Field) field).getType())) {
                throw new GraphQLConnectionException(((Field) field).getName() + " type must be SimplePaginatedData");
            }
        } else {
            if (!SimplePaginatedData.class.isAssignableFrom(((Method) field).getReturnType())) {
                throw new GraphQLConnectionException(((Method) field).getName() + " return type must be SimplePaginatedData");
            }
        }
    }
}
