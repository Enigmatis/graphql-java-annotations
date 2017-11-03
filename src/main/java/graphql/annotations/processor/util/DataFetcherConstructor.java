package graphql.annotations.processor.util;

import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.schema.DataFetcher;

import java.lang.reflect.Constructor;
import java.util.stream.Stream;

import static graphql.annotations.processor.util.ReflectionKit.constructNewInstance;
import static graphql.annotations.processor.util.ReflectionKit.newInstance;
import static java.util.Arrays.stream;

public class DataFetcherConstructor {
    public DataFetcher constructDataFetcher(String fieldName, GraphQLDataFetcher annotatedDataFetcher) {
        final String[] args;
        if (annotatedDataFetcher.firstArgIsTargetName()) {
            args = Stream.concat(Stream.of(fieldName), stream(annotatedDataFetcher.args())).toArray(String[]::new);
        } else {
            args = annotatedDataFetcher.args();
        }
        if (args.length == 0) {
            return newInstance(annotatedDataFetcher.value());
        } else {
            try {
                final Constructor<? extends DataFetcher> ctr = annotatedDataFetcher.value().getDeclaredConstructor(
                        stream(args).map(v -> String.class).toArray(Class[]::new));
                return constructNewInstance(ctr, (Object[]) args);
            } catch (final NoSuchMethodException e) {
                throw new GraphQLAnnotationsException("Unable to instantiate DataFetcher via constructor for: " + fieldName, e);
            }
        }
    }

}
