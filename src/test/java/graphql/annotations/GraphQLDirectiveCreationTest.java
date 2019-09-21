/**
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
package graphql.annotations;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.directives.definition.GraphQLDirectiveDefinition;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.directives.AnnotationsDirectiveWiring;
import graphql.annotations.annotationTypes.directives.definition.DirectiveLocations;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.introspection.Introspection;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLString;
import static org.testng.AssertJUnit.*;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

public class GraphQLDirectiveCreationTest {

    private GraphQLAnnotations graphQLAnnotations;

    @BeforeMethod
    public void setUp() {
        this.graphQLAnnotations = new GraphQLAnnotations();
    }

    /**
     * Defining of directives through a class (@Deprecated)
     */

    public static class GeneralWiring implements AnnotationsDirectiveWiring {

    }

    @GraphQLName("upper")
    @GraphQLDescription("makes string upper case")
    @DirectiveLocations({Introspection.DirectiveLocation.FIELD_DEFINITION, Introspection.DirectiveLocation.INTERFACE})
    @GraphQLDirectiveDefinition(wiring = GeneralWiring.class)
    public static class UpperDirective {
        private boolean isActive = true;
        @GraphQLName("suffixToAdd")
        @GraphQLDescription("adds suffix to the string")
        private String suffix = "";

        private String noDefaultValue;
    }

    @Test
    public void directive_suppliedDirectiveClass_returnCorrectDirective() {
        // Act
        GraphQLDirective directive = this.graphQLAnnotations.directive(UpperDirective.class);

        // Assert
        assertEquals(directive.getName(), "upper");
        assertEquals(directive.getDescription(), "makes string upper case");
        assertArrayEquals(directive.validLocations().toArray(), new Introspection.DirectiveLocation[]{Introspection.DirectiveLocation.FIELD_DEFINITION,
                Introspection.DirectiveLocation.INTERFACE});
        GraphQLArgument isActive = directive.getArgument("isActive");
        assertNotNull(isActive);
        assertEquals(isActive.getName(), "isActive");
        assertEquals(isActive.getType(), GraphQLBoolean);
        assertEquals(isActive.getDefaultValue(), true);

        GraphQLArgument suffixToAdd = directive.getArgument("suffixToAdd");
        assertNotNull(suffixToAdd);
        assertEquals(suffixToAdd.getType(), GraphQLString);
        assertEquals(suffixToAdd.getDescription(), "adds suffix to the string");
        assertEquals(suffixToAdd.getDefaultValue(), "");

        GraphQLArgument noDefaultValue = directive.getArgument("noDefaultValue");
        assertNotNull(noDefaultValue);
        assertNull(noDefaultValue.getDefaultValue());
    }

    /**
     * Defining of directives through a method
     */

    static class Wiring implements AnnotationsDirectiveWiring{

    }

    static class DirectivesMethodsContainer {
        @GraphQLName("upper")
        @GraphQLDescription("upper directive")
        @GraphQLDirectiveDefinition(wiring = Wiring.class)
        @DirectiveLocations({Introspection.DirectiveLocation.FIELD_DEFINITION, Introspection.DirectiveLocation.INTERFACE})
        public void upperDirective(@GraphQLName("isActive") @GraphQLDescription("is active") boolean isActive) {
        }

        @GraphQLName("suffix")
        @GraphQLDescription("suffix directive")
        @GraphQLDirectiveDefinition(wiring = Wiring.class)
        @DirectiveLocations({Introspection.DirectiveLocation.FIELD_DEFINITION, Introspection.DirectiveLocation.INTERFACE})
        public void suffixDirective(@GraphQLName("suffix") @GraphQLDescription("the suffix") String suffix) {
        }
    }


    @Test
    public void directive_suppliedDirectiveMethodContainer_returnCorrectDirective() {
        // Act
        Set<GraphQLDirective> directive = this.graphQLAnnotations.directives(DirectivesMethodsContainer.class);

        GraphQLDirective upper = (GraphQLDirective) directive.toArray()[0];
        GraphQLDirective suffix = (GraphQLDirective) directive.toArray()[1];

        // Assert
        assertEquals(upper.getName(), "upper");
        assertEquals(upper.getDescription(), "upper directive");
        assertArrayEquals(upper.validLocations().toArray(), new Introspection.DirectiveLocation[]{Introspection.DirectiveLocation.FIELD_DEFINITION,
                Introspection.DirectiveLocation.INTERFACE});
        GraphQLArgument isActive = upper.getArgument("isActive");
        assertNotNull(isActive);
        assertEquals(isActive.getName(), "isActive");
        assertEquals(isActive.getType(), GraphQLBoolean);
        assertNull(isActive.getDefaultValue());

        GraphQLArgument suffixToAdd = suffix.getArgument("suffix");
        assertNotNull(suffixToAdd);
        assertEquals(suffixToAdd.getType(), GraphQLString);
        assertEquals("the suffix", suffixToAdd.getDescription());
        assertNull(suffixToAdd.getDefaultValue());
    }


    /**
     * Defining of directives through a java annotation
     */

    @GraphQLName("upper")
    @GraphQLDescription("the upper")
    @GraphQLDirectiveDefinition(wiring = Wiring.class)
    @DirectiveLocations({Introspection.DirectiveLocation.FIELD_DEFINITION, Introspection.DirectiveLocation.INTERFACE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface UpperAnnotation {
        @GraphQLName("isActive")
        @GraphQLDescription("is active")
        boolean isActive() default true;
    }

    @GraphQLName("bla")
    @Retention(RetentionPolicy.RUNTIME)
    @interface NoDirectiveAnnotation {
        boolean isActive() default true;
    }

    @Test
    public void directive_suppliedDirectiveAnnotation_returnCorrectDirective() {
        // Act
        GraphQLDirective upper = this.graphQLAnnotations.directiveViaAnnotation(UpperAnnotation.class);

        // Assert
        assertEquals(upper.getName(), "upper");
        assertEquals(upper.getDescription(), "the upper");
        assertArrayEquals(upper.validLocations().toArray(), new Introspection.DirectiveLocation[]{Introspection.DirectiveLocation.FIELD_DEFINITION,
                Introspection.DirectiveLocation.INTERFACE});
        GraphQLArgument isActive = upper.getArgument("isActive");
        assertNotNull(isActive);
        assertEquals(isActive.getName(), "isActive");
        assertEquals(isActive.getType(), GraphQLBoolean);
        assertEquals(true,isActive.getDefaultValue());
    }

    @Test(expectedExceptions = GraphQLAnnotationsException.class)
    public void directive_suppliedNoDirectiveAnnotation_throwException() {
        // Act
        GraphQLDirective upper = this.graphQLAnnotations.directiveViaAnnotation(NoDirectiveAnnotation.class);
    }


}
