package graphql.annotations.connection.simple;

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
}