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
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.TypeResolver;
import org.testng.annotations.Test;

import java.util.*;

import static org.testng.AssertJUnit.assertEquals;


public class GraphQLFragmentTest {

    static Map<String, GraphQLObjectType> registry;

    /**
     * Test a query which returns a list (RootObject.items) of two different classes (MyObject + MyObject2) which implement the same interface (MyInterface).
     */
    @Test
    public void testInterfaceInlineFragment() throws Exception {
        // Given
        registry = new HashMap<>();

        GraphQLInterfaceType iface = (GraphQLInterfaceType) GraphQLAnnotations.iface(MyInterface.class);

        GraphQLObjectType rootType = GraphQLAnnotations.object(RootObject.class);

        GraphQLObjectType objectType2 = GraphQLAnnotations.object(MyObject2.class);

        registry.put("MyObject2", objectType2);

        GraphQLObjectType objectType = GraphQLAnnotations.object(MyObject.class);

        registry.put("MyObject", objectType);

        GraphQLSchema schema = GraphQLSchema.newSchema()
                .query(rootType)
                .build(new HashSet(Arrays.asList(iface, rootType, objectType, objectType2)));

        GraphQL graphQL2 = new GraphQL(schema);

        // When
        ExecutionResult graphQLResult = graphQL2.execute("{items { ... on MyObject {a, my {b}} ... on MyObject2 {a, b}  }}", new RootObject());
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
    public static interface MyInterface {
        @GraphQLField
        public String getA();

        @GraphQLField
        public String getB();
    }

    public static class MyTypeResolver implements TypeResolver {

        @Override
        public GraphQLObjectType getType(Object object) {
            return registry.get(object.getClass().getSimpleName());
        }
    }


}