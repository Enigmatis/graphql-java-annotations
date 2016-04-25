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
package graphql.annotations;

import graphql.relay.Base64;
import graphql.relay.ConnectionCursor;
import graphql.relay.Edge;
import graphql.relay.PageInfo;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamConnection implements DataFetcher, Connection {

    private final Stream<?> stream;
    private static final String DUMMY_CURSOR_PREFIX = "stream-cursor";
    
    public StreamConnection(Stream<?> stream) {
        this.stream = stream;
    }

    private Stream<Edge> buildEdges() {
        AtomicInteger ix = new AtomicInteger();
        return stream.map(obj -> new Edge(obj, new ConnectionCursor(createCursor(ix.incrementAndGet()))));
    }


    @Override
    public Object get(DataFetchingEnvironment environment) {

        int afterOffset = getOffsetFromCursor(environment.<String>getArgument("after"), 0);
        int beforeOffset = getOffsetFromCursor(environment.<String>getArgument("before"), Integer.MAX_VALUE);

        Stream<Edge> edgesStream = buildEdges().skip(afterOffset).limit(beforeOffset - afterOffset);

        List<Edge> edges = edgesStream.collect(Collectors.toList());

        if (edges.size() == 0) {
            return emptyConnection();
        }

        Integer first = environment.<Integer>getArgument("first");
        Integer last = environment.<Integer>getArgument("last");

        ConnectionCursor firstPresliceCursor = edges.get(0).getCursor();
        ConnectionCursor lastPresliceCursor = edges.get(edges.size() - 1).getCursor();

        if (first != null) {
            edges = edges.subList(0, first <= edges.size() ? first : edges.size());
        }
        if (last != null) {
            edges = edges.subList(edges.size() - last, edges.size());
        }

        if (edges.size() == 0) {
            return emptyConnection();
        }

        Edge firstEdge = edges.get(0);
        Edge lastEdge = edges.get(edges.size() - 1);

        PageInfo pageInfo = new PageInfo();
        pageInfo.setStartCursor(firstEdge.getCursor());
        pageInfo.setEndCursor(lastEdge.getCursor());
        pageInfo.setHasPreviousPage(!firstEdge.getCursor().equals(firstPresliceCursor));
        pageInfo.setHasNextPage(!lastEdge.getCursor().equals(lastPresliceCursor));

        graphql.relay.Connection connection = new graphql.relay.Connection();
        connection.setEdges(edges);
        connection.setPageInfo(pageInfo);

        return connection;
    }

    private graphql.relay.Connection emptyConnection() {
        graphql.relay.Connection connection = new graphql.relay.Connection();
        connection.setPageInfo(new PageInfo());
        return connection;
    }


//    public ConnectionCursor cursorForObjectInConnection(Object object) {
//        int index = data.indexOf(object);
//        String cursor = createCursor(index);
//        return new ConnectionCursor(cursor);
//    }


    private int getOffsetFromCursor(String cursor, int defaultValue) {
        if (cursor == null) return defaultValue;
        String string = Base64.fromBase64(cursor);
        return Integer.parseInt(string.substring(DUMMY_CURSOR_PREFIX.length()));
    }

    private String createCursor(int offset) {
        String string = Base64.toBase64(DUMMY_CURSOR_PREFIX + Integer.toString(offset));
        return string;
    }
}
