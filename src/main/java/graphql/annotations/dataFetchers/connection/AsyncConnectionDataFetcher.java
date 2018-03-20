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
package graphql.annotations.dataFetchers.connection;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class AsyncConnectionDataFetcher<T> implements DataFetcher<CompletableFuture<graphql.relay.Connection<T>>> {
    private final ConnectionDataFetcher<T> connectionDataFetcher;

    @SuppressWarnings("unchecked")
    public AsyncConnectionDataFetcher(ConnectionDataFetcher<T> connectionFetcher) {
        this.connectionDataFetcher = connectionFetcher;
    }

    @Override
    public CompletableFuture<graphql.relay.Connection<T>> get(DataFetchingEnvironment environment) {
        return supplyAsync(() -> connectionDataFetcher.get(environment));
    }
}
