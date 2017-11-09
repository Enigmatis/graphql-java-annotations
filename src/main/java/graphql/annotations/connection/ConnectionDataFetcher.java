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

import graphql.relay.Connection;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;

import static graphql.annotations.processor.util.ReflectionKit.constructNewInstance;


public class ConnectionDataFetcher<T> implements DataFetcher<Connection<T>> {
    private final DataFetcher<?> actualDataFetcher;
    private final Constructor<ConnectionFetcher<T>> constructor;

    @SuppressWarnings("unchecked")
    public ConnectionDataFetcher(Class<? extends ConnectionFetcher<T>> connection, DataFetcher<?> actualDataFetcher) {
        this.actualDataFetcher =  actualDataFetcher;
        Optional<Constructor<ConnectionFetcher<T>>> constructor =
                Arrays.stream(connection.getConstructors()).
                        filter(c -> c.getParameterCount() == 1).
                        map(c -> (Constructor<ConnectionFetcher<T>>) c).
                        findFirst();
        if (constructor.isPresent()) {
            this.constructor = constructor.get();
        } else {
            throw new IllegalArgumentException(connection.getSimpleName() + " doesn't have a single argument constructor");
        }
    }

    @Override
    public Connection<T> get(DataFetchingEnvironment environment) {
        ConnectionFetcher<T> conn = constructNewInstance(constructor, actualDataFetcher);
        return conn.get(environment);
    }
}
