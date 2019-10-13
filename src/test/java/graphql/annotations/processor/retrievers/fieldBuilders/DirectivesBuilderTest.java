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
package graphql.annotations.processor.retrievers.fieldBuilders;

import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.directives.activation.GraphQLDirectives;
import graphql.annotations.annotationTypes.directives.definition.DirectiveLocations;
import graphql.annotations.annotationTypes.directives.definition.GraphQLDirectiveDefinition;
import graphql.annotations.directives.AnnotationsDirectiveWiring;
import graphql.annotations.annotationTypes.directives.activation.Directive;
import graphql.annotations.processor.DirectiveAndWiring;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.introspection.Introspection;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputObjectType;
import org.testng.annotations.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLDirective.newDirective;
import static org.testng.Assert.*;

public class DirectivesBuilderTest {

    private static class WiringClass implements AnnotationsDirectiveWiring {

    }

    @GraphQLDirectiveDefinition(wiring = WiringClass.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
    @DirectiveLocations({Introspection.DirectiveLocation.FIELD, Introspection.DirectiveLocation.ARGUMENT_DEFINITION, Introspection.DirectiveLocation.OBJECT})
    @GraphQLName("anno")
    @interface AnnotationDirective{

    }

    @GraphQLDirectives({@Directive(name = "upperCase"), @Directive(name = "lowerCase")})
    @AnnotationDirective
    public static String decoratedField;

    @GraphQLDirectives({@Directive(name = "upperCase", argumentsValues = {"This is a string"}), @Directive(name = "lowerCase")})
    public static String decoratedFieldWithArguments;

    public String forTestMethod(@GraphQLDirectives(@Directive(name = "upperCase")) @AnnotationDirective String decoratedArgument) {
        return null;
    }

    public String forTestMethodParameterWithArguments(@GraphQLDirectives(@Directive(name = "upperCase", argumentsValues = {"This is a string"})) String decoratedArgument) {
        return null;
    }

    @GraphQLDirectives({@Directive(name = "upperCase"), @Directive(name = "lowerCase")})
    @AnnotationDirective
    public static class DecoratedClass {

    }

    @GraphQLDirectives({@Directive(name = "upperCase", argumentsValues = {"This is a string"}), @Directive(name = "lowerCase")})
    public static class DecoratedClassWithArgs {

    }

    // Field

    @Test(expectedExceptions = GraphQLAnnotationsException.class)
    public void testDecoratedField_noDirectiveInRegistry_throwAGraphQLAnnotationsException() throws Exception {
        // Arrange
        ProcessingElementsContainer processingElementsContainer = new ProcessingElementsContainer();
        DirectivesBuilder directivesBuilder = new DirectivesBuilder(this.getClass().getField("decoratedField"), processingElementsContainer);

        // Act
        directivesBuilder.build();
    }

    @Test
    public void testDecoratedField_directivesAreInRegistry_directivesAreBuilt() throws Exception {
        // Arrange
        ProcessingElementsContainer processingElementsContainer = new ProcessingElementsContainer();
        GraphQLDirective upperCase = newDirective().name("upperCase").argument(builder -> builder.name("myArg").type(GraphQLString).defaultValue("DefaultString")).build();
        GraphQLDirective lowerCase = newDirective().name("lowerCase").build();
        GraphQLDirective annotationDirective = newDirective().name("anno").build();

        processingElementsContainer.getDirectiveRegistry().put("upperCase", new DirectiveAndWiring(upperCase, WiringClass.class));
        processingElementsContainer.getDirectiveRegistry().put("lowerCase", new DirectiveAndWiring(lowerCase, WiringClass.class));
        processingElementsContainer.getDirectiveRegistry().put("anno", new DirectiveAndWiring(annotationDirective, WiringClass.class));

        DirectivesBuilder directivesBuilder = new DirectivesBuilder(this.getClass().getField("decoratedField"), processingElementsContainer);

        // Act
        GraphQLDirective[] graphQLDirectives = directivesBuilder.build();

        // Assert
        assertEquals(graphQLDirectives[0].getName(), "anno");
        assertTrue(graphQLDirectives[0].getArguments().isEmpty());

        assertEquals(graphQLDirectives.length, 3);
        assertEquals(graphQLDirectives[1].getName(), "upperCase");
        assertFalse(graphQLDirectives[1].getArguments().isEmpty());
        assertEquals(graphQLDirectives[1].getArguments().get(0).getName(), "myArg");
        assertEquals(graphQLDirectives[1].getArguments().get(0).getValue(), "DefaultString");

        assertEquals(graphQLDirectives[2].getName(), "lowerCase");
        assertTrue(graphQLDirectives[2].getArguments().isEmpty());
    }

    @Test
    public void testDecoratedFieldWithArguments_directivesAreInRegistry_directivesAreBuilt() throws Exception {
        // Arrange
        ProcessingElementsContainer processingElementsContainer = new ProcessingElementsContainer();
        GraphQLDirective upperCase = newDirective().name("upperCase").argument(builder -> builder.name("myArg").type(GraphQLString)).build();
        GraphQLDirective lowerCase = newDirective().name("lowerCase").build();
        processingElementsContainer.getDirectiveRegistry().put("upperCase", new DirectiveAndWiring(upperCase, WiringClass.class));
        processingElementsContainer.getDirectiveRegistry().put("lowerCase", new DirectiveAndWiring(lowerCase, WiringClass.class));

        DirectivesBuilder directivesBuilder = new DirectivesBuilder(this.getClass().getField("decoratedFieldWithArguments"), processingElementsContainer);

        // Act
        GraphQLDirective[] graphQLDirectives = directivesBuilder.build();

        // Assert
        assertEquals(graphQLDirectives.length, 2);
        assertEquals(graphQLDirectives[0].getName(), "upperCase");
        assertFalse(graphQLDirectives[0].getArguments().isEmpty());
        assertEquals(graphQLDirectives[0].getArguments().get(0).getName(), "myArg");
        assertEquals(graphQLDirectives[0].getArguments().get(0).getValue(), "This is a string");
        assertEquals(graphQLDirectives[1].getName(), "lowerCase");
        assertTrue(graphQLDirectives[1].getArguments().isEmpty());
    }

    @Test
    public void testDecoratedFieldWithArguments_argumentsValuesLongerThanArgumentsNumber_throwAGraphQLAnnotationsException() throws Exception {
        // Arrange
        ProcessingElementsContainer processingElementsContainer = new ProcessingElementsContainer();
        GraphQLDirective upperCase = newDirective().name("upperCase").build();
        GraphQLDirective lowerCase = newDirective().name("lowerCase").build();
        processingElementsContainer.getDirectiveRegistry().put("upperCase", new DirectiveAndWiring(upperCase, WiringClass.class));
        processingElementsContainer.getDirectiveRegistry().put("lowerCase", new DirectiveAndWiring(lowerCase, WiringClass.class));

        DirectivesBuilder directivesBuilder = new DirectivesBuilder(this.getClass().getField("decoratedFieldWithArguments"), processingElementsContainer);

        // Act
        try {
            directivesBuilder.build();
            throw new Exception();
        } catch (GraphQLAnnotationsException e) {
            assertEquals(e.getMessage(), "Directive 'upperCase' is supplied with more argument values than it supports");
        }
    }

    @Test
    public void testDecoratedFieldWithArguments_directiveArgumentIsNotAScalarType_throwAGraphQLAnnotationsException() throws Exception {
        // Arrange
        ProcessingElementsContainer processingElementsContainer = new ProcessingElementsContainer();
        GraphQLInputObjectType inputField = GraphQLInputObjectType.newInputObject().name("inputField").build();
        GraphQLDirective upperCase = newDirective().name("upperCase").argument(builder -> builder.name("arg").type(inputField)).build();
        processingElementsContainer.getDirectiveRegistry().put("upperCase", new DirectiveAndWiring(upperCase, WiringClass.class));

        DirectivesBuilder directivesBuilder = new DirectivesBuilder(this.getClass().getField("decoratedFieldWithArguments"), processingElementsContainer);

        // Act
        try {
            directivesBuilder.build();
            throw new Exception();
        } catch (GraphQLAnnotationsException e) {
            assertEquals(e.getMessage(), "Directive argument type must be a scalar!");
        }
    }

    @Test
    public void testDecoratedFieldWithArguments_argumentValueIsNotTheSameTypeAsArgument_throwAGraphQLAnnotationsException() throws Exception {
        // Arrange
        ProcessingElementsContainer processingElementsContainer = new ProcessingElementsContainer();
        GraphQLDirective upperCase = newDirective().name("upperCase").argument(builder -> builder.name("arg").type(GraphQLInt)).build();
        processingElementsContainer.getDirectiveRegistry().put("upperCase", new DirectiveAndWiring(upperCase, WiringClass.class));

        DirectivesBuilder directivesBuilder = new DirectivesBuilder(this.getClass().getField("decoratedFieldWithArguments"), processingElementsContainer);

        // Act
        try {
            directivesBuilder.build();
            throw new Exception();
        } catch (GraphQLAnnotationsException e) {
            assertEquals(e.getMessage(), "Could not parse argument value to argument type");
        }
    }

    @Test
    public void testDecoratedFieldWithArguments_moreArgumentsThanArgumentsValues_directivesAreBuilt() throws Exception {
        // Arrange
        ProcessingElementsContainer processingElementsContainer = new ProcessingElementsContainer();
        GraphQLDirective upperCase = newDirective().name("upperCase").argument(builder -> builder.name("arg").type(GraphQLString))
                .argument(builder -> builder.name("arg2").type(GraphQLInt).defaultValue("5")).build();
        GraphQLDirective lowerCase = newDirective().name("lowerCase").build();
        processingElementsContainer.getDirectiveRegistry().put("upperCase", new DirectiveAndWiring(upperCase, WiringClass.class));
        processingElementsContainer.getDirectiveRegistry().put("lowerCase", new DirectiveAndWiring(lowerCase, WiringClass.class));

        DirectivesBuilder directivesBuilder = new DirectivesBuilder(this.getClass().getField("decoratedFieldWithArguments"), processingElementsContainer);

        GraphQLDirective[] graphQLDirectives = directivesBuilder.build();

        assertEquals(graphQLDirectives.length, 2);

        assertEquals(graphQLDirectives[0].getArguments().size(), 2);
        assertEquals(graphQLDirectives[0].getArguments().get(0).getName(), "arg");
        assertEquals(graphQLDirectives[0].getArguments().get(0).getValue(), "This is a string");

        assertEquals(graphQLDirectives[0].getArguments().get(1).getName(), "arg2");
        assertEquals(graphQLDirectives[0].getArguments().get(1).getValue(), "5");
    }

    // Argument
    @Test(expectedExceptions = GraphQLAnnotationsException.class)
    public void testDecoratedArgument_noDirectiveInRegistry_throwAGraphQLAnnotationsException() throws Exception {
        // Arrange
        ProcessingElementsContainer processingElementsContainer = new ProcessingElementsContainer();
        DirectivesBuilder directivesBuilder = new DirectivesBuilder(this.getClass().getMethod("forTestMethod", String.class).getParameters()[0], processingElementsContainer);

        // Act
        directivesBuilder.build();
    }

    @Test
    public void testDecoratedArgument_directivesAreInRegistry_directivesAreBuilt() throws Exception {
        // Arrange
        ProcessingElementsContainer processingElementsContainer = new ProcessingElementsContainer();
        GraphQLDirective upperCase = newDirective().name("upperCase").argument(builder -> builder.name("myArg").type(GraphQLString).defaultValue("DefaultString")).build();
        GraphQLDirective lowerCase = newDirective().name("lowerCase").build();
        GraphQLDirective anno = newDirective().name("anno").build();

        processingElementsContainer.getDirectiveRegistry().put("upperCase", new DirectiveAndWiring(upperCase, WiringClass.class));
        processingElementsContainer.getDirectiveRegistry().put("lowerCase", new DirectiveAndWiring(lowerCase, WiringClass.class));
        processingElementsContainer.getDirectiveRegistry().put("anno", new DirectiveAndWiring(anno, WiringClass.class));

        DirectivesBuilder directivesBuilder = new DirectivesBuilder(this.getClass().getMethod("forTestMethod", String.class).getParameters()[0], processingElementsContainer);

        // Act
        GraphQLDirective[] graphQLDirectives = directivesBuilder.build();

        // Assert
        assertEquals(graphQLDirectives.length, 2);
        assertEquals(graphQLDirectives[0].getName(), "anno");
        assertTrue(graphQLDirectives[0].getArguments().isEmpty());


        assertEquals(graphQLDirectives[1].getName(), "upperCase");
        assertFalse(graphQLDirectives[1].getArguments().isEmpty());
        assertEquals(graphQLDirectives[1].getArguments().get(0).getName(), "myArg");
        assertEquals(graphQLDirectives[1].getArguments().get(0).getValue(), "DefaultString");
    }

    @Test
    public void testDecoratedParameterWithArguments_directivesAreInRegistry_directivesAreBuilt() throws Exception {
        // Arrange
        ProcessingElementsContainer processingElementsContainer = new ProcessingElementsContainer();
        GraphQLDirective upperCase = newDirective().name("upperCase").argument(builder -> builder.name("myArg").type(GraphQLString)).build();
        GraphQLDirective lowerCase = newDirective().name("lowerCase").build();
        processingElementsContainer.getDirectiveRegistry().put("upperCase", new DirectiveAndWiring(upperCase, WiringClass.class));
        processingElementsContainer.getDirectiveRegistry().put("lowerCase", new DirectiveAndWiring(lowerCase, WiringClass.class));

        DirectivesBuilder directivesBuilder = new DirectivesBuilder(this.getClass().getMethod("forTestMethodParameterWithArguments", String.class).getParameters()[0], processingElementsContainer);

        // Act
        GraphQLDirective[] graphQLDirectives = directivesBuilder.build();

        // Assert
        assertEquals(graphQLDirectives.length, 1);
        assertEquals(graphQLDirectives[0].getName(), "upperCase");
        assertFalse(graphQLDirectives[0].getArguments().isEmpty());
        assertEquals(graphQLDirectives[0].getArguments().get(0).getName(), "myArg");
        assertEquals(graphQLDirectives[0].getArguments().get(0).getValue(), "This is a string");
    }

    // Class
    @Test(expectedExceptions = GraphQLAnnotationsException.class)
    public void testDecoratedClass_noDirectiveInRegistry_throwAGraphQLAnnotationsException() throws Exception {
        // Arrange
        ProcessingElementsContainer processingElementsContainer = new ProcessingElementsContainer();
        DirectivesBuilder directivesBuilder = new DirectivesBuilder(DecoratedClass.class, processingElementsContainer);

        // Act
        directivesBuilder.build();
    }

    @Test
    public void testDecoratedClass_directivesAreInRegistry_directivesAreBuilt() throws Exception {
        // Arrange
        ProcessingElementsContainer processingElementsContainer = new ProcessingElementsContainer();
        GraphQLDirective upperCase = newDirective().name("upperCase").argument(builder -> builder.name("myArg").type(GraphQLString).defaultValue("DefaultString")).build();
        GraphQLDirective lowerCase = newDirective().name("lowerCase").build();
        GraphQLDirective anno = newDirective().name("anno").build();

        processingElementsContainer.getDirectiveRegistry().put("upperCase", new DirectiveAndWiring(upperCase, WiringClass.class));
        processingElementsContainer.getDirectiveRegistry().put("lowerCase", new DirectiveAndWiring(lowerCase, WiringClass.class));
        processingElementsContainer.getDirectiveRegistry().put("anno", new DirectiveAndWiring(anno, WiringClass.class));

        DirectivesBuilder directivesBuilder = new DirectivesBuilder(DecoratedClass.class, processingElementsContainer);

        // Act
        GraphQLDirective[] graphQLDirectives = directivesBuilder.build();

        // Assert
        assertEquals(graphQLDirectives.length, 3);

        assertEquals(graphQLDirectives[0].getName(), "anno");
        assertTrue(graphQLDirectives[0].getArguments().isEmpty());


        assertEquals(graphQLDirectives[1].getName(), "upperCase");
        assertFalse(graphQLDirectives[1].getArguments().isEmpty());
        assertEquals(graphQLDirectives[1].getArguments().get(0).getName(), "myArg");
        assertEquals(graphQLDirectives[1].getArguments().get(0).getValue(), "DefaultString");

        assertEquals(graphQLDirectives[2].getName(), "lowerCase");
        assertTrue(graphQLDirectives[2].getArguments().isEmpty());
    }

    @Test
    public void testDecoratedClassWithArguments_directivesAreInRegistry_directivesAreBuilt() throws Exception {
        // Arrange
        ProcessingElementsContainer processingElementsContainer = new ProcessingElementsContainer();
        GraphQLDirective upperCase = newDirective().name("upperCase").argument(builder -> builder.name("myArg").type(GraphQLString)).build();
        GraphQLDirective lowerCase = newDirective().name("lowerCase").build();
        processingElementsContainer.getDirectiveRegistry().put("upperCase", new DirectiveAndWiring(upperCase, WiringClass.class));
        processingElementsContainer.getDirectiveRegistry().put("lowerCase", new DirectiveAndWiring(lowerCase, WiringClass.class));

        DirectivesBuilder directivesBuilder = new DirectivesBuilder(DecoratedClassWithArgs.class, processingElementsContainer);

        // Act
        GraphQLDirective[] graphQLDirectives = directivesBuilder.build();

        // Assert
        assertEquals(graphQLDirectives.length, 2);
        assertEquals(graphQLDirectives[0].getName(), "upperCase");
        assertFalse(graphQLDirectives[0].getArguments().isEmpty());
        assertEquals(graphQLDirectives[0].getArguments().get(0).getName(), "myArg");
        assertEquals(graphQLDirectives[0].getArguments().get(0).getValue(), "This is a string");
        assertEquals(graphQLDirectives[1].getName(), "lowerCase");
        assertTrue(graphQLDirectives[1].getArguments().isEmpty());
    }


}
