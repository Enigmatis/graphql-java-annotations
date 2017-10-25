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
package graphql.annotations.connection;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.annotations.*;
import graphql.annotations.connection.ConnectionFetcher;
import graphql.annotations.connection.GraphQLConnection;
import graphql.annotations.util.CustomRelay;
import graphql.relay.Relay;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static graphql.annotations.util.RelayKit.EMPTY_CONNECTION;
import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@SuppressWarnings("unchecked")
public class GraphQLConnectionTest {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

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

    public static class DuplicateTest {
        @GraphQLField
        public TestListField field1;

        @GraphQLField
        public TestListField2 field2;
    }

    public static class TestListField {
        @GraphQLField
        @GraphQLConnection
        public List<Obj> objs;

        public TestListField(List<Obj> objs) {
            this.objs = objs;
        }
    }

    public static class TestListField2 {
        @GraphQLField
        @GraphQLConnection
        public List<Obj> objs;

        public TestListField2(List<Obj> objs) {
            this.objs = objs;
        }
    }

    @Test
    public void fieldList() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestListField.class);
        GraphQLSchema schema = newSchema().query(object).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ objs(first: 1) { edges { cursor node { id, val } } } }",
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
        @GraphQLConnection(name = "objStream")
        public Stream<Obj> getObjStream() {
            Obj[] a = new Obj[objs.size()];
            return Stream.of(objs.toArray(a));
        }

        @GraphQLField
        @GraphQLConnection(name = "objStreamWithParam")
        public Stream<Obj> getObjStreamWithParam(@GraphQLName("filter") String filter) {
            return this.objs.stream().filter( obj -> obj.val.startsWith(filter));
        }

        @GraphQLField
        @GraphQLConnection(name = "nonNullObjs")
        @GraphQLNonNull
        public List<Obj> getNonNullObjs() {
            return this.objs;
        }

        @GraphQLField
        @GraphQLConnection(name = "nullObj")
        public List<Obj> getNullObj() {
            return null;
        }


    }

    @Test
    public void methodList() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestConnections.class);
        GraphQLSchema schema = newSchema().query(object).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ objs(first: 1) { edges { cursor node { id, val } } } }",
                new TestConnections(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world"))));

        assertTrue(result.getErrors().isEmpty());

        testResult("objs", result);

    }

    @Test
    public void customRelayMethodList() {
        try {
            GraphQLAnnotations.getInstance().setRelay(new CustomRelay());
            GraphQLObjectType object = GraphQLAnnotations.object(TestConnections.class);
            GraphQLSchema schema = newSchema().query(object).build();

            graphql.schema.GraphQLObjectType f = (GraphQLObjectType) schema.getType("ObjConnection");
            assertTrue(f.getFieldDefinitions().size() == 4);
            assertTrue(f.getFieldDefinition("nodes").getType() instanceof GraphQLList);
            assertEquals(((GraphQLList)f.getFieldDefinition("nodes").getType()).getWrappedType().getName(), "Obj");

            GraphQLObjectType pageInfo = (GraphQLObjectType) schema.getType("PageInfo");
            assertTrue(pageInfo.getFieldDefinition("additionalInfo") != null);
        } finally {
            GraphQLAnnotations.getInstance().setRelay(new Relay());
        }
    }

    public void testResult(String name, ExecutionResult result) {
        Map<String, Map<String, List<Map<String, Map<String, Object>>>>> data = result.getData();
        List<Map<String, Map<String, Object>>> edges = data.get(name).get("edges");

        assertEquals(edges.size(), 1);
        assertEquals(edges.get(0).get("node").get("id"), "1");
        assertEquals(edges.get(0).get("node").get("val"), "test");
    }

    @Test
    public void methodStream() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestConnections.class);
        GraphQLSchema schema = newSchema().query(object).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ objStream(first: 1) { edges { cursor node { id, val } } } }",
                new TestConnections(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world"))));

        assertTrue(result.getErrors().isEmpty());

        testResult("objStream", result);
    }

    @Test
    public void methodNonNull() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestConnections.class);
        GraphQLSchema schema = newSchema().query(object).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ nonNullObjs(first: 1) { edges { cursor node { id, val } } } }",
                new TestConnections(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world"))));

        assertTrue(result.getErrors().isEmpty());

        testResult("nonNullObjs", result);
    }

    @Test
    public void methodNull() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestConnections.class);
        GraphQLSchema schema = newSchema().query(object).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ nullObj(first: 1) { edges { cursor node { id, val } } } }",
                new TestConnections(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world"))));

        assertTrue(result.getErrors().isEmpty());

        Map<String, Map<String, List<Map<String, Map<String, Object>>>>> data = result.getData();

        assertEquals(data.get("nullObj"), null);
    }

    @Test
    public void methodListWithParam() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestConnections.class);
        GraphQLSchema schema = newSchema().query(object).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ objStreamWithParam(first: 2, filter:\"hel\") { edges { cursor node { id, val } } } }",
                new TestConnections(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world"), new Obj("4", "hello world"), new Obj("5", "hello again"))));

        assertTrue(result.getErrors().isEmpty());

        Map<String, Map<String, List<Map<String, Map<String, Object>>>>> data = result.getData();
        List<Map<String, Map<String, Object>>> edges = data.get("objStreamWithParam").get("edges");

        assertEquals(edges.size(), 2);
        assertEquals(edges.get(0).get("node").get("id"), "2");
        assertEquals(edges.get(0).get("node").get("val"), "hello");
        assertEquals(edges.get(1).get("node").get("id"), "4");
        assertEquals(edges.get(1).get("node").get("val"), "hello world");
    }


    public static class CustomConnection implements ConnectionFetcher {

        public CustomConnection(Object o) {
        }

        @Override
        public Object get(DataFetchingEnvironment environment) {
            return EMPTY_CONNECTION;
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

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ objs(first: 1) { edges { cursor node { id, val } } } }",
                new TestCustomConnection(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world"))));

        assertTrue(result.getErrors().isEmpty());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        Map<String, Object> objs = (Map<String, Object>) (data.get("objs"));
        List edges = (List) objs.get("edges");
        assertEquals(edges.size(), 0);
    }

    @Test
    public void duplicateConnection() {
        try {
            GraphQLObjectType object = GraphQLAnnotations.object(DuplicateTest.class);
            GraphQLSchema schema = newSchema().query(object).build();
        } catch (GraphQLAnnotationsException e) {
            fail("Schema cannot be created",e);
        }
    }

}
