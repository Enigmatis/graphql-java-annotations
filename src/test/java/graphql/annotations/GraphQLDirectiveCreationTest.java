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
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.directives.creation.DirectiveLocations;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.introspection.Introspection;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import org.testng.annotations.Test;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLString;
import static org.testng.AssertJUnit.*;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

public class GraphQLDirectiveCreationTest {

    @GraphQLName("upper")
    @GraphQLDescription("makes string upper case")
    @DirectiveLocations({Introspection.DirectiveLocation.FIELD_DEFINITION, Introspection.DirectiveLocation.INTERFACE})
    public static class UpperDirective {
        private boolean isActive = true;
        @GraphQLName("suffixToAdd")
        @GraphQLDescription("adds suffix to the string")
        private String suffix = "";

        private String noDefaultValue;
    }

    @Test
    public void test_directive_creation() {
        // Act
        GraphQLDirective directive = GraphQLAnnotations.directive(UpperDirective.class);

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
}
