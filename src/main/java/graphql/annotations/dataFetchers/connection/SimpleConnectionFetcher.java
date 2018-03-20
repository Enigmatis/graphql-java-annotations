package graphql.annotations.dataFetchers.connection;

import graphql.schema.DataFetcher;

public interface SimpleConnectionFetcher<T> extends DataFetcher<SimpleConnection<T>> {
}
