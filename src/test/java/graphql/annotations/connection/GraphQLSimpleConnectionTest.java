package graphql.annotations.connection;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;

public class GraphQLSimpleConnectionTest {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    @Test
    public void simpleConnection_buildSchema_TypeOfSimpleConnectionIsGraphQLList() throws Exception {
        GraphQLObjectType object = GraphQLAnnotations.object(MainConnection.class);
        GraphQLSchema schema = newSchema().query(object).build();

        String objsTypeName = schema.getQueryType().getFieldDefinition("objs").getType().getName();

        assertThat(objsTypeName, is("ObjChunk"));
    }

    @Test(expectedExceptions = GraphQLConnectionException.class)
    public void simpleConnection_fieldDoesNotHaveDataFetcherAnnotation_throwsError() {
        GraphQLAnnotations.object(TestConnectionOnField.class);
    }

    @Test(expectedExceptions = GraphQLConnectionException.class)
    public void simpleConnection_returnTypeIsNotValid_throwsError() {
        GraphQLAnnotations.object(TestConnectionNotGoodReturnType.class);
    }

    @Test
    public void simpleConnection_queryForOverAll_getCorrectAnswer() {
        GraphQLObjectType object = GraphQLAnnotations.object(MainConnection.class);
        GraphQLSchema schema = newSchema().query(object).build();
        GraphQL graphQL = GraphQL.newGraphQL(schema).build();


        ExecutionResult executionResult = graphQL.execute("{ objs(first: 1) {overAllCount}}");

        int overAllCount = (Integer) ((HashMap) ((HashMap) executionResult.getData()).get("objs")).get("overAllCount");

        assertEquals(overAllCount, 5);
    }

    @Test
    public void simpleConnection_queryForTwoObject_getTwoObject() {
        GraphQLObjectType object = GraphQLAnnotations.object(MainConnection.class);
        GraphQLSchema schema = newSchema().query(object).build();
        GraphQL graphQL = GraphQL.newGraphQL(schema).build();


        ExecutionResult executionResult = graphQL.execute("{ objs(first: 2){data{id,val}}}");

        List data = (List) ((HashMap) ((HashMap) executionResult.getData()).get("objs")).get("data");

        assertEquals(data.size(), 2);
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

    public static class TestConnectionOnField {
        @GraphQLField
        @GraphQLSimpleConnection
        public SimplePaginatedData<Obj> objs;

        public TestConnectionOnField(List<Obj> objs) {
            this.objs = new SimplePaginatedDataImpl<>(objs, objs.size());
        }
    }

    public static class TestConnectionNotGoodReturnType {
        @GraphQLField
        @GraphQLSimpleConnection
        @GraphQLDataFetcher(ObjsSimpleConnectionFetcher.class)
        public List<Obj> objs;

        public TestConnectionNotGoodReturnType(List<Obj> objs) {
            this.objs = objs;
        }
    }

    public static class MainConnection {
        @GraphQLField
        @GraphQLSimpleConnection
        @GraphQLDataFetcher(ObjsSimpleConnectionFetcher.class)
        public SimplePaginatedData<Obj> objs;
    }

    public static class ObjsSimpleConnectionFetcher implements DataFetcher<SimplePaginatedData<Obj>> {

        @Override
        public SimplePaginatedData<Obj> get(DataFetchingEnvironment environment) {
            List<Obj> objList = new ArrayList<>();
            objList.add(new Obj("1", "first"));
            objList.add(new Obj("2", "second"));
            objList.add(new Obj("3", "third"));
            objList.add(new Obj("4", "fourth"));
            objList.add(new Obj("5", "fifth"));
            int first = environment.getArgument("first");

            return new SimplePaginatedDataImpl<>(objList.subList(0, first), objList.size());
        }
    }
}
