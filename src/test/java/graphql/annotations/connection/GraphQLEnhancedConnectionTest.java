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
package graphql.annotations.connection;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.connection.exceptions.GraphQLConnectionException;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

import static graphql.annotations.AnnotationsSchemaCreator.newAnnotationsSchema;
import static org.testng.Assert.assertEquals;

@SuppressWarnings("ALL")
public class GraphQLEnhancedConnectionTest {

    private GraphQL graphQL;
    private GraphQLAnnotations graphQLAnnotations;


    @BeforeMethod
    public void setUp() throws Exception {
        this.graphQLAnnotations = new GraphQLAnnotations();
        GraphQLSchema schema = newAnnotationsSchema().query(TestListField.class).build();
        graphQL = GraphQL.newGraphQL(schema).build();
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

        public String getId() {
            return id;
        }

        public String getVal() {
            return val;
        }
    }

    public static class TestListField {
        @GraphQLField
        @GraphQLConnection(connectionFetcher = PaginatedDataConnectionFetcher.class)
        @GraphQLDataFetcher(GoodConnectionDataFetcher.class)
        public PaginatedData<Obj> objs;

        @GraphQLField
        @GraphQLConnection(connectionFetcher = PaginatedDataConnectionFetcher.class, async = true)
        @GraphQLDataFetcher(GoodConnectionDataFetcher.class)
        public PaginatedData<Obj> objsAsync;

        public TestListField(PaginatedData<Obj> objs) {
            this.objs = objs;
        }
    }

    public static class GoodConnectionDataFetcher implements DataFetcher<PaginatedData<Obj>> {

        @Override
        public PaginatedData<Obj> get(DataFetchingEnvironment environment) {

            Integer first = environment.getArgument("first");
            List<Obj> objs = Arrays.asList(new Obj("1", "1"), new Obj("2", "2"), new Obj("3", "3"));
            if (first != null && first <= 3) {
                objs = objs.subList(0, first);
            }
            return new AbstractPaginatedData<Obj>(false, true, objs) {
                @Override
                public String getCursor(Obj entity) {
                    return entity.getId();
                }
            };
        }

    }

public static class NotValidConnectionField {
    @GraphQLField
    @GraphQLConnection
    @GraphQLDataFetcher(NotGoodDataFetcher.class)
    public List<GraphQLConnectionTest.Obj> objs;

    public NotValidConnectionField(List<GraphQLConnectionTest.Obj> objs) {
        this.objs = objs;
    }
}

public static class NotGoodDataFetcher implements DataFetcher<List<Obj>> {

    @Override
    public List<Obj> get(DataFetchingEnvironment environment) {
        return Collections.emptyList();
    }

}


    @Test(expectedExceptions = GraphQLConnectionException.class)
    public void ConnectionFieldDoesntReturnPaginatedData_tryToBuildSchema_getException() throws Exception {
        //Act + Assert
        this.graphQLAnnotations.object(NotValidConnectionField.class);
    }

    @Test
    public void validDatafetcher_queryForCursors_getValidCursors() throws Exception {
        //Arrange
        ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("{ objs(first:2) { edges { cursor } } }").build();
        //Act
        ExecutionResult result = graphQL.execute(executionInput);
        Map<String, Map<String, List<Map<String, Map<String, Object>>>>> data = result.getData();
        List<Map<String, Map<String, Object>>> edges = data.get("objs").get("edges");

        //Assert
        assertEquals(edges.get(0).get("cursor"), "1");
        assertEquals(edges.get(1).get("cursor"), "2");
    }

    @Test
    public void fetchConnectionAsync() throws Exception {
        //Arrange
        ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("{ objsAsync(first:2) { edges { cursor } } }").build();
        //Act
        ExecutionResult result = graphQL.execute(executionInput);
        Map<String, Map<String, List<Map<String, Map<String, Object>>>>> data = result.getData();
        List<Map<String, Map<String, Object>>> edges = data.get("objsAsync").get("edges");

        //Assert
        assertEquals(edges.get(0).get("cursor"), "1");
        assertEquals(edges.get(1).get("cursor"), "2");
    }

    @Test
    public void validDatafetcher_queryForValues_returnsValidValues() throws Exception {
        //Arrange
        ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("{ objs(first:2) { edges { cursor node { id, val } } } }")
                .context("CONTEXT").root(null).operationName(null).variables(new HashMap<>())
                .build();

        //Act
        ExecutionResult result = graphQL.execute(executionInput);
        Map<String, Map<String, List<Map<String, Map<String, Object>>>>> data = result.getData();
        List<Map<String, Map<String, Object>>> edges = data.get("objs").get("edges");

        //Assert
        assertEquals(edges.get(0).get("node").get("val"), "1");
        assertEquals(edges.get(1).get("node").get("val"), "2");
    }

    @Test
    public void validDatafetcher_queryForHasPreviousPage_returnsFalse() throws Exception {

        //Arrange
        ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("{ objs(first:2) { pageInfo { hasPreviousPage } } }").build();

        //Act
        ExecutionResult result = graphQL.execute(executionInput);
        Map<String, Map<String, Map<String, Map<String, Object>>>> data = result.getData();

        //Assert
        assertEquals(data.get("objs").get("pageInfo").get("hasPreviousPage"), false);
    }

    @Test
    public void validDatafetcher_queryForHasNextPage_returnsTrue() throws Exception {

        //Arrange
        ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("{ objs(first:2) { pageInfo { hasNextPage } } }").build();

        //Act
        ExecutionResult result = graphQL.execute(executionInput);
        Map<String, Map<String, Map<String, Map<String, Object>>>> data = result.getData();

        //Assert
        assertEquals(data.get("objs").get("pageInfo").get("hasNextPage"), true);
    }
}
