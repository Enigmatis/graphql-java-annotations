package graphql.annotations.connection.simple;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;

import static graphql.annotations.processor.util.ReflectionKit.constructNewInstance;

public class SimpleConnectionDataFetcher<T> implements SimpleConnectionFetcher<T> {
    private final DataFetcher<?> actualDataFetcher;
    private final Constructor<SimpleConnectionFetcher<T>> constructor;

    @SuppressWarnings("unchecked")
    public SimpleConnectionDataFetcher(Class<? extends SimpleConnectionFetcher<T>> connection, DataFetcher<?> actualDataFetcher) {
        this.actualDataFetcher = actualDataFetcher;
        Optional<Constructor<SimpleConnectionFetcher<T>>> constructor =
                Arrays.stream(connection.getConstructors()).
                        filter(c -> c.getParameterCount() == 1).
                        map(c -> (Constructor<SimpleConnectionFetcher<T>>) c).
                        findFirst();
        if (constructor.isPresent()) {
            this.constructor = constructor.get();
        } else {
            throw new IllegalArgumentException(connection.getSimpleName() + " doesn't have a single argument constructor");
        }
    }

    @Override
    public SimpleConnection<T> get(DataFetchingEnvironment environment) {
        SimpleConnectionFetcher<T> conn = constructNewInstance(constructor, actualDataFetcher);
        return conn.get(environment);
    }
}
