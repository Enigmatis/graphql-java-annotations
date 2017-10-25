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

import graphql.relay.*;
import graphql.schema.DataFetchingEnvironment;

import java.util.ArrayList;
import java.util.List;

public class EnhancedConnectionFetcher<T> implements ConnectionFetcher<Connection<T>> {

    private PaginationDataFetcher<T> paginationDataFetcher;

    public EnhancedConnectionFetcher(PaginationDataFetcher<T> paginationDataFetcher) {
        this.paginationDataFetcher = paginationDataFetcher;
    }

    @Override
    public Connection<T> get(DataFetchingEnvironment environment) {
        List<Edge<T>> edges = buildEdges(paginationDataFetcher.get(environment));
        PageInfo pageInfo = getPageInfo(edges);
        return new DefaultConnection<>(edges, pageInfo);
    }

    private PageInfo getPageInfo(List<Edge<T>> edges) {
        ConnectionCursor firstCursor = edges.get(0).getCursor();
        ConnectionCursor lastCursor = edges.get(edges.size()-1).getCursor();
        return new DefaultPageInfo(
                firstCursor,
                lastCursor,
                paginationDataFetcher.hasPreviousPage(firstCursor.getValue()),
                paginationDataFetcher.hasNextPage(lastCursor.getValue())
        );
    }

    private List<Edge<T>> buildEdges(List<T> data) {
        List<Edge<T>> edges = new ArrayList<>();
        for (T object : data) {
            edges.add(new DefaultEdge<>(object, new DefaultConnectionCursor(paginationDataFetcher.getCursor(object))));
        }
        return edges;
    }
}
