package graphql.annotations.connection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractSimplePaginatedData<T> implements SimplePaginatedData<T>{
    private Iterable<T> data;

    public AbstractSimplePaginatedData(Iterable<T> data) {
        this.data = data;
    }

    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }

    @Override
    public List<T> getData() {
        List<T> dataList = new ArrayList<>();
        data.forEach(dataList::add);
        return dataList;
    }
}
