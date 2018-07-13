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
package graphql.annotations.directives;

import graphql.annotations.annotationTypes.GraphQLDirectives;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.schema.GraphQLDirective;
import org.testng.annotations.Test;

import java.util.Map;

import static graphql.schema.GraphQLDirective.newDirective;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class DirectiveWiringMapRetrieverTest {

    public static class WiringClass implements AnnotationsDirectiveWiring {

    }

    public static class SecondWiringClass implements AnnotationsDirectiveWiring {

    }

    private static class ThirdWiringClass implements AnnotationsDirectiveWiring{

    }

    @GraphQLDirectives({@Directive(name = "upperCase", wiringClass = WiringClass.class), @Directive(name = "lowerCase", wiringClass = SecondWiringClass.class)})
    public static String field;

    @GraphQLDirectives({@Directive(name = "upperCase", wiringClass = ThirdWiringClass.class)})
    public static String fieldWithPrivateWiringClassThatShouldFail;

    @Test
    public void getDirectiveWiringMap_noDirectivesInRegistry_throwAGraphQLAnnotationsException() throws Exception {
        // Arrange
        DirectiveWiringMapRetriever directiveWiringMapRetriever = new DirectiveWiringMapRetriever();
        ProcessingElementsContainer container = new ProcessingElementsContainer();

        // Act
        try {
            directiveWiringMapRetriever.getDirectiveWiringMap(this.getClass().getField("field"), container);
            throw new Exception();
        } catch (GraphQLAnnotationsException e) {
            assertEquals(e.getMessage(), "No directive named upperCase is found in the directive registry");
        }
    }

    @Test
    public void getDirectiveWiringMap_wiringClassIsPrivate_throwAGraphQLAnnotationsException() throws Exception {
        // Arrange
        DirectiveWiringMapRetriever directiveWiringMapRetriever = new DirectiveWiringMapRetriever();
        GraphQLDirective upperCase = newDirective().name("upperCase").build();
        GraphQLDirective lowerCase = newDirective().name("lowerCase").build();
        ProcessingElementsContainer container = new ProcessingElementsContainer();
        container.getDirectiveRegistry().put("upperCase", upperCase);
        container.getDirectiveRegistry().put("lowerCase", lowerCase);

        // Act
        try {
            directiveWiringMapRetriever.getDirectiveWiringMap(this.getClass().getField("fieldWithPrivateWiringClassThatShouldFail"), container);
            throw new Exception();
        } catch (GraphQLAnnotationsException e) {
            assertEquals(e.getMessage(), "Cannot create an instance of the wiring class " + ThirdWiringClass.class.getSimpleName());
        }
    }


    @Test
    public void getDirectiveWiringMap_directivesAreInRegistry_returnCorrectMap() throws Exception {
        // Arrange
        DirectiveWiringMapRetriever directiveWiringMapRetriever = new DirectiveWiringMapRetriever();
        GraphQLDirective upperCase = newDirective().name("upperCase").build();
        GraphQLDirective lowerCase = newDirective().name("lowerCase").build();
        ProcessingElementsContainer container = new ProcessingElementsContainer();
        container.getDirectiveRegistry().put("upperCase", upperCase);
        container.getDirectiveRegistry().put("lowerCase", lowerCase);

        // Act
        Map<GraphQLDirective, AnnotationsDirectiveWiring> map = directiveWiringMapRetriever.getDirectiveWiringMap(this.getClass().getField("field"), container);

        // Assert
        assertTrue(map.get(upperCase) instanceof WiringClass);
        assertTrue(map.get(lowerCase) instanceof SecondWiringClass);
    }
}
