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
import graphql.TypeResolutionEnvironment;
import graphql.annotations.annotationTypes.*;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.annotations.processor.retrievers.GraphQLInterfaceRetriever;
import graphql.annotations.typeResolvers.UnionTypeResolver;
import graphql.schema.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
        assertThat(unions.size(), is(3));
    }

    @Test
    public void unionQuery_returnTypeIsComputer_getComputer() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(Query.class)).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        String query = "{ getHardwareComputer{ ... on Computer {name}, ... on Screen{resolution}} }";
        ExecutionResult result = graphQL.execute(query);
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("getHardwareComputer").get("name"), "MyComputer");
    }

    @Test
    public void unionQuery_returnTypeIsScreen_getScreen() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(Query.class)).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        String query = "{ getHardwareScreen{ ... on Computer {name}, ... on Screen{resolution}} }";
        ExecutionResult result = graphQL.execute(query);
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("getHardwareScreen").get("resolution"), 10);
    }

    @Test
    public void unionQueryWithCustomTypeResolver_askForDog_getDog() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(Query.class)).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        String query = "{ getPet(kindOfPet:\"dog\"){ ... on Cat {mew}, ... on Dog{waf}} }";
        ExecutionResult result = graphQL.execute(query);
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("getPet").get("waf"), "waf");
    }

    @Test
    public void unionQueryWithCustomTypeResolver_askForCat_getCat() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(Query.class)).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        String query = "{ getPet(kindOfPet:\"cat\"){ ... on Cat {mew}, ... on Dog{waf}} }";
        ExecutionResult result = graphQL.execute(query);
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("getPet").get("mew"), "mew");
    }

    static class Screen implements Hardware {
        @GraphQLField
        int resolution;

        public Screen(int resolution) {
            this.resolution = resolution;
        }
    }

    @GraphQLUnion(possibleTypes = {Computer.class, Screen.class})
    interface Hardware {
    }

    // Hibernate class with same structure of API class
    public static class ComputerFetcher implements DataFetcher<Computer> {
        Computer computerDB = new Computer("MyComputer");

        @Override
        public Computer get(DataFetchingEnvironment environment) {
            return computerDB;
        }
    }

    public static class ScreenFetcher implements DataFetcher<Screen> {
        Screen screenDB = new Screen(10);

        @Override
        public Screen get(DataFetchingEnvironment environment) {
            return screenDB;
        }
    }

    class Query {

        @GraphQLField
        @GraphQLDataFetcher(ComputerFetcher.class)
        public Hardware getHardwareComputer;
        @GraphQLField
        @GraphQLDataFetcher(ScreenFetcher.class)
        public Hardware getHardwareScreen;

        @GraphQLField
        @GraphQLDataFetcher(PetDataFetcher.class)
        public Pet getPet(@GraphQLName("kindOfPet") @GraphQLNonNull String kindOfPet){return null;}

    }
    static class Computer implements Hardware {

        @GraphQLField
        String name;
        public Computer(String name) {
            this.name = name;
        }

    }
    @GraphQLUnion(typeResolver = PetResolver.class, possibleTypes = {Cat.class, Dog.class})
    interface Pet {
    }

    static class Cat implements Pet{
        @GraphQLField
        String mew;

        public Cat(String mew) {
            this.mew = mew;
        }
    }

    static class Dog implements Pet{
        @GraphQLField
        String waf;

        public Dog(String waf) {
            this.waf = waf;
        }
    }

    public static class PetResolver implements TypeResolver {
        @Override
        public GraphQLObjectType getType(TypeResolutionEnvironment env) {
            Object object = env.getObject();
            if(object instanceof Dog) {
                return env.getSchema().getObjectType("Dog");
            }
            else {
                return env.getSchema().getObjectType("Cat");
            }
        }
    }

    public static class PetDataFetcher implements DataFetcher<Pet> {
        @Override
        public Pet get(DataFetchingEnvironment environment) {
            String nameOfPet = environment.getArgument("kindOfPet");
            if(nameOfPet.toLowerCase().equals("dog")) {
                return new Dog("waf");
            }
            else {
                return new Cat("mew");
            }
        }
    }
}
