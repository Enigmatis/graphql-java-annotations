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
package graphql.annotations;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.TypeResolutionEnvironment;
import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLTypeResolver;
import graphql.annotations.annotationTypes.GraphQLUnion;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.retrievers.GraphQLInterfaceRetriever;
import graphql.schema.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class GraphQLInterfaceTest {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    @Test
    public void noResolver() {
        GraphQLInterfaceRetriever graphQLInterfaceRetriever = GraphQLAnnotations.getInstance().getObjectHandler().getTypeRetriever().getGraphQLInterfaceRetriever();

        GraphQLObjectType object = (GraphQLObjectType) graphQLInterfaceRetriever.getInterface(NoResolverIface.class, GraphQLAnnotations.getInstance().getContainer());
        List<GraphQLFieldDefinition> fields = object.getFieldDefinitions();
        assertEquals(fields.size(), 1);
        assertEquals(fields.get(0).getName(), "value");
    }

    @Test
    public void test() {
        GraphQLInterfaceRetriever graphQLInterfaceRetriever = GraphQLAnnotations.getInstance().getObjectHandler().getTypeRetriever().getGraphQLInterfaceRetriever();
        GraphQLInterfaceType iface = (GraphQLInterfaceType) graphQLInterfaceRetriever.getInterface(TestIface.class, GraphQLAnnotations.getInstance().getContainer());
        List<GraphQLFieldDefinition> fields = iface.getFieldDefinitions();
        assertEquals(fields.size(), 1);
        assertEquals(fields.get(0).getName(), "value");
    }

    @Test
    public void testUnion() {
        GraphQLInterfaceRetriever graphQLInterfaceRetriever = GraphQLAnnotations.getInstance().getObjectHandler().getTypeRetriever().getGraphQLInterfaceRetriever();
        GraphQLUnionType unionType = (GraphQLUnionType) graphQLInterfaceRetriever.getInterface(TestUnion.class, GraphQLAnnotations.getInstance().getContainer());
        assertEquals(unionType.getTypes().size(), 1);
        assertEquals(unionType.getTypes().get(0).getName(), "TestObject1");
    }

    @Test
    public void testInterfaces() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestObject.class);
        List<GraphQLOutputType> ifaces = object.getInterfaces();
        assertEquals(ifaces.size(), 1);
        assertEquals(ifaces.get(0).getName(), "TestIface");
    }

    @Test
    public void query() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(Query.class)).build();
        schema = schema.transform(builder -> builder.additionalTypes(GraphQLAnnotations.additionalTypes(Query.class)));

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ iface { value } }");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("iface").get("value"), "a");
    }

    @Test
    public void getAdditionalTypes_thereAreObjectsThatOnlyImplementButNotExplicitlySpecified_additionalTypesAreNotEmpty() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(Query.class)).build();
        schema = schema.transform(builder -> builder.additionalTypes(GraphQLAnnotations.additionalTypes(Query.class)));

        assertTrue(!schema.getAdditionalTypes().isEmpty());
    }

    @Test
    public void queryForObject_objectImplementsInterfaceButNotExplicitInTheSchema_worksAsExpected() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(Query.class)).build();
        schema = schema.transform(builder -> builder.additionalTypes(GraphQLAnnotations.additionalTypes(Query.class)));

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ anotherIface{ ... on TestObject2 {value}}}");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("anotherIface").get("value"), "b");
    }

    @Test
    public void queryUnion() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(UnionQuery.class)).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ union {  ... on TestObject1 { value }  } }", new UnionQuery(new TestObject1()));
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("union").get("value"), "a");
    }


    @GraphQLTypeResolver(Resolver.class)
    public interface BaseTestIface {

        @GraphQLField
        String value();
    }

    @GraphQLTypeResolver(Resolver.class)
    public interface TestIface extends BaseTestIface {

    }

    @GraphQLUnion(possibleTypes = TestObject1.class)
    public interface TestUnion extends BaseTestIface {

    }

    interface NoResolverIface {
        @GraphQLField
        String value();
    }

    public static class Resolver implements TypeResolver {

        public static Resolver getInstance() {
            return new Resolver();
        }

        @Override
        public GraphQLObjectType getType(TypeResolutionEnvironment env) {
            try {
                Object object = env.getObject();
                if (object instanceof TestObject2) {
                    return env.getSchema().getObjectType("TestObject2");
                }
                if (object instanceof TestObject1) {
                    return env.getSchema().getObjectType("TestObject1");
                }
                if (object instanceof TestObject) {
                    return env.getSchema().getObjectType("TestObject");
                }

                return env.getSchema().getObjectType("TestIface");


            } catch (GraphQLAnnotationsException e) {
                return null;
            }
        }

    }

    public static class TestObject implements TestIface {

        @Override
        public String value() {
            return "a";
        }

    }

    public static class TestObject1 implements TestUnion {

        @GraphQLField
        public int i = 1;

        @Override
        public String value() {
            return "a";
        }

    }

    public static class TestObject2 implements TestIface {

        @Override
        public String value() {
            return "b";
        }

    }

    public static class IfaceFetcher implements DataFetcher {


        @Override
        public Object get(DataFetchingEnvironment environment) {
            return new TestObject();
        }
    }

    public static class Query {

        @GraphQLDataFetcher(IfaceFetcher.class)
        @GraphQLField
        public TestIface iface;

        @GraphQLDataFetcher(AnotherIfaceFetcher.class)
        @GraphQLField
        public TestIface anotherIface;
    }

    public static class UnionQuery {

        @GraphQLField
        public TestUnion union;

        public UnionQuery(TestUnion union) {
            this.union = union;
        }
    }

    public static class AnotherIfaceFetcher implements DataFetcher {
        @Override
        public Object get(DataFetchingEnvironment environment) {
            return new TestObject2();
        }
    }
}
