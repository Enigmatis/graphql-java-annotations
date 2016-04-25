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
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.*;

public class RelayTest {

    public static class Result {
        private final int i;

        public Result(int i) {
            this.i = i;
        }

        @GraphQLField
        public int getI() {
            return i;
        }
    }
    public static class WrongReturnType {
        @GraphQLField @GraphQLRelayMutation
        public int doSomething() {
            return 0;
        }
    }

    public static class TestObject {
        @GraphQLField @GraphQLRelayMutation
        public Result doSomething() {
            return new Result(0);
        }
        @GraphQLField @GraphQLRelayMutation
        public Result doSomethingElse(int a, int b) {
            return new Result(a - b);
        }
    }

    @Test(expectedExceptions = RuntimeException.class) @SneakyThrows
    public void notAnObjectType() {
        GraphQLObjectType object = GraphQLAnnotations.object(WrongReturnType.class);
    }

    @Test @SneakyThrows
    public void noArgMutation() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestObject.class);

        GraphQLFieldDefinition doSomething = object.getFieldDefinition("doSomething");

        assertNotNull(doSomething);

        assertEquals(doSomething.getArguments().size(), 1);
        GraphQLInputType input = doSomething.getArgument("input").getType();
        assertTrue(input instanceof GraphQLNonNull);
        GraphQLType inputType = ((graphql.schema.GraphQLNonNull) input).getWrappedType();
        assertTrue(inputType instanceof GraphQLInputObjectType);

        assertTrue(doSomething.getType() instanceof GraphQLObjectType);
        GraphQLObjectType returnType = (GraphQLObjectType) doSomething.getType();

        assertNotNull(returnType.getFieldDefinition("i"));
        assertNotNull(returnType.getFieldDefinition("clientMutationId"));

        GraphQLSchema schema = GraphQLSchema.newSchema().query(object).mutation(object).build();

        GraphQL graphQL = new GraphQL(schema, new EnhancedExecutionStrategy());

        ExecutionResult result = graphQL.execute("mutation { doSomething(input: {clientMutationId: \"1\"}) { i clientMutationId } }", new TestObject());

        assertEquals(result.getErrors().size(), 0);

        Map<String, Object> returns = (Map<String, Object>) ((Map<String, Object>) result.getData()).get("doSomething");

        assertEquals(returns.get("i"), 0);
        assertEquals(returns.get("clientMutationId"), "1");
    }

    @Test @SneakyThrows
    public void argMutation() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestObject.class);

        GraphQLFieldDefinition doSomethingElse = object.getFieldDefinition("doSomethingElse");

        assertNotNull(doSomethingElse);

        assertEquals(doSomethingElse.getArguments().size(), 1);
        GraphQLInputType input = doSomethingElse.getArgument("input").getType();
        assertTrue(input instanceof GraphQLNonNull);
        GraphQLType inputType = ((graphql.schema.GraphQLNonNull) input).getWrappedType();
        assertTrue(inputType instanceof GraphQLInputObjectType);
        GraphQLInputObjectType inputType_ = (GraphQLInputObjectType) inputType;
        assertNotNull(inputType_.getField("a"));
        assertNotNull(inputType_.getField("b"));

        assertTrue(doSomethingElse.getType() instanceof GraphQLObjectType);
        GraphQLObjectType returnType = (GraphQLObjectType) doSomethingElse.getType();

        assertNotNull(returnType.getFieldDefinition("i"));
        assertNotNull(returnType.getFieldDefinition("clientMutationId"));

        GraphQLSchema schema = GraphQLSchema.newSchema().query(object).mutation(object).build();

        GraphQL graphQL = new GraphQL(schema, new EnhancedExecutionStrategy());

        ExecutionResult result = graphQL.execute("mutation { doSomethingElse(input: {a: 0, b: 1, clientMutationId: \"1\"}) { i clientMutationId } }", new TestObject());

        assertEquals(result.getErrors().size(), 0);

        Map<String, Object> returns = (Map<String, Object>) ((Map<String, Object>) result.getData()).get("doSomethingElse");

        assertEquals(returns.get("i"), -1);
        assertEquals(returns.get("clientMutationId"), "1");
    }
}
