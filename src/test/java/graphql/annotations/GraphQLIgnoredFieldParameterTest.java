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

import static graphql.annotations.AnnotationsSchemaCreator.newAnnotationsSchema;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLIgnore;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GraphQLIgnoredFieldParameterTest {
    private AnnotationsSchemaCreator.Builder builder;
    private GraphQLAnnotations graphQLAnnotations;

    @BeforeMethod
    public void setUp() {
        graphQLAnnotations = new GraphQLAnnotations();
        builder = newAnnotationsSchema().setAnnotationsProcessor(graphQLAnnotations);
    }

    public static class QueryTest1 {
        @GraphQLField
        public int foo(@GraphQLIgnore int bar) {
            return 5;
        }
    }

    @Test
    public void build_Query1IsProvided_SchemaIsCreatedWithoutIgnoredFieldParameters() {
        // arrange + act
        GraphQLSchema schema = builder.query(QueryTest1.class).build();

        // assert
        GraphQLObjectType queryType = schema.getQueryType();
        assertThat(queryType.getFieldDefinition("foo"), notNullValue());
        assertThat(queryType.getFieldDefinition("foo").getArguments().size(), is(0));
    }

    public static class QueryTest2 {
        @GraphQLField
        public int foo(int bar, @GraphQLIgnore String baz, @GraphQLName("quuxAlias") long quux) {
            return 5;
        }
    }

    @Test
    public void build_Query2IsProvided_SchemaIsCreatedWithoutIgnoredFieldParameters() {
        // arrange + act
        GraphQLSchema schema = builder.query(QueryTest2.class).build();

        // assert
        GraphQLObjectType queryType = schema.getQueryType();
        assertThat(queryType.getFieldDefinition("foo"), notNullValue());
        assertThat(queryType.getFieldDefinition("foo").getArguments().size(), is(2));
        assertThat(queryType.getFieldDefinition("foo").getArgument("arg0"), notNullValue());
        assertThat(queryType.getFieldDefinition("foo").getArgument("baz"), nullValue());
        assertThat(queryType.getFieldDefinition("foo").getArgument("quuxAlias"), notNullValue());
    }

    public static class MutationTest1 {
        @GraphQLField
        public int foo(@GraphQLIgnore int bar) {
            return 4;
        }
    }

    @Test
    public void build_Mutation1IsProvided_SchemaIsCreatedWithoutIgnoredFieldParameters() {
        // arrange + act
        GraphQLSchema schema = builder.query(QueryTest1.class).mutation(MutationTest1.class).build();

        /// assert
        GraphQLObjectType mutation = schema.getMutationType();
        assertThat(mutation.getFieldDefinition("foo"), notNullValue());
        assertThat(mutation.getFieldDefinition("foo").getArguments().size(), is(0));
    }

    public static class MutationTest2 {
        @GraphQLField
        public int foo(int bar, @GraphQLIgnore String baz, @GraphQLName("quuxAlias") long quux) {
            return 4;
        }
    }

    @Test
    public void build_Mutation2IsProvided_SchemaIsCreatedWithoutIgnoredFieldParameters() {
        // arrange + act
        GraphQLSchema schema = builder.query(QueryTest1.class).mutation(MutationTest2.class).build();

        /// assert
        GraphQLObjectType mutation = schema.getMutationType();
        assertThat(mutation.getFieldDefinition("foo"), notNullValue());
        assertThat(mutation.getFieldDefinition("foo").getArguments().size(), is(2));
        assertThat(mutation.getFieldDefinition("foo").getArgument("arg0"), notNullValue());
        assertThat(mutation.getFieldDefinition("foo").getArgument("baz"), nullValue());
        assertThat(mutation.getFieldDefinition("foo").getArgument("quuxAlias"), notNullValue());
    }
}
