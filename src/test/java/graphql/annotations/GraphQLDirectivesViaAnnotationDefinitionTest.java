package graphql.annotations;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.directives.activation.Directive;
import graphql.annotations.annotationTypes.directives.activation.GraphQLDirectives;
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

import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class GraphQLDirectivesViaAnnotationDefinitionTest {
    private GraphQLAnnotations graphQLAnnotations;
    private GraphQLSchema schema;

    @BeforeMethod
    public void setUp() {
        this.graphQLAnnotations = new GraphQLAnnotations();
        this.graphQLAnnotations.directiveViaAnnotation(Upper.class);
        this.graphQLAnnotations.directiveViaAnnotation(Suffix.class);
        GraphQLObjectType object = this.graphQLAnnotations.object(Query.class);
        GraphQLCodeRegistry codeRegistry = graphQLAnnotations.getContainer().getCodeRegistryBuilder().build();
        this.schema = newSchema().query(object).codeRegistry(codeRegistry).build();
    }

    /**
     * Definitions
     */

    public static class UpperWiring implements AnnotationsDirectiveWiring {
        @Override
        public GraphQLFieldDefinition onField(AnnotationsWiringEnvironment environment) {
            GraphQLFieldDefinition field = (GraphQLFieldDefinition) environment.getElement();
            boolean isActive = (boolean) environment.getDirective().getArgument("isActive").getValue();
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
            boolean isActive = (boolean) environment.getDirective().getArgument("isActive").getValue();
            if (!isActive) return field;
            String suffix = (String) environment.getDirective().getArgument("suffix").getValue();
            return field.transform(builder -> builder.name(field.getName() + suffix));
        }

        @Override
        public GraphQLFieldDefinition onField(AnnotationsWiringEnvironment environment) {
            GraphQLFieldDefinition field = (GraphQLFieldDefinition) environment.getElement();
            String suffix = (String) environment.getDirective().getArgument("suffix").getValue();
            boolean isActive = (boolean) environment.getDirective().getArgument("isActive").getValue();
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
            String suffix = (String) environment.getDirective().getArgument("suffix").getValue();
            boolean isActive = (boolean) environment.getDirective().getArgument("isActive").getValue();
            if (!isActive) return element;
            return element.transform(builder -> builder.name(element.getName() + suffix));
        }

        @Override
        public GraphQLInputObjectType onInputObjectType(AnnotationsWiringEnvironment environment) {
            GraphQLInputObjectType element = (GraphQLInputObjectType) environment.getElement();
            boolean isActive = (boolean) environment.getDirective().getArgument("isActive").getValue();
            if (!isActive) return element;
            String suffix = (String) environment.getDirective().getArgument("suffix").getValue();
            return element;
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

}
