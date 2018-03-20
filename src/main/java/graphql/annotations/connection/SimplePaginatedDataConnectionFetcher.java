package graphql.annotations.connection;

import graphql.annotations.dataFetchers.connection.SimpleConnection;
import graphql.annotations.dataFetchers.connection.SimpleConnectionFetcher;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class SimplePaginatedDataConnectionFetcher<T> implements SimpleConnectionFetcher<T> {

    private DataFetcher<SimplePaginatedData<T>> simplePaginatedDataDataFetcher;

    public SimplePaginatedDataConnectionFetcher(DataFetcher<SimplePaginatedData<T>> simplePaginatedDataDataFetcher) {
        this.simplePaginatedDataDataFetcher = simplePaginatedDataDataFetcher;
    }

    @Override
    public SimpleConnection<T> get(DataFetchingEnvironment environment) {
        return simplePaginatedDataDataFetcher.get(environment);
    }
}
