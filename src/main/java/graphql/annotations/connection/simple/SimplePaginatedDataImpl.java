package graphql.annotations.connection.simple;

import graphql.relay.Edge;
import graphql.relay.PageInfo;

import java.util.List;

public class SimplePaginatedDataImpl<T> extends AbstractSimplePaginatedData<T> {

    private long totalCount;

    public SimplePaginatedDataImpl(Iterable<T> data, long totalCount) {
        super(data);
        this.totalCount = totalCount;
    }

    @Override
    public long getTotalCount() {
        return totalCount;
    }

    @Override
    public List<Edge<T>> getEdges() {
        throw new UnsupportedOperationException("Simple paging doesn't have edges");
    }

    @Override
    public PageInfo getPageInfo() {
        throw new UnsupportedOperationException("Simple paging doesn't have page info");
    }
}