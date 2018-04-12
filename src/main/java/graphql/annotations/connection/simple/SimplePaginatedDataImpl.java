package graphql.annotations.connection.simple;

public class SimplePaginatedDataImpl<T> extends AbstractSimplePaginatedData<T> {

    private long overAllCount;

    public SimplePaginatedDataImpl(Iterable<T> data, long overAllCount) {
        super(data);
        this.overAllCount = overAllCount;
    }

    @Override
    public long getOverAllCount() {
        return overAllCount;
    }
}