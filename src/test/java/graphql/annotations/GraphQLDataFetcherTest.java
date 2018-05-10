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
import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.schema.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.*;

public class GraphQLDataFetcherTest {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    @Test
    public void shouldUsePreferredConstructor() {
        // Given
        final GraphQLObjectType object = GraphQLAnnotations.object(GraphQLDataFetcherTest.TestGraphQLQuery.class);
        final GraphQLSchema schema = newSchema().query(object).build();
        GraphQL graphql = GraphQL.newGraphQL(schema).build();

        // When
        final ExecutionResult result = graphql.execute("{sample {isGreat isBad}}");

        // Then
        final HashMap<String, Object> data = result.getData();
        assertNotNull(data);
        assertTrue(((HashMap<String, Boolean>) data.get("sample")).get("isGreat"));
        assertTrue(((HashMap<String, Boolean>) data.get("sample")).get("isBad"));
    }

    @Test
    public void shouldUseProvidedSoloArgumentForDataFetcherDeclaredInMethod() {
        // Given
        final GraphQLObjectType object = GraphQLAnnotations.object(TestMethodWithDataFetcherGraphQLQuery.class);
        final GraphQLSchema schema = newSchema().query(object).build();
        final GraphQL graphql = GraphQL.newGraphQL(schema).build();

        // When
        final ExecutionResult result = graphql.execute("{great}");

        // Then
        final HashMap<String, Object> data = result.getData();
        assertNotNull(data);
        assertFalse((Boolean)data.get("great"));
    }

    @Test
    public void shouldUseTargetAndArgumentsForDataFetcherDeclaredInMethod() {
        // Given
        final GraphQLObjectType object = GraphQLAnnotations.object(TestMethodWithDataFetcherGraphQLQuery.class);
        final GraphQLSchema schema = newSchema().query(object).build();
        final GraphQL graphql = GraphQL.newGraphQL(schema).build();

        // When
        final ExecutionResult result = graphql.execute("{sample {isBad}}");

        // Then
        final HashMap<String, Object> data = result.getData();
        assertNotNull(data);
        assertTrue(((HashMap<String,Boolean>)data.get("sample")).get("isBad"));
    }

    @GraphQLName("Query")
    public static class TestGraphQLQuery {
        @GraphQLField
        @GraphQLDataFetcher(SampleDataFetcher.class)
        public TestSample sample() { // Note that GraphQL uses TestSample to build the graph
          return null;
        }
    }

    @GraphQLName("Query")
    public static class TestMethodWithDataFetcherGraphQLQuery {
        @GraphQLField
        @GraphQLDataFetcher(value = SampleOneArgDataFetcher.class, args = "true")
        public Boolean great() { return false; }

        @GraphQLField
        @GraphQLDataFetcher(SampleDataFetcher.class)
        public TestSampleMethod sample() { return null; }
    }

    public static class TestSample {
        @GraphQLField
        @GraphQLDataFetcher(value = PropertyDataFetcher.class, args = "isGreat")
        private Boolean isGreat = false; // Defaults to FieldDataFetcher

        @GraphQLField
        @GraphQLDataFetcher(value = SampleMultiArgDataFetcher.class, firstArgIsTargetName = true, args = {"true"})
        private Boolean isBad = false; // Defaults to FieldDataFetcher

    }

    public static class TestSampleMethod {

        @GraphQLField
        @GraphQLDataFetcher(value = SampleMultiArgDataFetcher.class, firstArgIsTargetName = true, args = {"true"})
        public Boolean isBad() { return false; } // Defaults to FieldDataFetcher

    }

    public static class SampleDataFetcher implements DataFetcher {
        @Override
        public Object get(final DataFetchingEnvironment environment) {
            return new Sample(); // Notice that it return a Sample, not a TestSample
        }
    }

    public static class SampleOneArgDataFetcher implements DataFetcher {
        private boolean flip = false;

        public SampleOneArgDataFetcher(String flip) {
          this.flip = Boolean.valueOf(flip);
        }

        @Override
        public Object get(DataFetchingEnvironment environment) {
            if ( flip ) {
                return !flip;
            } else {
                return flip;
            }
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
                return !(Boolean)result;
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
