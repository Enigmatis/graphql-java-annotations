package graphql.annotations.connection;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static java.util.Base64.getEncoder;

public class SimplePaginatedData<T> extends AbstractPaginatedData<T> {
    private static final String DUMMY_CURSOR_PREFIX = "simple-cursor";
    private final String prefix;

    public SimplePaginatedData(boolean hasPreviousPage, boolean hasNextPage, Iterable<T> data) {
        this(hasPreviousPage, hasNextPage, data, DUMMY_CURSOR_PREFIX);
    }

    public SimplePaginatedData(boolean hasPreviousPage, boolean hasNextPage, Iterable<T> data, String prefix) {
        super(hasPreviousPage, hasNextPage, data);
        this.prefix = prefix;
    }

    /**
     * creates the cursor by offset (i.e first entity has cursor 0, the second has 1 and so on)
     * NOTE: to make it consistent, please make the data ordered
     *
     * @param entity the entity
     * @return the cursor of the entity (i.e the offset)
     */
    @Override
    public String getCursor(T entity) {
        Iterator<T> iterator = data.iterator();
        long offset = 0;
        for (; iterator.hasNext(); ) {
            T next = iterator.next();
            if (entity.equals(next)) {
                break;
            }
            offset++;
        }
        return createCursor(offset);
    }

    private String createCursor(long offset) {
        byte[] bytes = (prefix + Long.toString(offset)).getBytes(StandardCharsets.UTF_8);
        return getEncoder().encodeToString(bytes);
    }
}
