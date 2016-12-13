package graphql.annotations;

import java.util.HashMap;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.PropertyDataFetcher;
import org.testng.annotations.Test;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class GraphQLDataFetcherTest {

  @Test
  public void shouldUsePreferredConstructor() {
    // Given
    final GraphQLObjectType object = GraphQLAnnotations.object(GraphQLDataFetcherTest.TestGraphQLQuery.class);
    final GraphQLSchema schema = newSchema().query(object).build();
    final GraphQL graphql = new GraphQL(schema);

    // When
    final ExecutionResult result = graphql.execute("{sample {isGreat}}");

    // Then
    final HashMap<String, Object> data = (HashMap) result.getData();
    assertNotNull(data);
    assertTrue(((HashMap<String,Boolean>)data.get("sample")).get("isGreat"));
  }

  @GraphQLName("Query")
  public static class TestGraphQLQuery {
    @GraphQLField
    @GraphQLDataFetcher(SampleDataFetcher.class)
    public TestSample sample() { // Note that GraphQL uses TestSample to build the graph
      return null;
    }
  }

  public static class TestSample {
    @GraphQLField
    @GraphQLDataFetcher(value = PropertyDataFetcher.class, args = "isGreat")
    private Boolean isGreat = false; // Defaults to FieldDataFetcher
  }

  public static class SampleDataFetcher implements DataFetcher {
    @Override
    public Object get(final DataFetchingEnvironment environment) {
      return new Sample(); // Notice that it return a Sample, not a TestSample
    }
  }

  public static class Sample {
    private Boolean isGreat = true;

    public Boolean getIsGreat() {
      return isGreat;
    }

    public void setIsGreat(final Boolean isGreat) {
      this.isGreat = isGreat;
    }
  }
}