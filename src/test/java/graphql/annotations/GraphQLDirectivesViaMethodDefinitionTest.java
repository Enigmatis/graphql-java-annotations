package graphql.annotations;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.directives.activation.Directive;
import graphql.annotations.annotationTypes.directives.activation.GraphQLDirectives;
import graphql.annotations.annotationTypes.directives.definition.DirectiveLocations;
import graphql.annotations.annotationTypes.directives.definition.GraphQLDirectiveDefinition;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.introspection.Introspection;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GraphQLDirectivesViaMethodDefinitionTest {
    private GraphQLAnnotations graphQLAnnotations;
    private GraphQLSchema schema;

    public static class DirectivesContainer {
        @GraphQLName("suffix")
        @GraphQLDirectiveDefinition(wiring = GraphQLDirectivesViaClassDefinitionTest.SuffixWiring.class)
        @DirectiveLocations({Introspection.DirectiveLocation.FIELD_DEFINITION, Introspection.DirectiveLocation.ARGUMENT_DEFINITION})
        public static void suffixDirective(@GraphQLName("suffix") String suffix) {

        }

        @GraphQLField
        @GraphQLDirectives({
                @Directive(name = "suffix", argumentsValues = {"coolSuffix"})})
        public static String name() {
            return "yarin";
        }
    }


    @BeforeMethod
    public void setUp() {
        this.graphQLAnnotations = new GraphQLAnnotations();
        this.graphQLAnnotations.directives(DirectivesContainer.class);
        GraphQLObjectType object = this.graphQLAnnotations.object(DirectivesContainer.class);
        GraphQLCodeRegistry codeRegistry = graphQLAnnotations.getContainer().getCodeRegistryBuilder().build();
        this.schema = newSchema().query(object).codeRegistry(codeRegistry).build();

    }

    @Test
    public void queryName_directivesInAlternativeWayCreation_wiringIsActivated() throws Exception {
        // Act
        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("query { name }");
        // Assert
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result.getData()).get("name").toString(), "yarincoolSuffix");
    }
}
