package graphql.annotations.connection;

import graphql.GraphQL;
import graphql.annotations.GraphQLAnnotations;
import graphql.annotations.GraphQLDataFetcher;
import graphql.annotations.GraphQLField;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static graphql.schema.GraphQLSchema.newSchema;

public class GraphQLEnhancedConnectionTest {

    private static GraphQL graphQL;


    @BeforeClass
    public static void setUp() throws Exception {
        GraphQLObjectType object = GraphQLAnnotations.object(TestListField.class);
        GraphQLSchema schema = newSchema().query(object).build();

        graphQL = GraphQL.newGraphQL(schema).build();
    }

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    public static class Obj {
        @GraphQLField
        public String id;
        @GraphQLField
        public String val;

        public Obj(String id, String val) {
            this.id = id;
            this.val = val;
        }
    }

    public static class TestListField {
        @GraphQLField
        @GraphQLConnection(connection = EnhancedConnectionFetcher.class)
        @GraphQLDataFetcher(PaginationDataFetcher.class)
        public List<GraphQLConnectionTest.Obj> objs;

        public TestListField(List<GraphQLConnectionTest.Obj> objs) {
            this.objs = objs;
        }
    }


    @Test
    public void name() throws Exception {

    }
}
