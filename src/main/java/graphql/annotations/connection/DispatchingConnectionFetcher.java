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

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.util.List;
import java.util.stream.Stream;

public class DispatchingConnectionFetcher<T> implements ConnectionFetcher<T> {
    private final DataFetcher<T> connection;

    public DispatchingConnectionFetcher(Object o) {
        if (o instanceof List) {
            connection = new ListConnection((List<?>) o);
        } else if (o instanceof Stream) {
            connection = new StreamConnection((Stream<?>) o);
        } else {
            throw new RuntimeException("unsupported type " + o.getClass());
        }
    }

    @Override
    public T get(DataFetchingEnvironment environment) {
        return connection.get(environment);
    }
}
