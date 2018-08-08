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
package graphql.annotations; /**
 * Copyright 2016 Yurii Rashkovskii
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.annotations.annotationTypes.GraphQLDirectives;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.directives.AnnotationsDirectiveWiring;
import graphql.annotations.directives.AnnotationsWiringEnvironment;
import graphql.annotations.directives.Directive;
import graphql.annotations.directives.creation.DirectiveLocations;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.introspection.Introspection;
import graphql.schema.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLDirective.newDirective;
import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GraphQLDirectivesTest {

    @BeforeClass
    public static void setUp() throws Exception {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
        GraphQLAnnotations.getInstance().getContainer().getDirectiveRegistry().clear();
    }


    public static class UpperWiring implements AnnotationsDirectiveWiring {
        @Override
        public GraphQLFieldDefinition onField(AnnotationsWiringEnvironment environment) {
            GraphQLFieldDefinition field = (GraphQLFieldDefinition) environment.getElement();
            boolean isActive = (boolean) environment.getDirective().getArgument("isActive").getValue();
            DataFetcher dataFetcher = DataFetcherFactories.wrapDataFetcher(field.getDataFetcher(), (((dataFetchingEnvironment, value) -> {
                if (value instanceof String && isActive) {
                    return ((String) value).toUpperCase();
                }
                return value;
            })));
            return field.transform(builder -> builder.dataFetcher(dataFetcher));
        }
    }

    public static class SuffixWiring implements AnnotationsDirectiveWiring {
        @Override
        public GraphQLFieldDefinition onField(AnnotationsWiringEnvironment environment) {
            GraphQLFieldDefinition field = (GraphQLFieldDefinition) environment.getElement();
            String suffix = (String) environment.getDirective().getArgument("suffix").getValue();
            DataFetcher dataFetcher = DataFetcherFactories.wrapDataFetcher(field.getDataFetcher(), (((dataFetchingEnvironment, value) -> {
                if (value instanceof String) {
                    return value + suffix;
                }
                return value;
            })));
            return field.transform(builder -> builder.dataFetcher(dataFetcher));
        }
    }

    public static class Query {
        @GraphQLField
        @GraphQLDirectives(@Directive(name = "upperCase", wiringClass = UpperWiring.class, argumentsValues = {"true"}))
        public static String name() {
            return "yarin";
        }

        @GraphQLField
        @GraphQLDirectives(@Directive(name = "upperCase", wiringClass = UpperWiring.class, argumentsValues = {"false"}))
        public static String nameWithFalse() {
            return "yarin";
        }
    }

    public static class Query2 {
        @GraphQLField
        @GraphQLDirectives(@Directive(name = "upperCase", wiringClass = UpperWiring.class))
        public static String nameWithNoArgs() {
            return "yarin";
        }
    }

    public static class Query3 {
        @GraphQLField
        @GraphQLDirectives({@Directive(name = "upperCase", wiringClass = UpperWiring.class, argumentsValues = {"true"}),
                @Directive(name = "suffix", wiringClass = SuffixWiring.class, argumentsValues = {"coolSuffix"})})
        public static String name() {
            return "yarin";
        }
    }


    @Test(expectedExceptions = GraphQLAnnotationsException.class)
    public void queryName_noDirectivesProvidedToRegistry_exceptionIsThrown() throws Exception {
        GraphQLObjectType object = GraphQLAnnotations.object(Query.class);
        GraphQLSchema schema = newSchema().query(object).build();

        GraphQL.newGraphQL(schema).build().execute("query { name }");
    }

    @GraphQLName("upperCase")
    @DirectiveLocations(Introspection.DirectiveLocation.FIELD_DEFINITION)
    public static class UpperCase{
        boolean isActive;
    }

    @Test
    public void queryName_directivesProvidedToRegistry_wiringIsActivated() throws Exception {
        GraphQLDirective upperCase = GraphQLAnnotations.directive(UpperCase.class);
        GraphQLObjectType object = GraphQLAnnotations.object(Query.class, upperCase);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("query { name }");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result.getData()).get("name").toString(), "YARIN");
    }

    @Test
    public void queryNameWithFalse_directivesProvidedToRegistry_wiringIsActivated() throws Exception {
        GraphQLDirective upperCase = newDirective().name("upperCase").argument(builder -> builder.name("isActive").type(GraphQLBoolean))
                .validLocations(Introspection.DirectiveLocation.FIELD_DEFINITION).build();
        GraphQLObjectType object = GraphQLAnnotations.object(Query.class, upperCase);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("query { nameWithFalse }");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result.getData()).get("nameWithFalse").toString(), "yarin");
    }

    @Test
    public void queryNameWithNoArgs_directivesProvidedToRegistry_wiringIsActivated() throws Exception {
        GraphQLDirective upperCase = newDirective().name("upperCase").argument(builder -> builder.name("isActive").type(GraphQLBoolean).defaultValue(true))
                .validLocations(Introspection.DirectiveLocation.FIELD_DEFINITION).build();
        GraphQLObjectType object = GraphQLAnnotations.object(Query2.class, upperCase);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("query { nameWithNoArgs }");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result.getData()).get("nameWithNoArgs").toString(), "YARIN");
    }

    @Test(expectedExceptions = GraphQLAnnotationsException.class)
    public void queryNameWithNoArgs_noDefaultValue_exceptionIsThrown() throws Exception {
        GraphQLDirective upperCase = newDirective().name("upperCase").argument(builder -> builder.name("isActive").type(GraphQLBoolean))
                .validLocations(Introspection.DirectiveLocation.FIELD_DEFINITION).build();
        GraphQLObjectType object = GraphQLAnnotations.object(Query2.class, upperCase);
        GraphQLSchema schema = newSchema().query(object).build();

        GraphQL.newGraphQL(schema).build().execute("query { nameWithNoArgs }");
    }

    @Test
    public void queryName_chainedDirectives_wiringIsActivatedInCorrectOrder() throws Exception {
        GraphQLDirective upperCase = newDirective().name("upperCase").argument(builder -> builder.name("isActive").type(GraphQLBoolean).defaultValue(true))
                .validLocations(Introspection.DirectiveLocation.FIELD_DEFINITION).build();
        GraphQLDirective suffixDirective = GraphQLDirective.newDirective().name("suffix").argument(builder -> builder.name("suffix").type(GraphQLString))
                .validLocations(Introspection.DirectiveLocation.FIELD_DEFINITION).build();
        GraphQLObjectType object = GraphQLAnnotations.object(Query3.class, upperCase, suffixDirective);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("query { name }");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result.getData()).get("name").toString(), "YARINcoolSuffix");
    }


}
