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
import graphql.annotations.annotationTypes.*;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.retrievers.GraphQLObjectHandler;
import graphql.schema.*;
import org.testng.annotations.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.*;

public class GraphQLExtensionsTest {

    @GraphQLDescription("TestObject object")
    @GraphQLName("TestObject")
    public static class TestObject {
        @GraphQLField
        public String field() {
            return "test";
        }

    }

    @GraphQLTypeExtension(GraphQLExtensionsTest.TestObject.class)
    public static class TestObjectExtension {
        private TestObject obj;

        public TestObjectExtension(TestObject obj) {
            this.obj = obj;
            this.field4 = obj.field() + " test4";
        }

        @GraphQLField
        public String field2() {
            return obj.field() + " test2";
        }

        @GraphQLDataFetcher(TestDataFetcher.class)
        @GraphQLField
        private String field3;

        @GraphQLField
        public String field4;

        @GraphQLField
        public String field5;

        public String getField5() {
            return obj.field() + " test5";
        }
    }

    @GraphQLTypeExtension(GraphQLExtensionsTest.TestObject.class)
    public static class TestObjectExtensionInvalid {
        private TestObject obj;

        public TestObjectExtensionInvalid(TestObject obj) {
            this.obj = obj;
        }

        @GraphQLField
        public String field() {
            return "invalid";
        }
    }

    public static class TestDataFetcher implements DataFetcher {
        @Override
        public Object get(DataFetchingEnvironment environment) {
            return ((TestObject) environment.getSource()).field() + " test3";
        }
    }

    @Test
    public void fields() {
        GraphQLAnnotations instance = new GraphQLAnnotations();
        instance.registerTypeExtension(TestObjectExtension.class);
        GraphQLObjectHandler graphQLObjectHandler = instance.getObjectHandler();
        GraphQLObjectType object = graphQLObjectHandler.getObject(GraphQLExtensionsTest.TestObject.class, instance.getContainer());

        List<GraphQLFieldDefinition> fields = object.getFieldDefinitions();
        assertEquals(fields.size(), 5);

        fields.sort(Comparator.comparing(GraphQLFieldDefinition::getName));

        assertEquals(fields.get(0).getName(), "field");
        assertEquals(fields.get(1).getName(), "field2");
        assertEquals(fields.get(1).getType(), GraphQLString);
        assertEquals(fields.get(2).getName(), "field3");
        assertEquals(fields.get(2).getType(), GraphQLString);
    }

    @Test
    public void values() {
        GraphQLAnnotations instance = new GraphQLAnnotations();
        instance.registerTypeExtension(TestObjectExtension.class);
        GraphQLObjectHandler graphQLObjectHandler = instance.getObjectHandler();
        GraphQLObjectType object = graphQLObjectHandler.getObject(GraphQLExtensionsTest.TestObject.class, instance.getContainer());

        GraphQLSchema schema = newSchema().query(object).build();
        GraphQLSchema schemaInherited = newSchema().query(object).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("{field field2 field3 field4 field5}", new GraphQLExtensionsTest.TestObject());
        Map<String, Object> data = result.getData();
        assertEquals(data.get("field"), "test");
        assertEquals(data.get("field2"), "test test2");
        assertEquals(data.get("field3"), "test test3");
        assertEquals(data.get("field4"), "test test4");
        assertEquals(data.get("field5"), "test test5");
    }

    @Test
    public void testDuplicateField() {
        GraphQLAnnotations instance = new GraphQLAnnotations();
        GraphQLObjectHandler graphQLObjectHandler = instance.getObjectHandler();
        instance.registerTypeExtension(TestObjectExtensionInvalid.class);
        GraphQLAnnotationsException e = expectThrows(GraphQLAnnotationsException.class, () -> graphQLObjectHandler.getObject(TestObject.class,instance.getContainer()));
        assertTrue(e.getMessage().startsWith("Duplicate field"));
    }
}
