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

import java.util.List;
import java.util.Map;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@SuppressWarnings({"WeakerAccess", "unchecked", "AssertEqualsBetweenInconvertibleTypesTestNG"})
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
        assertThat(unionType, instanceOf(GraphQLUnionType.class));
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
        assertThat(unions.size(), is(2));
        assertThat(unions.get(0).getName(), is("hardwareComputer"));
        assertThat(unions.get(1).getName(), is("hardwareScreen"));
    }

    @Test
    public void unionQuery_returnTypeIsComputer_getComputer() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(Query.class)).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        String query = "{ hardwareComputer{ ... on Computer {name}, ... on Screen{resolution}} }";
        ExecutionResult result = graphQL.execute(query);
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("hardwareComputer").get("name"), "MyComputer");
    }

    @Test
    public void unionQuery_returnTypeIsScreen_getScreen() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(Query.class)).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        String query = "{ hardwareScreen{ ... on Computer {name}, ... on Screen{resolution}} }";
        ExecutionResult result = graphQL.execute(query);
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("hardwareScreen").get("resolution"), 10);
    }

    static class Screen implements Hardware {
        @GraphQLField
        int resolution;

    }

    @GraphQLUnion(possibleTypes = {Computer.class, Screen.class})
    interface Hardware {
    }

    // Hibernate class with same structure of API class
    public static class ComputerFetcher implements DataFetcher<ComputerDB> {
        ComputerDB computerDB = new ComputerDB("MyComputer");

        @Override
        public ComputerDB get(DataFetchingEnvironment environment) {
            return computerDB;
        }
    }

    public static class ScreenFetcher implements DataFetcher<ScreenDB> {
        ScreenDB screenDB = new ScreenDB(10);

        @Override
        public ScreenDB get(DataFetchingEnvironment environment) {
            return screenDB;
        }
    }

    static class ComputerDB {
        String name;

        public ComputerDB(String name) {
            this.name = name;
        }
    }

    static class ScreenDB {
        int resolution;

        public ScreenDB(int resolution) {
            this.resolution = resolution;
        }
    }

    class Query {
        @GraphQLField
        @GraphQLDataFetcher(ComputerFetcher.class)
        public Hardware getHardwareComputer() {
            return null;
        }

        @GraphQLField
        @GraphQLDataFetcher(ScreenFetcher.class)
        public Hardware getHardwareScreen() {
            return null;
        }
    }

    class Computer implements Hardware {
        @GraphQLField
        String name;
    }
}
