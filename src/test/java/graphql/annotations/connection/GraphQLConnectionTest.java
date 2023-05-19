/**
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

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.annotations.GraphQLHelper;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import graphql.annotations.connection.exceptions.GraphQLConnectionException;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.annotations.processor.util.CustomRelay;
import graphql.relay.Relay;
import graphql.schema.*;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static graphql.annotations.AnnotationsSchemaCreator.newAnnotationsSchema;
import static graphql.annotations.processor.util.RelayKit.EMPTY_CONNECTION;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLSchema.newSchema;
import static java.util.Collections.emptyList;
import static org.testng.Assert.*;

@SuppressWarnings("unchecked")
public class GraphQLConnectionTest {

    private GraphQLAnnotations graphQLAnnotations;

    @BeforeMethod
    public void init() {
        this.graphQLAnnotations = new GraphQLAnnotations();
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
        public List<Obj> objs;

        public TestListField(List<Obj> objs) {
            this.objs = objs;
        }

        @GraphQLField
        @GraphQLConnection
        public PaginatedData<Obj> objs() {
            return new AbstractPaginatedData<Obj>(false, true, objs) {
                @Override
                public String getCursor(Obj entity) {
                    return entity.id;
                }
            };
        }
    }

    public static class TestListField2 {

        public List<Obj> objs;

        public TestListField2(List<Obj> objs) {
            this.objs = objs;
        }

        @GraphQLField
        @GraphQLConnection
        public PaginatedData<Obj> objs() {
            return new AbstractPaginatedData<Obj>(false, true, objs) {
                @Override
                public String getCursor(Obj entity) {
                    return entity.id;
                }
            };
        }
    }

    public static class TestConnectionOnField {
        @GraphQLField
        @GraphQLConnection
        public PaginatedData<Obj> objs;

        public TestConnectionOnField(List<Obj> objs) {
            this.objs = new AbstractPaginatedData<Obj>(false, true, objs) {
                @Override
                public String getCursor(Obj entity) {
                    return entity.id;
                }
            };
        }
    }

    @Test(expectedExceptions = GraphQLConnectionException.class)
    public void fieldList() {
        GraphQLObjectType object = this.graphQLAnnotations.object(TestConnectionOnField.class);
        GraphQLSchema schema = newSchema().query(object).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute(
            GraphQLHelper.createExecutionInput( "{ objs(first: 1) { edges { cursor node { id, val } } } }",
                                                new TestListField(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world")))));
    }

    @Test
    public void methodList() {
        GraphQLSchema schema = newAnnotationsSchema().query(TestConnections.class).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute(GraphQLHelper.createExecutionInput("{ getObjs(first: 1) { edges { cursor node { id, val } } } }",
                new TestConnections(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world")))));

        assertTrue(result.getErrors().isEmpty());

        testResult("getObjs", result);

    }

    @Test
    public void customRelayMethodList() {
        try {
            this.graphQLAnnotations.setRelay(new CustomRelay());
            GraphQLObjectType object = this.graphQLAnnotations.object(TestConnections.class);
            GraphQLSchema schema = newSchema().query(object).build();

            graphql.schema.GraphQLObjectType f = (GraphQLObjectType) schema.getType("ObjConnection");
            assertTrue(f.getFieldDefinitions().size() == 4);
            assertTrue(f.getFieldDefinition("nodes").getType() instanceof GraphQLList);
            assertEquals(((GraphQLNamedType) ((GraphQLList) f.getFieldDefinition("nodes").getType()).getWrappedType()).getName(), "Obj");

            GraphQLObjectType pageInfo = (GraphQLObjectType) schema.getType("PageInfo");
            assertTrue(pageInfo.getFieldDefinition("additionalInfo") != null);
        } finally {
            this.graphQLAnnotations.setRelay(new Relay());
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
        GraphQLSchema schema = newAnnotationsSchema().query(TestConnections.class).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute(GraphQLHelper.createExecutionInput("{ getObjStream(first: 1) { edges { cursor node { id, val } } } }",
                new TestConnections(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world")))));

        assertTrue(result.getErrors().isEmpty());

        testResult("getObjStream", result);
    }

    @Test
    public void methodNonNull() {
        GraphQLSchema schema = newAnnotationsSchema().query(TestConnections.class).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute(GraphQLHelper.createExecutionInput("{ getNonNullObjs(first: 1) { edges { cursor node { id, val } } } }",
                new TestConnections(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world")))));

        assertTrue(result.getErrors().isEmpty());

        testResult("getNonNullObjs", result);
    }

    @Test
    public void methodDoubleNonNull() {
        GraphQLSchema schema = newAnnotationsSchema().query(TestConnections.class).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute(GraphQLHelper.createExecutionInput("{ getDoubleNonNullObjs(first: 1) { edges { cursor node { id, val } } } }",
            new TestConnections(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world")))));

        assertTrue(result.getErrors().isEmpty());

        testResult("getDoubleNonNullObjs", result);
    }

    @Test
    public void methodNull() {
        GraphQLSchema schema = newAnnotationsSchema().query(TestConnections.class).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute(GraphQLHelper.createExecutionInput("{ getNullObj(first: 1) { edges { cursor node { id, val } } } }",
                new TestConnections(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world")))));

        assertTrue(result.getErrors().isEmpty());

        Map<String, Map<String, List<Map<String, Map<String, Object>>>>> data = result.getData();

        assertEquals(data.get("getNullObj").get("edges").size(), 0);
    }

    @Test
    public void emptyListData() {
        GraphQLSchema schema = newAnnotationsSchema().query(TestConnections.class).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute(GraphQLHelper.createExecutionInput("{ getObjStreamWithParam(first: 1, filter:\"hel\") { edges { cursor node { id, val } } } }",
                new TestConnections(emptyList())));
        assertTrue(result.getErrors().isEmpty());

        Map<String, Map<String, List<Map<String, Map<String, Object>>>>> data = result.getData();
        List<Map<String, Map<String, Object>>> edges = data.get("getObjStreamWithParam").get("edges");

        assertEquals(edges.size(), 0);
    }

    @Test
    public void methodListWithParam() {
        GraphQLSchema schema = newAnnotationsSchema().query(TestConnections.class).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute(GraphQLHelper.createExecutionInput("{ getObjStreamWithParam(first: 2, filter:\"hel\") { edges { cursor node { id, val } } } }",
                new TestConnections(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world"), new Obj("4", "hello world"), new Obj("5", "hello again")))));

        assertTrue(result.getErrors().isEmpty());

        Map<String, Map<String, List<Map<String, Map<String, Object>>>>> data = result.getData();
        List<Map<String, Map<String, Object>>> edges = data.get("getObjStreamWithParam").get("edges");

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
        @GraphQLConnection(connectionFetcher = CustomConnection.class)
        public PaginatedData<Obj> getObjs() {
            return new AbstractPaginatedData<Obj>(true, false, objs) {
                @Override
                public String getCursor(Obj entity) {
                    return entity.id;
                }
            };
        }
    }

    @Test
    public void customConnection() {
        GraphQLSchema schema = newAnnotationsSchema().query(TestCustomConnection.class).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute(GraphQLHelper.createExecutionInput("{ getObjs(first: 1) { edges { cursor node { id, val } } } }",
                new TestCustomConnection(Arrays.asList(new Obj("1", "test"), new Obj("2", "hello"), new Obj("3", "world")))));

        assertTrue(result.getErrors().isEmpty());
        Map<String, Object> data = result.getData();
        Map<String, Object> objs = (Map<String, Object>) (data.get("getObjs"));
        List edges = (List) objs.get("edges");
        assertEquals(edges.size(), 0);
    }

    @Test
    public void duplicateConnection() {
        try {
            newAnnotationsSchema().query(DuplicateTest.class).build();
        } catch (GraphQLAnnotationsException e) {
            fail("Schema cannot be created", e);
        }
    }

    public static class TestConnections {

        private List<Obj> objs;

        public TestConnections(List<Obj> objs) {
            this.objs = objs;
        }

        @GraphQLField
        @GraphQLConnection
        public PaginatedData<Obj> getObjs(DataFetchingEnvironment environment) {
            Integer first = environment.getArgument("first");
            List<Obj> actualobjs = new ArrayList<>(objs);

            if (first != null && first < objs.size()) {
                actualobjs = actualobjs.subList(0, first);
            }
            return new AbstractPaginatedData<Obj>(false, true, actualobjs) {
                @Override
                public String getCursor(Obj entity) {
                    return entity.id;
                }
            };
        }

        @GraphQLField
        @GraphQLConnection(name = "objStream")
        public PaginatedData<Obj> getObjStream(DataFetchingEnvironment environment) {
            Integer first = environment.getArgument("first");
            List<Obj> actualobjs = new ArrayList<>(objs);

            if (first != null && first < objs.size()) {
                actualobjs = actualobjs.subList(0, first);
            }

            Obj[] a = new Obj[actualobjs.size()];
            Iterable<Obj> data = Stream.of(actualobjs.toArray(a))::iterator;
            return new AbstractPaginatedData<Obj>(false, true, data) {
                @Override
                public String getCursor(Obj entity) {
                    return entity.id;
                }
            };
        }

        @GraphQLField
        @GraphQLConnection(name = "objStreamWithParam")
        public PaginatedData<Obj> getObjStreamWithParam(DataFetchingEnvironment environment, @GraphQLName("filter") String filter) {
            Integer first = environment.getArgument("first");
            List<Obj> actualobjs = new ArrayList<>(objs);
            List<Obj> filteredObjs = actualobjs.stream().filter(obj -> obj.val.startsWith(filter)).collect(Collectors.toList());
            if (first != null && first < filteredObjs.size()) {
                filteredObjs = filteredObjs.subList(0, first);
            }
            Iterable<Obj> objIterable = filteredObjs::iterator;
            return new AbstractPaginatedData<Obj>(false, true, objIterable) {
                @Override
                public String getCursor(Obj entity) {
                    return entity.id;
                }
            };
        }

        @GraphQLField
        @GraphQLConnection(name = "nonNullObjs")
        @GraphQLNonNull
        public PaginatedData<Obj> getNonNullObjs(DataFetchingEnvironment environment) {
            Integer first = environment.getArgument("first");
            List<Obj> actualobjs = new ArrayList<>(objs);

            if (first != null && first < objs.size()) {
                actualobjs = actualobjs.subList(0, first);
            }
            return new AbstractPaginatedData<Obj>(false, true, actualobjs) {
                @Override
                public String getCursor(Obj entity) {
                    return entity.id;
                }
            };
        }

        @GraphQLField
        @GraphQLConnection(name = "doubleNonNullObjs")
        @graphql.annotations.annotationTypes.GraphQLType(DoubleNonNullIterableTypeFunction.class)
        public PaginatedData<Obj> getDoubleNonNullObjs(DataFetchingEnvironment environment) {
          return getNonNullObjs(environment);
        }

        @GraphQLField
        @GraphQLConnection(name = "nullObj")
        public PaginatedData<Obj> getNullObj() {
            return null;
        }


    }

    /*
     * double @GraphQLNonNull on @GraphQLList like [ObjectType!]!
     */
    public static class DoubleNonNullIterableTypeFunction implements TypeFunction {

        @Override
        public boolean canBuildType(Class<?> theClass, AnnotatedType annotatedType) {
            return Iterable.class.isAssignableFrom(theClass);
        }

        @Override
        public GraphQLType buildType(
            boolean input,
            Class<?> theClass,
            AnnotatedType annotatedType,
            ProcessingElementsContainer container) {

            if (!(annotatedType instanceof AnnotatedParameterizedType)) {
                throw new IllegalArgumentException("List type parameter should be specified");
            }
            AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) annotatedType;
            AnnotatedType arg = parameterizedType.getAnnotatedActualTypeArguments()[0];
            Class<?> klass;
            if (arg.getType() instanceof ParameterizedType) {
                klass = (Class<?>) ((ParameterizedType) (arg.getType())).getRawType();
            } else {
                klass = (Class<?>) arg.getType();
            }

            TypeFunction typeFunction = container.getDefaultTypeFunction();
            GraphQLType type = typeFunction.buildType(input, klass, arg, container);

            // double @GraphQLNonNull on @GraphQLList like [ObjectType!]!
            return nonNull(new GraphQLList(nonNull(type)));
        }
    }


}
