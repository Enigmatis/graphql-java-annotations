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
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLInvokeDetached;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.annotations.processor.retrievers.GraphQLObjectHandler;
import graphql.schema.GraphQLObjectType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertEquals;

public class GraphQLSimpleSchemaTest {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }


    public static class User {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @GraphQLField
        public String name() {
            return this.getName();
        }
    }


    public static class Query {
        @GraphQLField
        public static User defaultUser() {
            User user = new User();
            user.setName("Test Name");
            return user;
        }

        @GraphQLField
        @GraphQLInvokeDetached
        public User defaultUser2() {
            User user = new User();
            user.setName("Test Name");
            return user;
        }
    }

    @Test
    public void detachedCall() {
        GraphQLAnnotations graphQLAnnotations = new GraphQLAnnotations();
        GraphQLObjectHandler graphQLObjectHandler = graphQLAnnotations.getObjectHandler();
        GraphQLObjectType queryObject = graphQLObjectHandler.getObject(Query.class,graphQLAnnotations.getContainer());
        GraphQL graphql = GraphQL.newGraphQL(newSchema().query(queryObject).build()).build();

        ExecutionResult result = graphql.execute("{ defaultUser{ name } }");
        String actual = result.getData().toString();
        assertEquals(actual, "{defaultUser={name=Test Name}}");
    }

    @Test
    public void staticCall() {
        GraphQLAnnotations graphQLAnnotations = new GraphQLAnnotations();
        GraphQLObjectHandler graphQLObjectHandler = graphQLAnnotations.getObjectHandler();
        GraphQLObjectType queryObject = graphQLObjectHandler.getObject(Query.class,graphQLAnnotations.getContainer());
        GraphQL graphql = GraphQL.newGraphQL(newSchema().query(queryObject).build()).build();

        ExecutionResult result = graphql.execute("{ defaultUser2{ name } }");
        String actual = result.getData().toString();
        assertEquals(actual, "{defaultUser2={name=Test Name}}");
    }
}

