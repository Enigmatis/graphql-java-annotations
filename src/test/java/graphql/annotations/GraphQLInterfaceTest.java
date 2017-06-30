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
import graphql.TypeResolutionEnvironment;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLUnionType;
import graphql.schema.TypeResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@SuppressWarnings("unchecked")
public class GraphQLInterfaceTest {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    interface NoResolverIface {
        @GraphQLField
        String value();
    }

    @Test
    public void noResolver() {
        GraphQLObjectType object = (GraphQLObjectType) GraphQLAnnotations.iface(NoResolverIface.class);
        List<GraphQLFieldDefinition> fields = object.getFieldDefinitions();
        assertEquals(fields.size(), 1);
        assertEquals(fields.get(0).getName(), "value");
    }

    public static class Resolver implements TypeResolver {

        @Override
        public GraphQLObjectType getType(TypeResolutionEnvironment env) {
            try {
                return GraphQLAnnotations.object(TestObject.class);
            } catch (GraphQLAnnotationsException e) {
                return null;
            }
        }
    }

    @GraphQLTypeResolver(Resolver.class)
    interface BaseTestIface {
        @GraphQLField
        String value();
    }

    @GraphQLTypeResolver(Resolver.class)
    interface TestIface extends BaseTestIface {
    }

    @GraphQLUnion(possibleTypes = TestObject1.class)
    interface TestUnion extends BaseTestIface {
    }

    static class TestObject implements TestIface {

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

    @Test
    public void test() {
        GraphQLInterfaceType iface = (GraphQLInterfaceType) GraphQLAnnotations.iface(TestIface.class);
        List<GraphQLFieldDefinition> fields = iface.getFieldDefinitions();
        assertEquals(fields.size(), 1);
        assertEquals(fields.get(0).getName(), "value");
    }

    @Test
    public void testUnion() {
        GraphQLUnionType unionType = (GraphQLUnionType) GraphQLAnnotations.iface(TestUnion.class);
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

    public static class IfaceFetcher implements DataFetcher {

        @Override
        public Object get(DataFetchingEnvironment environment) {
            return new TestObject();
        }
    }

    static class Query {
        @GraphQLDataFetcher(IfaceFetcher.class)
        @GraphQLField
        public TestIface iface;
    }

    public static class UnionQuery {
        @GraphQLField
        public TestUnion union;

        public UnionQuery(TestUnion union) {
            this.union = union;
        }
    }

    @Test
    public void query() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(Query.class)).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ iface { value } }");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("iface").get("value"), "a");
    }

    @Test
    public void queryUnion() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(UnionQuery.class)).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ union {  ... on TestObject1 { value }  } }", new UnionQuery(new TestObject1()));
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("union").get("value"), "a");
    }

}
