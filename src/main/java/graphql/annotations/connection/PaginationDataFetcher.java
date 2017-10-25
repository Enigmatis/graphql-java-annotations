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

import java.util.List;

/**
 * When use connection with {@link EnhancedConnectionFetcher}, the dataFetcher
 * must implements this interface
 * @param <T> the type of the entity that is fetched
 */
public interface PaginationDataFetcher<T> extends DataFetcher<List<T>> {

    /**
     * decides whether this is the last page
     * @param lastCursor the cursor of the last entity that was fetched
     * @return true if there is a next page, o.w false
     */
    boolean hasNextPage(String lastCursor);

    /**
     * decides whether this is the first page
     * @param firstCursor the cursor of the first entity that was fetched
     * @return true if there is a previous page, o.w false
     */
    boolean hasPreviousPage(String firstCursor);

    /**
     * get the cursor of the entity. This method is called when the edges are built.
     * This method is called for every entity that was fetched
     * @param entity the fetched entity (one of them)
     * @return the cursor of the entity
     */
    String getCursor(T entity);
}
