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
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.schema.GraphQLObjectType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertEquals;

public class GraphQLEnumTest {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    public enum Foo {
        ONE,
        TWO
    }

    public static class User {

        private Foo foo;

        public User(Foo foo) {
            this.foo = foo;
        }

        @GraphQLField
        public Foo getName() {
            return foo;
        }
    }


    public static class Query {
        @GraphQLField
        public static User defaultUser() {
            User user = new User(Foo.ONE);
            return user;
        }

        @GraphQLField
        public static User user(@GraphQLName("param") Foo param) {
            User user = new User(param);
            return user;
        }
    }

    @Test
    public void test() throws IllegalAccessException, NoSuchMethodException, InstantiationException {
        GraphQLObjectType queryObject = GraphQLAnnotations.object(Query.class);
        GraphQL graphql = GraphQL.newGraphQL(newSchema().query(queryObject).build()).build();

        ExecutionResult result = graphql.execute("{ defaultUser{ getName } }");
        assertEquals(result.getData().toString(), "{defaultUser={getName=ONE}}");
    }

    @Test
    public void testAsInput() throws IllegalAccessException, NoSuchMethodException, InstantiationException {
        GraphQLObjectType queryObject = GraphQLAnnotations.object(Query.class);
        GraphQL graphql = GraphQL.newGraphQL(newSchema().query(queryObject).build()).build();

        ExecutionResult result = graphql.execute("{ user(param:TWO){ getName } }");
        assertEquals(result.getData().toString(), "{user={getName=TWO}}");
    }


}
