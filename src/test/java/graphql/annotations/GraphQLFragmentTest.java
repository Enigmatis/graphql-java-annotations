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
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLTypeResolver;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.TypeResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

import static graphql.annotations.AnnotationsSchemaCreator.newAnnotationsSchema;
import static org.testng.AssertJUnit.assertEquals;


public class GraphQLFragmentTest {

    private GraphQLAnnotations graphQLAnnotations;

    @BeforeMethod
    public void init() {
        this.graphQLAnnotations = new GraphQLAnnotations();
    }

    static Map<String, GraphQLObjectType> registry;

    /**
     * Test a query which returns a list (RootObject.items) of two different classes (MyObject + MyObject2) which implement the same interface (MyInterface).
     */
    @Test
    public void testInterfaceInlineFragment() throws Exception {
        // Given
        registry = new HashMap<>();

        GraphQLObjectType objectType2 = this.graphQLAnnotations.object(MyObject2.class);

        registry.put("MyObject2", objectType2);

        GraphQLObjectType objectType = this.graphQLAnnotations.object(MyObject.class);

        registry.put("MyObject", objectType);


        GraphQLSchema schema = newAnnotationsSchema().query(RootObject.class).additionalType(MyObject.class).additionalType(MyInterface.class).build();

        GraphQL graphQL2 = GraphQL.newGraphQL(schema).build();

        // When
        ExecutionResult graphQLResult = graphQL2.execute(GraphQLHelper.createExecutionInput("{getItems { ... on MyObject {getA, getMy {getB}} ... on MyObject2 {getA, getB}  }}", new RootObject()));
        Set resultMap = ((Map) graphQLResult.getData()).entrySet();

        // Then
        assertEquals(graphQLResult.getErrors().size(), 0);
        assertEquals(resultMap.size(), 1);
    }

    public static class RootObject {
        @GraphQLField
        public List<MyInterface> getItems() {
            return Arrays.asList(new MyObject(), new MyObject2());
        }
    }

    public static class MyObject implements MyInterface {
        public String getA() {
            return "a1";
        }

        public String getB() {
            return "b1";
        }

        @GraphQLField
        public MyObject2 getMy() {
            return new MyObject2();
        }
    }

    public static class MyObject2 implements MyInterface {
        public String getA() {
            return "a2";
        }

        public String getB() {
            return "b2";
        }
    }

    @GraphQLTypeResolver(value = MyTypeResolver.class)
    public interface MyInterface {
        @GraphQLField
        String getA();

        @GraphQLField
        String getB();
    }

    public static class MyTypeResolver implements TypeResolver {

        @Override
        public GraphQLObjectType getType(TypeResolutionEnvironment env) {
            return registry.get(env.getObject().getClass().getSimpleName());
        }
    }


}