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
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLTypeResolver;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.TypeResolver;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class GraphQLInputTest {

    public static class Resolver implements TypeResolver {

        @Override
        public GraphQLObjectType getType(TypeResolutionEnvironment env) {
            try {
                return GraphQLAnnotations.object(TestObject.class);
            } catch (GraphQLAnnotationsException e) {
                return null;
            }
        }
    }

    public static class SubInputObject {
        public SubInputObject(@GraphQLName("subKey") String subKey) {
            this.subKey = subKey;
        }

        @GraphQLField
        private String subKey;
    }

    public static class InputObject {
        public InputObject(@GraphQLName("key") String key, @GraphQLName("complex") List<SubInputObject> complex) {
            this.key = key;
            this.complex = complex;
        }

        @GraphQLField
        private String key;

        @GraphQLField
        private List<SubInputObject> complex;
    }

    public static class RecursiveInputObject {
        public RecursiveInputObject(@GraphQLName("map") HashMap map) {
            key = (String) map.get("key");
            if (map.containsKey("rec")) {
                rec = new RecursiveInputObject((HashMap) map.get("rec"));
            }
        }

        @GraphQLField
        private String key;

        @GraphQLField
        private RecursiveInputObject rec;
    }

    @GraphQLTypeResolver(Resolver.class)
    public interface TestIface {
        @GraphQLField
        String value(@GraphQLName("input") InputObject input);
    }

    public static class TestObject implements TestIface {

        @Override
        public String value(@GraphQLName("input") InputObject input) {
            return input.key + "a";
        }
    }

    public static class TestObjectList {
        @GraphQLField
        public String value(@GraphQLName("input") List<List<List<InputObject>>> input) {
            InputObject inputObject = input.get(0).get(0).get(0);
            return inputObject.key + "-" + inputObject.complex.get(0).subKey;
        }
    }

    public static class TestObjectRec {
        @GraphQLField
        public String value(@GraphQLName("input") RecursiveInputObject input) {
            return (input.rec != null ? ("rec" + input.rec.key) : input.key) + "a";
        }
    }

    public static class Code {
        public Code(@GraphQLName("map") HashMap map) {
            this.firstField = (String) map.get("firstField");
            this.secondField = (String) map.get("secondField");
        }

        @GraphQLField
        public String firstField;
        @GraphQLField
        public String secondField;
    }

    public static class QueryMultipleDefinitions {
        @GraphQLField
        public String something(@GraphQLName("code") Code code) {
            return code.firstField + code.secondField;
        }

        @GraphQLField
        public String somethingElse(@GraphQLName("code") Code code) {
            return code.firstField + code.secondField;
        }
    }

    public static class Query {
        @GraphQLField
        public TestIface object() {
            return new TestObject();
        }
    }

    public static class QueryRecursion {
        @GraphQLField
        public TestObjectRec object() {
            return new TestObjectRec();
        }

        ;
    }

    public static class QueryList {
        @GraphQLField
        public TestObjectList object() {
            return new TestObjectList();
        }

        ;
    }

    public static class QueryIface {
        @GraphQLField
        public TestObject iface() {
            return new TestObject();
        }
    }


    @Test
    public void query() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(Query.class))
                .additionalType(GraphQLAnnotations.object(TestObject.class)).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ object { value(input:{key:\"test\"}) } }", new Query());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("object").get("value"), "testa");
    }

    @Test
    public void queryMultipleDefinitions() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(QueryMultipleDefinitions.class)).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ something(code: {firstField:\"a\",secondField:\"b\"}) somethingElse(code: {firstField:\"c\",secondField:\"d\"}) }", new QueryMultipleDefinitions());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result.getData()).get("something"), "ab");
        assertEquals(((Map<String, String>) result.getData()).get("somethingElse"), "cd");
    }

    @Test
    public void queryWithRecursion() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(QueryRecursion.class)).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ object { value(input:{key:\"test\"}) } }", new QueryRecursion());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("object").get("value"), "testa");

        result = graphQL.execute("{ object { value(input:{rec:{key:\"test\"}}) } }", new QueryRecursion());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("object").get("value"), "rectesta");
    }

    @Test
    public void queryWithList() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(QueryList.class)).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ object { value(input:[[[{key:\"test\", complex:[{subKey:\"subtest\"},{subKey:\"subtest2\"}]}]]]) } }", new QueryList());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("object").get("value"), "test-subtest");
    }

    @Test
    public void queryWithInterface() {
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(QueryIface.class)).build(Collections.singleton(GraphQLAnnotations.object(TestObject.class)));

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute("{ iface { value(input:{key:\"test\"}) } }", new QueryIface());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("iface").get("value"), "testa");
    }

    @GraphQLName("hero")
    public static class Hero {
        @GraphQLField
        int a;
    }

    @GraphQLName("hero")
    public static class HeroInput {
        @GraphQLField
        String b;

        @GraphQLField
        Skill skill;
    }


    public static class Skill {
        @GraphQLField
        String c;
    }

    public static class QueryInputAndOutput {
        @GraphQLField
        public Hero getHero() {
            return null;
        }

        @GraphQLField
        public String getString(@GraphQLName("input") HeroInput input) {
            return "asdf";
        }

        // todo: if another method with input argument with type Hero and not HeroInput, it will consider HeroInput as its type because its defined before
        /*
        public String getString2(Hero input) {return "Asdfasdf";}
         */
    }


    public static class QueryInputAndOutput2 {
        @GraphQLField
        public String getA(@GraphQLName("skill") Skill skill) {
            return "asdfasdf";
        }

        @GraphQLField
        public Skill getSkill() {
            return null;
        }
    }


    @Test
    public void testInputAndOutputWithSameName() {
        // arrange + act
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(QueryInputAndOutput.class)).build();
        // assert
        assertEquals(schema.getQueryType().getFieldDefinition("getHero").getType().getName(), "hero");
        assertEquals(schema.getQueryType().getFieldDefinition("getString").getArgument("input").getType().getName(), "Inputhero");
        assertEquals(((GraphQLInputObjectType) schema.getQueryType().getFieldDefinition("getString")
                .getArgument("input").getType()).getField("skill").getType().getName(), "InputSkill");
    }

    @Test
    public void testInputAndOutputSameClass() {
        // arrange + act
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(QueryInputAndOutput2.class)).build();
        // assert
        assertEquals(schema.getQueryType().getFieldDefinition("getSkill").getType().getName(), "Skill");
        assertEquals(schema.getQueryType().getFieldDefinition("getA").getArgument("skill").getType().getName(), "InputSkill");
    }

    @GraphQLName("A")
    public static class AOut {
        @GraphQLField
        int out;
    }

    @GraphQLName("A")
    public static class AIn {
        @GraphQLField
        String in;

        @GraphQLField
        BIn bIn;
    }

    @GraphQLName("B")
    public static class BOut {
        @GraphQLField
        int out;
    }

    @GraphQLName("B")
    public static class BIn {
        @GraphQLField
        String in;
    }

    public static class QuerySameNameWithChildren {
        @GraphQLField
        public BOut getBout() {
            return null;
        }

        @GraphQLField
        public AOut getAout() {
            return null;
        }

        @GraphQLField
        public String getA(@GraphQLName("input") AIn input) {
            return "asdfa";
        }

        @GraphQLField
        public String getB(@GraphQLName("input") BIn input) {
            return "asdfasdf";
        }
    }

    @Test
    public void testInputAndOutputWithSameNameWithChildrenWithSameName() {
        // arrange + act
        GraphQLSchema schema = newSchema().query(GraphQLAnnotations.object(QuerySameNameWithChildren.class)).build();
        // assert
        assertEquals(schema.getQueryType().getFieldDefinition("getAout").getType().getName(), "A");
        assertEquals(schema.getQueryType().getFieldDefinition("getAout").getType().getClass(), GraphQLObjectType.class);
        assertEquals(schema.getQueryType().getFieldDefinition("getBout").getType().getName(), "B");
        assertEquals(schema.getQueryType().getFieldDefinition("getBout").getType().getClass(), GraphQLObjectType.class);
        assertEquals(schema.getQueryType().getFieldDefinition("getA").getArgument("input").getType().getName(), "InputA");
        assertEquals(schema.getQueryType().getFieldDefinition("getA").getArgument("input").getType().getClass(), GraphQLInputObjectType.class);
        assertEquals(schema.getQueryType().getFieldDefinition("getB").getArgument("input").getType().getClass(), GraphQLInputObjectType.class);

    }
}
