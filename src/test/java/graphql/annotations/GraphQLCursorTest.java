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

import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.relay.PageInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class GraphQLCursorTest {

    @AllArgsConstructor
    public static class Obj {
        @GraphQLField
        public String id;
        @GraphQLField
        public String val;
    }

    @AllArgsConstructor
    public static class TestListField {
        @GraphQLField @GraphQLCursor
        public List<Obj> objs;
    }

    @Test @SneakyThrows
    public void fieldList() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestListField.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = new GraphQL(schema).execute("{ objs(first: 1) { edges { cursor node { id, val } } } }",
                new TestListField(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world"))));
        assertTrue(result.getErrors().isEmpty());
    }

    @AllArgsConstructor
    public static class TestListMethod {
        private List<Obj> objs;
        @GraphQLField @GraphQLCursor
        public List<Obj> getObjs() {
            return this.objs;
        }
    }

    @Test @SneakyThrows
    public void methodList() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestListMethod.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = new GraphQL(schema).execute("{ objs(first: 1) { edges { cursor node { id, val } } } }",
                new TestListMethod(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world"))));

        assertTrue(result.getErrors().isEmpty());
    }


    public static class CustomConnection implements Connection {

        public CustomConnection(Object o) {
        }

        @Override
        public Object get(DataFetchingEnvironment environment) {
            graphql.relay.Connection connection = new graphql.relay.Connection();
            connection.setEdges(Collections.emptyList());
            PageInfo pageInfo = new PageInfo();
            connection.setPageInfo(pageInfo);
            return connection;
        }
    }
    @AllArgsConstructor
    public static class TestCustomConnection {
        private List<Obj> objs;
        @GraphQLField @GraphQLCursor(connection = CustomConnection.class)
        public List<Obj> getObjs() {
            return this.objs;
        }
    }

    @Test @SneakyThrows
    public void customConnection() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestCustomConnection.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = new GraphQL(schema).execute("{ objs(first: 1) { edges { cursor node { id, val } } } }",
                new TestCustomConnection(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world"))));

        assertTrue(result.getErrors().isEmpty());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        Map<String, Object> objs = (Map<String, Object>) (data.get("objs"));
        List edges = (List) objs.get("edges");
        assertEquals(edges.size(), 0);
    }

}
