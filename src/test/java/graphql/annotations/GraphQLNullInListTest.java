package graphql.annotations;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLInvokeDetached;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import graphql.schema.GraphQLSchema;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static graphql.annotations.AnnotationsSchemaCreator.newAnnotationsSchema;
import static org.testng.AssertJUnit.assertEquals;

public class GraphQLNullInListTest {

    @GraphQLName("Query")
    public static class Query {
        @GraphQLField
        @GraphQLInvokeDetached
        @GraphQLNonNull
        public List<@GraphQLNonNull String> stringsList() {
            return Arrays.asList("blabla", "blabla", null);
        }
    }

    @Test
    public void shouldThrowError(){
        GraphQLSchema schema = newAnnotationsSchema().query(Query.class).build();
        GraphQL graphql = GraphQL.newGraphQL(schema).build();

        ExecutionResult result = graphql.execute("{stringsList}");
        assertEquals(result.getErrors().size(), 1);
        assertEquals(result.getErrors().get(0).getMessage(), "Cannot return null for non-nullable type: 'String' within parent '[String!]' (/stringsList[2])");
        System.out.println(result);
    }

}
