package graphql.annotations;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLUnion;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.annotations.processor.retrievers.GraphQLInterfaceRetriever;
import graphql.annotations.typeResolvers.UnionTypeResolver;
import graphql.schema.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.lang.model.type.UnionType;
import java.util.List;
import java.util.Map;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GraphQLUnionTest {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    @Test
    public void getGraphQLType_typeIsUnion_returnsUnionType() throws Exception {
        //Arrange
        GraphQLInterfaceRetriever graphQLInterfaceRetriever = GraphQLAnnotations.getInstance().getObjectHandler().getTypeRetriever().getGraphQLInterfaceRetriever();

        //Act
        GraphQLOutputType unionType = graphQLInterfaceRetriever.getInterface(Hardware.class, GraphQLAnnotations.getInstance().getContainer());

        //Assert
        assertThat(unionType, instanceOf(UnionType.class));
    }

    @Test
    public void getResolver_resolverIsDefaultOne_returnsUnionTypeResolver() throws Exception {
        //Arrange
        GraphQLInterfaceRetriever graphQLInterfaceRetriever = GraphQLAnnotations.getInstance().getObjectHandler().getTypeRetriever().getGraphQLInterfaceRetriever();

        //Act
        GraphQLUnionType unionType = (GraphQLUnionType) graphQLInterfaceRetriever.getInterface(Hardware.class, GraphQLAnnotations.getInstance().getContainer());
        TypeResolver typeResolver = unionType.getTypeResolver();

        //Assert
        assertThat(typeResolver, instanceOf(UnionTypeResolver.class));
    }

    @Test
    public void unionType_buildSchema_unionIsAFieldOfQuery() throws Exception {
        //Act
        GraphQLObjectType object = GraphQLAnnotations.object(Query.class);
        List<GraphQLFieldDefinition> unions = object.getFieldDefinitions();

        //Assert
        assertThat(unions.size(), is(1));
        assertThat(unions.get(0).getName(), is("hardware"));
    }

    @Test
    public void query() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(Query.class)).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ hardware{ ... on Computer {name}} }");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("hardware").get("name"), "Guy");
    }


    @GraphQLUnion(possibleTypes = {Computer.class, Screen.class})
    interface Hardware {
    }

    private static class Screen {
    }

    // Hibernate class with same structure of API class
    static class ComputerDB {
        String name;

        public ComputerDB(String name) {
            this.name = name;
        }
    }

    public static class HardwareFetcher implements DataFetcher<ComputerDB> {
        ComputerDB computerDB = new ComputerDB("Guy");

        @Override
        public ComputerDB get(DataFetchingEnvironment environment) {
            return computerDB;
        }
    }

    class Computer implements Hardware {
        @GraphQLField
        String name;
    }

    class Query {
        @GraphQLField
        @GraphQLDataFetcher(HardwareFetcher.class)
        public Hardware getHardware() {
            return null;
        }
    }

}
