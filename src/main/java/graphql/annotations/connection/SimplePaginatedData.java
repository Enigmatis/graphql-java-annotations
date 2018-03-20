package graphql.annotations.connection;

import graphql.annotations.dataFetchers.connection.SimpleConnection;

/**
 * This class is the result of a simple connection. Every Graphql connection field must return this interface
 * <p>
 * NOTE: this interface extends Iterable. The data is retrieved from the "iterator" function.
 * Please implement the iterator with data structure that has order
 *
 * @param <T> the data of which we paginated over
 */
public interface SimplePaginatedData<T> extends Iterable<T>, SimpleConnection<T> {
}
