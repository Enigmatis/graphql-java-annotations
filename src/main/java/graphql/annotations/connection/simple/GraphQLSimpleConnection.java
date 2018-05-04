package graphql.annotations.connection.simple;

import graphql.annotations.connection.GraphQLConnection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@GraphQLConnection(connectionFetcher = SimplePaginatedDataConnectionFetcher.class, validator = SimplePaginatedDataConnectionTypeValidator.class, connectionType = SimpleRelay.class)
public @interface GraphQLSimpleConnection {
}
