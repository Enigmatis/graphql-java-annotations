package graphql.annotations.connection;

import graphql.relay.*;
import graphql.schema.DataFetchingEnvironment;

import java.util.ArrayList;
import java.util.List;

public class EnhancedConnectionFetcher<T> implements ConnectionFetcher<Connection<T>> {

    private PaginationDataFetcher<T> paginationDataFetcher;

    public EnhancedConnectionFetcher(PaginationDataFetcher<T> paginationDataFetcher) {
        this.paginationDataFetcher = paginationDataFetcher;
    }

    @Override
    public Connection<T> get(DataFetchingEnvironment environment) {
        List<Edge<T>> edges = buildEdges(paginationDataFetcher.get(environment));
        PageInfo pageInfo = getPageInfo(edges);
        return new DefaultConnection<>(edges, pageInfo);
    }

    private PageInfo getPageInfo(List<Edge<T>> edges) {
        ConnectionCursor firstCursor = edges.get(0).getCursor();
        ConnectionCursor lastCursor = edges.get(edges.size()-1).getCursor();
        return new DefaultPageInfo(
                firstCursor,
                lastCursor,
                paginationDataFetcher.hasPreviousPage(firstCursor.getValue()),
                paginationDataFetcher.hasNextPage(lastCursor.getValue())
        );
    }

    private List<Edge<T>> buildEdges(List<T> data) {
        List<Edge<T>> edges = new ArrayList<>();
        for (T object : data) {
            edges.add(new DefaultEdge<>(object, new DefaultConnectionCursor(paginationDataFetcher.getCursor(object))));
        }
        return edges;
    }
}
