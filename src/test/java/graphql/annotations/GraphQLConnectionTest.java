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

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.relay.PageInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class GraphQLConnectionTest {

    public static class Obj {
        @GraphQLField
        public String id;
        @GraphQLField
        public String val;

        public Obj(String id, String val) {
            this.id = id;
            this.val = val;
        }
    }

    public static class TestListField {
        @GraphQLField
        @GraphQLConnection
        public List<Obj> objs;

        public TestListField(List<Obj> objs) {
            this.objs = objs;
        }
    }

    @Test
    public void fieldList() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestListField.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = new GraphQL(schema).execute("{ objs(first: 1) { edges { cursor node { id, val } } } }",
                new TestListField(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world"))));
        assertTrue(result.getErrors().isEmpty());

        testResult("objs", result);
    }

    public static class TestConnections {
        private List<Obj> objs;

        public TestConnections(List<Obj> objs) {
            this.objs = objs;
        }

        @GraphQLField
        @GraphQLConnection
        public List<Obj> getObjs() {
            return this.objs;
        }

        @GraphQLField
        @GraphQLConnection
        public Stream<Obj> getObjStream() {
            Obj[] a = new Obj[objs.size()];
            return Stream.of(objs.toArray(a));
        }
    }

    @Test
    public void methodList() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestConnections.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = new GraphQL(schema).execute("{ objs(first: 1) { edges { cursor node { id, val } } } }",
                new TestConnections(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world"))));

        assertTrue(result.getErrors().isEmpty());

        testResult("objs", result);

    }

    public void testResult(String name, ExecutionResult result) {
        Map<String, Map<String, List<Map<String, Map<String, Object>>>>> data = (Map<String, Map<String, List<Map<String, Map<String, Object>>>>>) result.getData();
        List<Map<String, Map<String, Object>>> edges = data.get(name).get("edges");

        assertEquals(edges.size(), 1);
        assertEquals(edges.get(0).get("node").get("id"), "1");
        assertEquals(edges.get(0).get("node").get("val"), "test");
    }

    @Test
    public void methodStream() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestConnections.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = new GraphQL(schema).execute("{ objStream(first: 1) { edges { cursor node { id, val } } } }",
                new TestConnections(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world"))));

        assertTrue(result.getErrors().isEmpty());

        testResult("objStream", result);
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

    public static class TestCustomConnection {
        private List<Obj> objs;

        public TestCustomConnection(List<Obj> objs) {
            this.objs = objs;
        }

        @GraphQLField
        @GraphQLConnection(connection = CustomConnection.class)
        public List<Obj> getObjs() {
            return this.objs;
        }
    }

    @Test
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
