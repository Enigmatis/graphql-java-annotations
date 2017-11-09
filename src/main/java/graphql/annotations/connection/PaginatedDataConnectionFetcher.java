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
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Use this class in {@link GraphQLConnection} to do a real pagination,
 * i.e you fetch each time the relevant data, you make the cursors and
 * you decide if there are previous or next pages
 * <p>
 * Note: If you are using the connection, the return type of the associated dataFetcher must implement {@link PaginatedData}
 *
 * @param <T> the entity type that is paginated
 */
public class PaginatedDataConnectionFetcher<T> implements ConnectionFetcher<T> {

    private DataFetcher<PaginatedData<T>> paginationDataFetcher;

    public PaginatedDataConnectionFetcher(DataFetcher<PaginatedData<T>> paginationDataFetcher) {
        this.paginationDataFetcher = paginationDataFetcher;
    }

    @Override
    public Connection<T> get(DataFetchingEnvironment environment) {
        PaginatedData<T> paginatedData = paginationDataFetcher.get(environment);
        if (paginatedData == null) {
            return new DefaultConnection<>(Collections.emptyList(), new DefaultPageInfo(null,null,false,false));
        }
        List<Edge<T>> edges = buildEdges(paginatedData);
        PageInfo pageInfo = getPageInfo(edges, paginatedData);
        return new DefaultConnection<>(edges, pageInfo);
    }

    private PageInfo getPageInfo(List<Edge<T>> edges, PaginatedData<T> paginatedData) {
        ConnectionCursor firstCursor = edges.get(0).getCursor();
        ConnectionCursor lastCursor = edges.get(edges.size() - 1).getCursor();
        return new DefaultPageInfo(
                firstCursor,
                lastCursor,
                paginatedData.hasPreviousPage(),
                paginatedData.hasNextPage()
        );
    }

    private List<Edge<T>> buildEdges(PaginatedData<T> paginatedData) {
        Iterator<T> data = paginatedData.iterator();
        List<Edge<T>> edges = new ArrayList<>();
        for (; data.hasNext(); ) {
            T entity = data.next();
            edges.add(new DefaultEdge<>(entity, new DefaultConnectionCursor(paginatedData.getCursor(entity))));
        }
        return edges;
    }
}
