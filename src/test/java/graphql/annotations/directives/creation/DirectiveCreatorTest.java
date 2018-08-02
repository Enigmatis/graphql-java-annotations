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
package graphql.annotations.directives.creation;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.directives.CommonPropertiesCreator;
import graphql.annotations.processor.directives.DirectiveArgumentCreator;
import graphql.annotations.processor.directives.DirectiveCreator;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.introspection.Introspection;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static graphql.Scalars.GraphQLBoolean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

public class DirectiveCreatorTest {
    private DirectiveCreator directiveCreator;
    private DirectiveArgumentCreator directiveArgumentCreator;
    private CommonPropertiesCreator commonPropertiesCreator;

    @GraphQLName("upper")
    @GraphQLDescription("upper")
    @DirectiveLocations({Introspection.DirectiveLocation.FIELD_DEFINITION, Introspection.DirectiveLocation.INTERFACE})
    public static class UpperDirective {
        private boolean isActive = true;
    }


    @GraphQLName("upper")
    @GraphQLDescription("upper")
    public static class UpperDirectiveNoValidLocations {
        private boolean isActive = true;
    }
    @BeforeMethod
    public void setUp() throws NoSuchFieldException {
        directiveArgumentCreator = Mockito.mock(DirectiveArgumentCreator.class);
        commonPropertiesCreator = Mockito.mock(CommonPropertiesCreator.class);
        when(directiveArgumentCreator.getArgument(UpperDirective.class.getDeclaredField("isActive"), UpperDirective.class))
                .thenReturn(GraphQLArgument.newArgument().name("isActive").type(GraphQLBoolean).defaultValue(true).build());
        when(commonPropertiesCreator.getDescription(any())).thenCallRealMethod();
        when(commonPropertiesCreator.getName(any())).thenCallRealMethod();
        directiveCreator = new DirectiveCreator(directiveArgumentCreator, commonPropertiesCreator);
    }

    @Test(expectedExceptions = GraphQLAnnotationsException.class)
    public void getDirective_noValidLocations_exceptionIsThrown(){
        // Act
        GraphQLDirective directive = directiveCreator.getDirective(UpperDirectiveNoValidLocations.class);
    }

    @Test
    public void getDirective_goodDirectiveClass_directiveIsCorrect() {
        // Act
        GraphQLDirective directive = directiveCreator.getDirective(UpperDirective.class);

        // Assert
        assertEquals(directive.getName(), "upper");
        assertEquals(directive.getDescription(), "upper");
        assertArrayEquals(directive.validLocations().toArray(), new Introspection.DirectiveLocation[]{Introspection.DirectiveLocation.FIELD_DEFINITION,
                Introspection.DirectiveLocation.INTERFACE});
        GraphQLArgument isActive = directive.getArgument("isActive");
        assertNotNull(isActive);
        assertEquals(isActive.getName(), "isActive");
        assertEquals(isActive.getType(), GraphQLBoolean);
        assertEquals(isActive.getDefaultValue(), true);
    }
}
