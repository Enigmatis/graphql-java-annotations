/**
 * Copyright 2016 Yurii Rashkovskii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations;

import graphql.annotations.util.Base64;
import graphql.relay.ConnectionCursor;
import graphql.relay.DefaultConnection;
import graphql.relay.DefaultConnectionCursor;
import graphql.relay.DefaultEdge;
import graphql.relay.DefaultPageInfo;
import graphql.relay.Edge;
import graphql.relay.PageInfo;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static graphql.annotations.util.RelayKit.EMPTY_CONNECTION;

public class StreamConnection implements DataFetcher, Connection {

    private final Stream<?> stream;
    private static final String DUMMY_CURSOR_PREFIX = "stream-cursor";

    public StreamConnection(Stream<?> stream) {
        this.stream = stream;
    }

    private Stream<Edge<Object>> buildEdges() {
        AtomicInteger ix = new AtomicInteger();
        return stream.map(obj -> {
            ConnectionCursor connectionCursor = new DefaultConnectionCursor(createCursor(ix.incrementAndGet()));
            return new DefaultEdge<>(obj, connectionCursor);
        });
    }


    @Override
    public Object get(DataFetchingEnvironment environment) {

        int afterOffset = getOffsetFromCursor(environment.getArgument("after"), 0);
        int beforeOffset = getOffsetFromCursor(environment.getArgument("before"), Integer.MAX_VALUE);

        Stream<Edge<Object>> edgesStream = buildEdges().skip(afterOffset).limit(beforeOffset - afterOffset);

        List<Edge<Object>> edges = edgesStream.collect(Collectors.toList());

        if (edges.size() == 0) {
            return EMPTY_CONNECTION;
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
            return EMPTY_CONNECTION;
        }

        Edge firstEdge = edges.get(0);
        Edge lastEdge = edges.get(edges.size() - 1);
        boolean hasPrevious = !firstEdge.getCursor().equals(firstPresliceCursor);
        boolean hasNext = !lastEdge.getCursor().equals(lastPresliceCursor);

        PageInfo pageInfo = new DefaultPageInfo(
                firstEdge.getCursor(),
                lastEdge.getCursor(),
                hasPrevious, hasNext
        );

        return new DefaultConnection<>(edges, pageInfo);
    }

    private int getOffsetFromCursor(String cursor, int defaultValue) {
        if (cursor == null) return defaultValue;
        String string = Base64.fromBase64(cursor);
        return Integer.parseInt(string.substring(DUMMY_CURSOR_PREFIX.length()));
    }

    private String createCursor(int offset) {
        return Base64.toBase64(DUMMY_CURSOR_PREFIX + Integer.toString(offset));
    }

}
