package graphql.annotations.connection;

import graphql.schema.DataFetcher;

import java.util.List;

public interface PaginationDataFetcher<T> extends DataFetcher<List<T>> {

    boolean hasNextPage(String lastCursor);

    boolean hasPreviousPage(String firstCursor);

    String getCursor(T entity);
}
