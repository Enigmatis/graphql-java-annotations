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

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.directives.definition.GraphQLDirectiveDefinition;
import graphql.annotations.directives.AnnotationsDirectiveWiring;
import graphql.annotations.directives.AnnotationsWiringEnvironment;
import graphql.annotations.annotationTypes.directives.definition.DirectiveLocations;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.introspection.Introspection;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static graphql.annotations.AnnotationsSchemaCreator.newAnnotationsSchema;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AnnotationsSchemaCreatorTest {
    private AnnotationsSchemaCreator.Builder builder;
    private GraphQLAnnotations graphQLAnnotations;

    @BeforeMethod
    public void setUp() {
        graphQLAnnotations = new GraphQLAnnotations();
        builder = newAnnotationsSchema().setAnnotationsProcessor(graphQLAnnotations);
    }

    @Test(expectedExceptions = AssertionError.class)
    public void build_QueryIsNotProvided_ExceptionIsThrown() {
        // act
        builder.build();
    }

    @GraphQLDescription("query obj")
    public static class QueryTest {
        @GraphQLField
        public int getNum() {
            return 5;
        }
    }

    @Test
    public void build_QueryIsProvided_SchemaIsCreatedWithQuery() {
        // arrange + act
        GraphQLSchema schema = builder.query(QueryTest.class).build();

        // assert
        GraphQLObjectType queryType = schema.getQueryType();
        assertThat(queryType.getDescription(), is("query obj"));
        assertThat(queryType.getFieldDefinition("getNum"), notNullValue());
        assertThat(queryType.getFieldDefinitions().size(), is(1));
    }

    public static class MutationTest {
        @GraphQLField
        public int mutate() {
            return 4;
        }
    }

    @Test
    public void build_Mutation_SchemaIsCreatedWithMutation() {
        // arrange + act
        GraphQLSchema schema = builder.query(QueryTest.class).mutation(MutationTest.class).build();
        GraphQLObjectType mutationType = schema.getMutationType();

        /// assert
        assertThat(mutationType.getFieldDefinition("mutate"), notNullValue());
        assertThat(mutationType.getFieldDefinitions().size(), is(1));
    }

    public static class SubscriptionTest {
        @GraphQLField
        public int subscribe() {
            return 4;
        }
    }

    @Test
    public void build_Subscription_SchemaIsCreatedWithSubscription() {
        // arrange + act
        GraphQLSchema schema = builder.query(QueryTest.class).subscription(SubscriptionTest.class).build();
        GraphQLObjectType subscriptionType = schema.getSubscriptionType();

        // assert
        assertThat(subscriptionType.getFieldDefinition("subscribe"), notNullValue());
        assertThat(subscriptionType.getFieldDefinitions().size(), is(1));
    }

    public static class GeneralWiring implements AnnotationsDirectiveWiring{
        @Override
        public GraphQLFieldDefinition onField(AnnotationsWiringEnvironment environment) {
            return null;
        }
    }

    @GraphQLName("testDirective")
    @DirectiveLocations({Introspection.DirectiveLocation.FIELD_DEFINITION})
    @GraphQLDirectiveDefinition(wiring = GeneralWiring.class)
    public static class DirectiveDefinitionTest {
        public boolean isActive = true;
    }

    @Test
    public void build_Directive_SchemaIsCreatedWithDirective() {
        // arrange + act
        GraphQLSchema schema = builder.query(QueryTest.class).directive(DirectiveDefinitionTest.class).build();

        // assert
        GraphQLDirective testDirective = schema.getDirective("testDirective");
        assertThat(testDirective, notNullValue());
        assertThat(testDirective.getArguments().size(), is(1));
        assertThat(testDirective.getArgument("isActive"), notNullValue());
    }

    @GraphQLName("secondDirective")
    @DirectiveLocations(Introspection.DirectiveLocation.FIELD)
    @GraphQLDirectiveDefinition(wiring = GeneralWiring.class)
    public static class SecondDirective {

    }

    @Test
    public void build_MultipleDirectives_SchemaIsCreatedWithDirectives() {
        // arrange + act
        Set<Class<?>> directives = new HashSet<>();
        directives.add(DirectiveDefinitionTest.class);
        directives.add(SecondDirective.class);
        GraphQLDirective directiveTest = graphQLAnnotations.directive(DirectiveDefinitionTest.class);
        GraphQLDirective secondDirective = graphQLAnnotations.directive(SecondDirective.class);
        GraphQLSchema schema = builder.query(QueryTest.class).directives(directives).build();

        // assert
        assertThat(schema.getDirective("secondDirective"), notNullValue());
        assertThat(schema.getDirective("testDirective"), notNullValue());
    }

    @GraphQLName("additional")
    public static class AdditionalTypeTest {
        public int getI() {
            return 4;
        }
    }

    @Test
    public void build_AdditionalType_SchemaIsCreatedWithAdditionalType() {
        // arrange + act
        GraphQLSchema schema = builder.query(QueryTest.class).additionalType(AdditionalTypeTest.class).build();
        GraphQLObjectType additionalType = graphQLAnnotations.object(AdditionalTypeTest.class);

        // assert
        assertThat(schema.getType("additional"), notNullValue());
        assertThat(schema.getType("additional").toString(), is(additionalType.toString()));
    }
}
