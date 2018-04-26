package graphql.annotations.connection.simple;


import graphql.relay.Connection;
import graphql.relay.Edge;
import graphql.relay.PageInfo;

import java.util.List;

/**
 * This is a more intuitive implementation for paging. You send back only the data that is requested,
 * along with "overall" - the amount of all the entities
 *
 * @param <T> The type of the entities
 */
public interface SimpleConnection<T> extends Connection<T> {

    /**
     * Get the list of the entities
     *
     * @return List of entities
     */
    List<T> getData();

    /**
     * The amount of entities
     *
     * @return The amount of entities
     */
    long getTotalCount();

    @Override
    default List<Edge<T>> getEdges() {
        throw new UnsupportedOperationException("Simple paging doesn't have edges");
    }

    @Override
    default PageInfo getPageInfo() {
        throw new UnsupportedOperationException("Simple paging doesn't have page info");
    }
}
