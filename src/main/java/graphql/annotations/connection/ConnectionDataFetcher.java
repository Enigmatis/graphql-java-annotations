/**
 * Copyright 2016 Yurii Rashkovskii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations.connection;

import graphql.annotations.ExtensionDataFetcherWrapper;
import graphql.relay.Connection;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldDataFetcher;
import graphql.schema.PropertyDataFetcher;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static graphql.annotations.ReflectionKit.constructNewInstance;

public class ConnectionDataFetcher<T> implements DataFetcher<Connection<T>> {
    private final Class<? extends ConnectionFetcher<T>> connection;
    private final DataFetcher<PaginatedData<T>> actualDataFetcher;
    private final Constructor<ConnectionFetcher<T>> constructor;
    private final List<Class<?>> blackListOfDataFetchers = Arrays.asList(PropertyDataFetcher.class, FieldDataFetcher.class);

    public ConnectionDataFetcher(Class<? extends ConnectionFetcher<T>> connection, DataFetcher<T> actualDataFetcher) {
        validateDataFetcher(actualDataFetcher);
        this.connection = connection;
        this.actualDataFetcher = (DataFetcher<PaginatedData<T>>) actualDataFetcher;
        Optional<Constructor<ConnectionFetcher<T>>> constructor =
                Arrays.stream(this.connection.getConstructors()).
                        filter(c -> c.getParameterCount() == 1).
                        map(c -> (Constructor<ConnectionFetcher<T>>) c).
                        findFirst();
        if (constructor.isPresent()) {
            this.constructor = constructor.get();
        } else {
            throw new IllegalArgumentException(connection.getSimpleName() + " doesn't have a single argument constructor");
        }
    }

    private void validateDataFetcher(DataFetcher<?> dataFetcher) {
        if( dataFetcher instanceof ExtensionDataFetcherWrapper) {
            dataFetcher = ((ExtensionDataFetcherWrapper) dataFetcher).getUnwrappedDataFetcher();
        }
        final DataFetcher<?> finalDataFetcher = dataFetcher;
        if(blackListOfDataFetchers.stream().anyMatch(aClass -> aClass.isInstance(finalDataFetcher))) {
            throw new GraphQLConnectionException("Please don't use @GraphQLConnection on a field, because " +
                    "neither PropertyDataFetcher nor FieldDataFetcher know how to handle connection");
        }
    }

    @Override
    public Connection<T> get(DataFetchingEnvironment environment) {
        ConnectionFetcher<T> conn = constructNewInstance(constructor, actualDataFetcher);
        return conn.get(environment);
    }
}
