package graphql.annotations.dataFetchers.connection;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class AsyncSimpleConnectionDataFetcher<T> implements DataFetcher<CompletableFuture<SimpleConnection<T>>> {
    private SimpleConnectionDataFetcher<T> simpleConnectionFetcher;

    public AsyncSimpleConnectionDataFetcher(SimpleConnectionDataFetcher<T> simpleConnectionFetcher) {
        this.simpleConnectionFetcher = simpleConnectionFetcher;
    }

    @Override
    public CompletableFuture<SimpleConnection<T>> get(DataFetchingEnvironment environment) {
        return supplyAsync(() -> simpleConnectionFetcher.get(environment));
    }
}
