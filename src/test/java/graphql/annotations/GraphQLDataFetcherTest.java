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
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.PropertyDataFetcher;
import org.testng.annotations.Test;

import java.util.HashMap;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class GraphQLDataFetcherTest {

    @Test
    public void shouldUsePreferredConstructor() {
        // Given
        final GraphQLObjectType object = GraphQLAnnotations.object(GraphQLDataFetcherTest.TestGraphQLQuery.class);
        final GraphQLSchema schema = newSchema().query(object).build();
        GraphQL graphql = GraphQL.newGraphQL(schema).build();

        // When
        final ExecutionResult result = graphql.execute("{sample {isGreat isBad}}");

        // Then
        final HashMap<String, Object> data = (HashMap) result.getData();
        assertNotNull(data);
        assertTrue(((HashMap<String, Boolean>) data.get("sample")).get("isGreat"));
        assertTrue(((HashMap<String, Boolean>) data.get("sample")).get("isBad"));
    }

    @GraphQLName("Query")
    public static class TestGraphQLQuery {
        @GraphQLField
        @GraphQLDataFetcher(SampleDataFetcher.class)
        public TestSample sample() { // Note that GraphQL uses TestSample to build the graph
            return null;
        }
    }

    public static class TestSample {
        @GraphQLField
        @GraphQLDataFetcher(value = PropertyDataFetcher.class, args = "isGreat")
        private Boolean isGreat = false; // Defaults to FieldDataFetcher

        @GraphQLField
        @GraphQLDataFetcher(value = SampleMultiArgDataFetcher.class, firstArgIsTargetName = true, args = {"true"})
        private Boolean isBad = false; // Defaults to FieldDataFetcher

    }

    public static class SampleDataFetcher implements DataFetcher {
        @Override
        public Object get(final DataFetchingEnvironment environment) {
            return new Sample(); // Notice that it return a Sample, not a TestSample
        }
    }

    public static class SampleMultiArgDataFetcher extends PropertyDataFetcher {
        private boolean flip = false;

        public SampleMultiArgDataFetcher(String target, String flip) {
            super(target);
            this.flip = Boolean.valueOf(flip);
        }

        @Override
        public Object get(DataFetchingEnvironment environment) {
            final Object result = super.get(environment);
            if (flip) {
                return !(Boolean) result;
            } else {
                return result;
            }
        }
    }

    public static class Sample {
        private Boolean isGreat = true;
        private Boolean isBad = false;

        public Boolean getIsGreat() {
            return isGreat;
        }

        public void setIsGreat(final Boolean isGreat) {
            this.isGreat = isGreat;
        }

        public Boolean getIsBad() {
            return isBad;
        }

        public void setIsBad(Boolean bad) {
            isBad = bad;
        }
    }
}
