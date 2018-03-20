package graphql.annotations.connection;

public class SimplePaginatedDataImpl<T> extends AbstractSimplePaginatedData<T> {

    private long overAll;

    public SimplePaginatedDataImpl(Iterable<T> data, long overAll) {
        super(data);
        this.overAll = overAll;
    }

    @Override
    public long getOverAll() {
        return overAll;
    }
}
