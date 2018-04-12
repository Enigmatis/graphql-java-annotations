package graphql.annotations.connection.simple;

import graphql.schema.DataFetcher;

public interface SimpleConnectionFetcher<T> extends DataFetcher<SimpleConnection<T>> {
}
