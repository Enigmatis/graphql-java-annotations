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
import graphql.relay.DefaultConnection;
import graphql.relay.DefaultPageInfo;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static graphql.annotations.ReflectionKit.constructNewInstance;

public class ConnectionDataFetcher<T> implements DataFetcher<Connection<T>> {
    private final Class<? extends ConnectionFetcher<T>> connection;
    private final DataFetcher<T> actualDataFetcher;
    private final Constructor<ConnectionFetcher> constructor;

    public ConnectionDataFetcher(Class<? extends ConnectionFetcher<T>> connection, DataFetcher<T> actualDataFetcher) {
        this.connection = connection;
        this.actualDataFetcher = actualDataFetcher;
        ValidateDataFetcher();
        Optional<Constructor<ConnectionFetcher>> constructor =
                Arrays.asList(this.connection.getConstructors()).stream().
                        filter(c -> c.getParameterCount() == 1).
                        map(c -> (Constructor<ConnectionFetcher>) c).
                        findFirst();
        if (constructor.isPresent()) {
            this.constructor = constructor.get();
        } else {
            throw new IllegalArgumentException(connection.getSimpleName() + " doesn't have a single argument constructor");
        }
    }

    private void ValidateDataFetcher() {
        if(connection.isAssignableFrom(EnhancedConnectionFetcher.class)) {
            if(!(actualDataFetcher instanceof PaginationDataFetcher)) {
                throw new GraphQLConnectionException(actualDataFetcher.getClass().getSimpleName() + " must extends PaginationDataFetcher");
            }
        }
    }

    @Override
    public Connection<T> get(DataFetchingEnvironment environment) {
        if(connection.isAssignableFrom(EnhancedConnectionFetcher.class)) {
            ConnectionFetcher<Connection<T>> conn = constructNewInstance(constructor, actualDataFetcher);
            return conn.get(environment);
        }
        else {
            T data = actualDataFetcher.get(environment);
            if (data != null) {
                ConnectionFetcher<Connection<T>> conn = constructNewInstance(constructor, data);
                return conn.get(environment);
            }
            return new DefaultConnection<>(Collections.emptyList(), new DefaultPageInfo(null, null, false, false));
        }
    }
}
