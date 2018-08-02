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
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
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
