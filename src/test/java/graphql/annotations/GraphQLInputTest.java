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
import graphql.schema.*;
import org.testng.annotations.Test;

import java.util.*;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class GraphQLInputTest {

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

    static class InputObject {
        public InputObject(HashMap map) {
            key = (String) map.get("key");
        }

        @GraphQLField
        private String key;
    }

    static class RecursiveInputObject {
        public RecursiveInputObject(HashMap map) {
            key = (String) map.get("key");
            if (map.containsKey("rec")) {
                rec = new RecursiveInputObject((HashMap) map.get("rec"));
            }
        }

        @GraphQLField
        private String key;

        @GraphQLField
        private RecursiveInputObject rec;
    }

    @GraphQLTypeResolver(Resolver.class)
    interface TestIface {
        @GraphQLField
        String value(InputObject input);
    }

    static class TestObject implements TestIface {

        @Override
        public String value(InputObject input) {
            return input.key + "a";
        }
    }

    static class TestObjectRec {
        @GraphQLField
        public String value(RecursiveInputObject input) {
            return (input.rec != null ? ("rec"+input.rec.key) : input.key) + "a";
        }
    }

    static class Code {
        public Code(HashMap map) {
            this.firstField = (String) map.get("firstField");
            this.secondField = (String) map.get("secondField");
        }

        @GraphQLField
        public String firstField;
        @GraphQLField
        public String secondField;
    }

    static class QueryMultipleDefinitions {
        @GraphQLField
        public String something(Code code) {
            return code.firstField + code.secondField;
        }

        @GraphQLField
        public String somethingElse(Code code) {
            return code.firstField + code.secondField;
        }
    }

    static class Query {
        @GraphQLField
        public TestIface object() {
            return new TestObject();
        };
    }

    static class QueryRecursion {
        @GraphQLField
        public TestObjectRec object() {
            return new TestObjectRec();
        };
    }

    static class QueryIface {
        @GraphQLField
        public TestObject iface() {
            return new TestObject();
        }
    }


    @Test
    public void query() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(Query.class)).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ object { value(input:{key:\"test\"}) } }", new Query());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("object").get("value"), "testa");
    }

    @Test
    public void queryMultipleDefinitions() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(QueryMultipleDefinitions.class)).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ something(code: {firstField:\"a\",secondField:\"b\"}) somethingElse(code: {firstField:\"c\",secondField:\"d\"}) }", new QueryMultipleDefinitions());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result.getData()).get("something"), "ab");
        assertEquals(((Map<String, String>) result.getData()).get("somethingElse"), "cd");
    }

    @Test
    public void queryWithRecursion() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(QueryRecursion.class)).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ object { value(input:{key:\"test\"}) } }", new QueryRecursion());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("object").get("value"), "testa");

        result = graphQL.execute("{ object { value(input:{rec:{key:\"test\"}}) } }", new QueryRecursion());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("object").get("value"), "rectesta");
    }

    @Test
    public void queryWithInterface() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(QueryIface.class)).build(Collections.singleton(GraphQLAnnotations.object(TestObject.class)));

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ iface { value(input:{key:\"test\"}) } }", new QueryIface());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("iface").get("value"), "testa");
    }


}
