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
import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.directives.definition.DirectiveLocations;
import graphql.annotations.annotationTypes.directives.definition.GraphQLDirectiveDefinition;
import graphql.annotations.directives.AnnotationsDirectiveWiring;
import graphql.annotations.directives.AnnotationsWiringEnvironment;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.annotations.processor.util.CodeRegistryUtil;
import graphql.introspection.Introspection;
import graphql.schema.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import static graphql.annotations.AnnotationsSchemaCreator.newAnnotationsSchema;
import static org.testng.Assert.*;

public class GraphQLDirectivesViaAnnotationDefinitionTest {
    private GraphQLAnnotations graphQLAnnotations;
    private GraphQLSchema schema;

    @BeforeMethod
    public void setUp() {
        this.graphQLAnnotations = new GraphQLAnnotations();
        this.schema = newAnnotationsSchema().query(Query.class).directives(Upper.class, Suffix.class, DirectiveWithList.class).build();
    }

    /**
     * Definitions
     */

    public static class UpperWiring implements AnnotationsDirectiveWiring {
        @Override
        public GraphQLFieldDefinition onField(AnnotationsWiringEnvironment environment) {
            GraphQLFieldDefinition field = (GraphQLFieldDefinition) environment.getElement();
            boolean isActive = (boolean) environment.getDirective().getArgument("isActive").getArgumentValue().getValue();
            CodeRegistryUtil.wrapDataFetcher(field, environment, (((dataFetchingEnvironment, value) -> {
                if (value instanceof String && isActive) {
                    return ((String) value).toUpperCase();
                }
                return value;
            })));

            return field;
        }
    }

    public static class SuffixWiring implements AnnotationsDirectiveWiring {
        @Override
        public GraphQLInputObjectField onInputObjectField(AnnotationsWiringEnvironment environment) {
            GraphQLInputObjectField field = (GraphQLInputObjectField) environment.getElement();
            boolean isActive = (boolean) environment.getDirective().getArgument("isActive").getArgumentValue().getValue();
            if (!isActive) return field;
            String suffix = (String) environment.getDirective().getArgument("suffix").getArgumentValue().getValue();
            return field.transform(builder -> builder.name(field.getName() + suffix));
        }

        @Override
        public GraphQLFieldDefinition onField(AnnotationsWiringEnvironment environment) {
            GraphQLFieldDefinition field = (GraphQLFieldDefinition) environment.getElement();
            String suffix = (String) environment.getDirective().getArgument("suffix").getArgumentValue().getValue();
            boolean isActive = (boolean) environment.getDirective().getArgument("isActive").getArgumentValue().getValue();
            if (!isActive) return field;
            CodeRegistryUtil.wrapDataFetcher(field, environment, (dataFetchingEnvironment, value) -> {
                if (value instanceof String) {
                    return value + suffix;
                }
                return value;
            });

            return field;
        }

        @Override
        public GraphQLArgument onArgument(AnnotationsWiringEnvironment environment) {
            GraphQLArgument element = (GraphQLArgument) environment.getElement();
            String suffix = (String) environment.getDirective().getArgument("suffix").getArgumentValue().getValue();
            boolean isActive = (boolean) environment.getDirective().getArgument("isActive").getArgumentValue().getValue();
            if (!isActive) return element;
            return element.transform(builder -> builder.name(element.getName() + suffix));
        }

        @Override
        public GraphQLInputObjectType onInputObjectType(AnnotationsWiringEnvironment environment) {
            GraphQLInputObjectType element = (GraphQLInputObjectType) environment.getElement();
            boolean isActive = (boolean) environment.getDirective().getArgument("isActive").getArgumentValue().getValue();
            if (!isActive) return element;
            String suffix = (String) environment.getDirective().getArgument("suffix").getArgumentValue().getValue();
            return element;
        }
    }

    public static class DirectiveWithListWiring implements AnnotationsDirectiveWiring{
        @Override
        public GraphQLFieldDefinition onField(AnnotationsWiringEnvironment environment) {
            GraphQLFieldDefinition field = (GraphQLFieldDefinition) environment.getElement();
            String[] list= (String[]) environment.getDirective().getArgument("list").getArgumentValue().getValue();
            CodeRegistryUtil.wrapDataFetcher(field, environment, (dataFetchingEnvironment, value) -> value + list[0]);
            return field;
        }
    }


    @GraphQLDirectiveDefinition(wiring = UpperWiring.class)
    @GraphQLName("upper")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @DirectiveLocations(Introspection.DirectiveLocation.FIELD_DEFINITION)
    @interface Upper{
        boolean isActive() default true;
    }

    @GraphQLDirectiveDefinition(wiring = SuffixWiring.class)
    @GraphQLName("suffix")
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @DirectiveLocations({Introspection.DirectiveLocation.FIELD_DEFINITION, Introspection.DirectiveLocation.ARGUMENT_DEFINITION, Introspection.DirectiveLocation.INPUT_FIELD_DEFINITION})
    @interface Suffix{
        @GraphQLName("suffix")
        String suffixToAdd();
        boolean isActive();
    }

    @GraphQLDirectiveDefinition(wiring = DirectiveWithListWiring.class)
    @GraphQLName("list")
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @DirectiveLocations({Introspection.DirectiveLocation.FIELD_DEFINITION, Introspection.DirectiveLocation.ARGUMENT_DEFINITION, Introspection.DirectiveLocation.INPUT_FIELD_DEFINITION})
    @interface DirectiveWithList{
        String[] list();
    }

    public static class Query{
        @GraphQLField
        @Upper
        public static String goodAnnotatedNameWithDefaultValue(){return "yarin";}

        @GraphQLField
        @Upper(isActive = true)
        public static String goodAnnotatedNameWithTrueValue(){return "yarin";}

        @GraphQLField
        @Upper(isActive = false)
        public static String goodAnnotatedNameWithFalseValue(){return "yarin";}

        @GraphQLField
        @Suffix(isActive = true, suffixToAdd = " is cool")
        public static String goodAnnotatedNameWithUnOrderedValues() {return "yarin";}

        @GraphQLField
        @GraphQLDataFetcher(NameWithArgument.class)
        public static String nameWithArgument(@Suffix(isActive = true, suffixToAdd = "x") @GraphQLName("extensionArg") String extensionArg) {
            return "yarin" + extensionArg;
        }

        @GraphQLField
        public static String nameWithInputObject(@GraphQLName("inputObject") InputObject input) {
            return "yarin";
        }

        @GraphQLField
        @Upper
        @Suffix(isActive = true, suffixToAdd = " is cool")
        public static String nameWithMultipleDirectives(){
            return "yarin";
        }

        @GraphQLField
        @DirectiveWithList(list = {"v", "x", "y"})
        public static String nameWithDirectiveWithList() {return "yarin";}

        public static class NameWithArgument implements DataFetcher {
            @Override
            public Object get(DataFetchingEnvironment environment) throws Exception {
                String value = environment.getArgument("extensionArgx");
                return "yarin"+value;
            }
        }

        public static class InputObject {
            @GraphQLField
            @Suffix(isActive = true, suffixToAdd = "coolSuffix")
            private String a;

            @GraphQLField
            private int b;

            public InputObject(@GraphQLName("a") String a, @GraphQLName("b") int b) {
                this.a = a;
                this.b = b;
            }
        }

    }

    @Test
    public void usingAnnotationDirective_goodAnnotatedFieldWithDefaultValue_wiringHappens() {
        // Act
        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("query { goodAnnotatedNameWithDefaultValue }");

        // Assert
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result.getData()).get("goodAnnotatedNameWithDefaultValue").toString(), "YARIN");
    }

    @Test
    public void usingAnnotationDirective_goodAnnotatedFieldWithCustomValue_wiringHappens() {
        // Act
        ExecutionResult result1 = GraphQL.newGraphQL(schema).build().execute("query { goodAnnotatedNameWithTrueValue }");
        ExecutionResult result2 = GraphQL.newGraphQL(schema).build().execute("query { goodAnnotatedNameWithFalseValue }");

        // Assert
        assertTrue(result1.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result1.getData()).get("goodAnnotatedNameWithTrueValue").toString(), "YARIN");
        assertTrue(result2.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result2.getData()).get("goodAnnotatedNameWithFalseValue").toString(), "yarin");
    }

    @Test
    public void usingAnnotationDirective_goodAnnotatedFieldWithUnOrderedValues_wiringHappens() {
        // Act
        ExecutionResult result1 = GraphQL.newGraphQL(schema).build().execute("query { goodAnnotatedNameWithUnOrderedValues }");

        // Assert
        assertTrue(result1.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result1.getData()).get("goodAnnotatedNameWithUnOrderedValues").toString(), "yarin is cool");
    }

    @Test
    public void usingAnnotationDirective_annotatedParam_wiringHappens(){
        // Act
        ExecutionResult result1 = GraphQL.newGraphQL(schema).build().execute("query { nameWithArgument(extensionArgx: \" is cool\") }");

        // Assert
        assertTrue(result1.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result1.getData()).get("nameWithArgument").toString(), "yarin is cool");
    }

    @Test
    public void usingAnnotationDirective_inputObject_wiringHappens(){
        // Act
        GraphQLFieldDefinition nameWithInputObject = schema.getQueryType().getFieldDefinition("nameWithInputObject");
        GraphQLInputObjectField field = ((GraphQLInputObjectType) nameWithInputObject.getArgument("inputObject").getType()).getField("acoolSuffix");
        // Assert
        assertNotNull(field);
    }

    @Test
    public void usingAnnotationDirective_multipleDirectivesOnField_wiringHappensInOrder(){
        // Act
        ExecutionResult result1 = GraphQL.newGraphQL(schema).build().execute("query { nameWithMultipleDirectives }");

        // Assert
        assertTrue(result1.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result1.getData()).get("nameWithMultipleDirectives").toString(), "YARIN is cool");
    }

    @Test
    public void usingAnnotationDirective_listArgument_wiringHappens(){
        // Act
        ExecutionResult result1 = GraphQL.newGraphQL(schema).build().execute("query { nameWithDirectiveWithList }");

        // Assert
        assertTrue(result1.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result1.getData()).get("nameWithDirectiveWithList").toString(), "yarinv");
    }


}
