/**
 * Copyright 2016 Yurii Rashkovskii
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations.connection.simple;


import graphql.relay.Connection;
import graphql.relay.Edge;
import graphql.relay.PageInfo;

import java.util.List;

/**
 * This is a more intuitive implementation for paging. You send back only the data that is requested,
 * along with "overall" - the amount of all the entities
 *
 * @param <T> The type of the entities
 */
public interface SimpleConnection<T> extends Connection<T> {

    /**
     * Get the list of the entities
     *
     * @return List of entities
     */
    List<T> getData();

    /**
     * The amount of entities
     *
     * @return The amount of entities
     */
    long getTotalCount();

    @Override
    default List<Edge<T>> getEdges() {
        throw new UnsupportedOperationException("Simple paging doesn't have edges");
    }

    @Override
    default PageInfo getPageInfo() {
        throw new UnsupportedOperationException("Simple paging doesn't have page info");
    }
}
