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
import graphql.schema.GraphQLObjectType;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertEquals;

/**
 * Created by ngoel on 5/31/16.
 */
public class GraphQLSimpleSchemaTest {

    public static class User {

        @Getter
        @Setter
        private String name;

        @GraphQLField
        public String name() {
            return this.getName();
        }
    }


    public static class Query {
        @GraphQLInvokeDetached
        @GraphQLField
        public User defaultUser() {
            User user = new User();
            user.setName("Test Name");
            return user;
        }
    }

    @Test @SneakyThrows
    public void detachedCall() {
        GraphQLObjectType queryObject = GraphQLAnnotations.object(Query.class);
        GraphQL graphql = new GraphQL(newSchema().query(queryObject).build());

        ExecutionResult result = graphql.execute("{ defaultUser{ name } }");
        String actual = result.getData().toString();
        assertEquals(actual, "{defaultUser={name=Test Name}}");
    }
}

