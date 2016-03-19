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

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.*;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GraphQLInterfaceTest {

    public static class Resolver implements TypeResolver {

        @Override
        public GraphQLObjectType getType(Object object) {
            try {
                return GraphQLAnnotations.object(TestObject.class);
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException e) {
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

    static class TestObject implements TestIface {

        @Override
        public String value() {
            return "a";
        }
    }

    @Test @SneakyThrows
    public void test() {
        GraphQLInterfaceType iface = GraphQLAnnotations.iface(TestIface.class);
        List<GraphQLFieldDefinition> fields = iface.getFieldDefinitions();
        assertEquals(fields.size(), 1);
        assertEquals(fields.get(0).getName(), "value");
    }

    @Test @SneakyThrows
    public void testInterfaces() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestObject.class);
        List<GraphQLInterfaceType> ifaces = object.getInterfaces();
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
        @GraphQLField public TestIface iface;
    }

    @Test @SneakyThrows
    public void query() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(Query.class)).build();

        ExecutionResult result = new GraphQL(schema).execute("{ iface { value } }");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>)result.getData()).get("iface").get("value"), "a");
    }

}
