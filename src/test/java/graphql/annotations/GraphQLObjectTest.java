/**
 * Copyright 2016 Yurii Rashkovskii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldDataFetcher;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.PropertyDataFetcher;
import org.testng.annotations.Test;

import javax.validation.constraints.NotNull;
import java.lang.reflect.AnnotatedType;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class GraphQLObjectTest {

    public static class DefaultAValue implements Supplier<Object> {

        @Override
        public Object get() {
            return "default";
        }
    }

    @GraphQLDescription("TestObject object")
    @GraphQLName("TestObject")
    private static class TestObject {
        @GraphQLField
        @GraphQLName("field0")
        @GraphQLDescription("field")
        public
        @NotNull
        String field() {
            return "test";
        }

        @GraphQLField
        public String fieldWithArgs(@NotNull String a, @GraphQLDefaultValue(DefaultAValue.class) @GraphQLDescription("b") String b) {
            return b;
        }

        @GraphQLField
        public String fieldWithArgsAndEnvironment(DataFetchingEnvironment env, String a, String b) {
            return a;
        }

        @GraphQLField
        @Deprecated
        public String deprecated() {
            return null;
        }

        @GraphQLField
        @GraphQLDeprecate("Reason")
        public String deprecate() {
            return null;
        }

        @GraphQLField
        public String publicTest = "public";

        @GraphQLField
        private String privateTest = "private";

        public String getPrivateTest() {
            return privateTest;
        }

        public void setPrivateTest(String privateTest) {
            this.privateTest = privateTest;
        }

        @GraphQLNonNull
        @GraphQLField
        @GraphQLName("z_nonOptionalString")
        private String z;

        public String getZ() {
            return z;
        }

        public void setZ(String z) {
            this.z = z;
        }
    }

    private static class TestDefaults {
    }

    private static class TestObjectNamedArgs {
        @GraphQLField
        public String fieldWithNamedArgs(@GraphQLName("namedArg") String firstArgument) {
            return firstArgument;
        }
    }

    @Test
    public void namedFields() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestObjectNamedArgs.class);
        List<GraphQLFieldDefinition> fields = object.getFieldDefinitions();
        assertEquals(fields.size(), 1);

        List<GraphQLArgument> args = fields.get(0).getArguments();
        assertEquals(args.size(), 1);

        GraphQLArgument arg = args.get(0);
        assertEquals(arg.getName(), "namedArg");
    }

    @Test
    public void metainformation() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestObject.class);
        assertEquals(object.getName(), "TestObject");
        assertEquals(object.getDescription(), "TestObject object");
    }

    @Test
    public void objectClass() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestObject.class);
        assertTrue(object instanceof GraphQLObjectTypeWrapper);
        assertEquals(((GraphQLObjectTypeWrapper) object).getObjectClass(), TestObject.class);
    }

    @Test
    public void fields() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestObject.class);
        List<GraphQLFieldDefinition> fields = object.getFieldDefinitions();
        assertEquals(fields.size(), 8);

        fields.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));

        assertEquals(fields.get(2).getName(), "field0");
        assertEquals(fields.get(2).getDescription(), "field");
        assertTrue(fields.get(2).getType() instanceof graphql.schema.GraphQLNonNull);
        assertEquals(((graphql.schema.GraphQLNonNull) fields.get(2).getType()).getWrappedType(), GraphQLString);

        assertEquals(fields.get(3).getName(), "fieldWithArgs");
        List<GraphQLArgument> args = fields.get(3).getArguments();
        assertEquals(args.size(), 2);
        assertEquals(args.get(0).getName(), "a");
        assertTrue(args.get(0).getType() instanceof graphql.schema.GraphQLNonNull);
        assertEquals(((graphql.schema.GraphQLNonNull) args.get(0).getType()).getWrappedType(), GraphQLString);
        assertEquals(args.get(1).getName(), "b");
        assertEquals(args.get(1).getType(), GraphQLString);
        assertEquals(args.get(1).getDescription(), "b");

        assertEquals(fields.get(4).getName(), "fieldWithArgsAndEnvironment");
        args = fields.get(4).getArguments();
        assertEquals(args.size(), 2);

        assertEquals(fields.get(1).getName(), "deprecated");
        assertTrue(fields.get(1).isDeprecated());

        assertEquals(fields.get(0).getName(), "deprecate");
        assertTrue(fields.get(0).isDeprecated());
        assertEquals(fields.get(0).getDeprecationReason(), "Reason");

        assertEquals(fields.get(5).getName(), "privateTest");
        assertEquals(fields.get(6).getName(), "publicTest");

        assertEquals(fields.get(5).getDataFetcher().getClass(), PropertyDataFetcher.class);
        assertEquals(fields.get(6).getDataFetcher().getClass(), FieldDataFetcher.class);

        assertEquals(fields.get(7).getName(), "z_nonOptionalString");
        assertTrue(fields.get(7).getType() instanceof graphql.schema.GraphQLNonNull);
    }

    private static class TestObjectInherited extends TestObject {
        @Override
        @GraphQLName("field1") // Test overriding field
        public String field() {
            return "inherited";
        }
    }

    @Test
    public void methodInheritance() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestObject.class);
        GraphQLObjectType objectInherited = GraphQLAnnotations.object(TestObjectInherited.class);
        assertEquals(object.getFieldDefinitions().size(), objectInherited.getFieldDefinitions().size());

        GraphQLSchema schema = newSchema().query(object).build();
        GraphQLSchema schemaInherited = newSchema().query(objectInherited).build();

        ExecutionResult result = new GraphQL(schema).execute("{field0}", new TestObject());
        assertEquals(((Map<String, Object>) result.getData()).get("field0"), "test");
        result = new GraphQL(schemaInherited).execute("{field1}", new TestObjectInherited());
        assertEquals(((Map<String, Object>) result.getData()).get("field1"), "inherited");
    }

    private static class TestObjectBridgMethodParent<Type> {
        private final Type id;

        public TestObjectBridgMethodParent(Type id) {
            this.id = id;
        }

        public Type id() {
            return id;
        }
    }

    private static class TestObjectBridgMethod extends TestObjectBridgMethodParent<Long> {

        public TestObjectBridgMethod() {
            super(1L);
        }

        @Override
        @GraphQLField
        public Long id() {
            return super.id();
        }
    }

    @Test
    public void methodInheritanceWithGenerics() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestObjectBridgMethod.class);

        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = new GraphQL(schema).execute("{id}", new TestObjectBridgMethod());
        assertEquals(((Map<String, Object>) result.getData()).get("id"), 1L);
    }

    public interface Iface {
        @GraphQLField
        default String field() {
            return "field";
        }
    }

    public static class IfaceImpl implements Iface {
    }

    @Test
    public void interfaceInheritance() {
        GraphQLObjectType object = GraphQLAnnotations.object(IfaceImpl.class);
        assertEquals(object.getFieldDefinitions().size(), 1);
        assertEquals(object.getFieldDefinition("field").getType(), GraphQLString);

    }

    private static class TestAccessors {
        @GraphQLField
        public String getValue() {
            return "hello";
        }

        @GraphQLField
        public String setAnotherValue(String s) {
            return s;
        }
    }

    @Test
    public void accessors() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestAccessors.class);
        List<GraphQLFieldDefinition> fields = object.getFieldDefinitions();
        assertEquals(fields.size(), 2);
        fields.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));

        assertEquals(fields.get(1).getName(), "value");
        assertEquals(fields.get(0).getName(), "anotherValue");
    }


    @Test
    public void defaults() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestDefaults.class);
        assertEquals(object.getName(), "TestDefaults");
        assertNull(object.getDescription());
    }

    public static class TestField {
        @GraphQLField
        @GraphQLName("field1")
        public String field = "test";
    }

    public static class PrivateTestField {

        @GraphQLField
        @GraphQLName("field1")
        private String field = "test";

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        @GraphQLField
        private String field2 = "test";

        public String field2() {
            return field2;
        }

        public PrivateTestField sfield2(String field2) {
            this.field2 = field2;
            return this;
        }

        @GraphQLField
        private boolean booleanField = true;

        public boolean isBooleanField() {
            return booleanField;
        }

        public void setBooleanField(boolean booleanField) {
            this.booleanField = booleanField;
        }
    }

    @Test
    public void field() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestField.class);
        List<GraphQLFieldDefinition> fields = object.getFieldDefinitions();
        assertEquals(fields.size(), 1);
        assertEquals(fields.get(0).getName(), "field1");
    }

    private static class OnMethodTest {
        private String value;

        @GraphQLField
        public String getValue() {
            return value;
        }
    }

    @Test
    public void onMethod() {
        GraphQLObjectType object = GraphQLAnnotations.object(OnMethodTest.class);
        List<GraphQLFieldDefinition> fields = object.getFieldDefinitions();
        assertEquals(fields.size(), 1);
        assertEquals(fields.get(0).getName(), "value");
    }

    public static class TestFetcher implements DataFetcher {

        @Override
        public Object get(DataFetchingEnvironment environment) {
            return "test";
        }
    }

    private static class TestDataFetcher {

        @GraphQLField
        @GraphQLDataFetcher(TestFetcher.class)
        public String field;

        @GraphQLField
        @GraphQLDataFetcher(TestFetcher.class)
        public String someField() {
            return "not test";
        }

    }

    @Test
    public void dataFetcher() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestDataFetcher.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = new GraphQL(schema).execute("{field someField}", new TestObject());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result.getData()).get("field"), "test");
        assertEquals(((Map<String, String>) result.getData()).get("someField"), "test");
    }

    @Test
    public void query() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestObject.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = new GraphQL(schema).execute("{field0}", new TestObject());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result.getData()).get("field0"), "test");

        result = new GraphQL(schema).execute("{fieldWithArgs(a: \"test\", b: \"passed\")}", new TestObject());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result.getData()).get("fieldWithArgs"), "passed");

        result = new GraphQL(schema).execute("{fieldWithArgsAndEnvironment(a: \"test\", b: \"passed\")}", new TestObject());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result.getData()).get("fieldWithArgsAndEnvironment"), "test");

    }

    @Test
    public void queryField() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestField.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = new GraphQL(schema).execute("{field1}", new TestField());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result.getData()).get("field1"), "test");
    }

    @Test
    public void queryPrivateField() {
        GraphQLObjectType object = GraphQLAnnotations.object(PrivateTestField.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = new GraphQL(schema).execute("{field1, field2, booleanField}", new PrivateTestField());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result.getData()).get("field1"), "test");
        assertEquals(((Map<String, String>) result.getData()).get("field2"), "test");
        assertTrue(((Map<String, Boolean>) result.getData()).get("booleanField"));

    }

    @Test
    public void defaultArg() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestObject.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = new GraphQL(schema).execute("{fieldWithArgs(a: \"test\")}", new TestObject());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result.getData()).get("fieldWithArgs"), "default");
    }


    public static class Class1 {
        @GraphQLField
        public Class2 class2;
        @GraphQLField
        public String value;
    }

    public static class Class2 {
        @GraphQLField
        public Class1 class1;
        @GraphQLField
        public String value;
    }

    @Test
    public void recursiveTypes() {
        GraphQLObjectType object = GraphQLAnnotations.object(Class1.class);
        GraphQLSchema schema = newSchema().query(object).build();

        Class1 class1 = new Class1();
        Class2 class2 = new Class2();
        class1.class2 = class2;
        class2.class1 = class1;
        class2.value = "hello";
        class1.value = "bye";

        ExecutionResult result = new GraphQL(schema).execute("{ class2 { value } }", class1);
        assertTrue(result.getErrors().isEmpty());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(((Map<String, Object>) data.get("class2")).get("value"), "hello");

        result = new GraphQL(schema).execute("{ class2 { class1 { value } } }", class1);
        assertTrue(result.getErrors().isEmpty());
        data = (Map<String, Object>) result.getData();
        Map<String, Object> k1 = (Map<String, Object>) ((Map<String, Object>) data.get("class2")).get("class1");
        assertEquals(k1.get("value"), "bye");

        result = new GraphQL(schema).execute("{ class2 { class1 { class2 { value } } } }", class1);
        assertTrue(result.getErrors().isEmpty());
    }

    private static class TestCustomType {
        @GraphQLField
        public UUID id() {
            return UUID.randomUUID();
        }
    }

    @Test
    public void customType() {
        GraphQLAnnotations.register(new UUIDTypeFunction());
        GraphQLObjectType object = GraphQLAnnotations.object(TestCustomType.class);
        assertEquals(object.getFieldDefinition("id").getType(), GraphQLString);
    }

    private static class TestCustomTypeFunction {
        @GraphQLField
        @graphql.annotations.GraphQLType(UUIDTypeFunction.class)
        public UUID id() {
            return UUID.randomUUID();
        }
    }

    @Test
    public void customTypeFunction() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestCustomTypeFunction.class);
        assertEquals(object.getFieldDefinition("id").getType(), GraphQLString);
    }

    private static class TestInputArgument {
        @GraphQLField
        public String a;
        @GraphQLField
        public int b;

        public TestInputArgument(HashMap<String, Object> args) {
            a = (String) args.get("a");
            b = (int) args.get("b");
        }
    }

    private static class TestObjectInput {
        @GraphQLField
        public String test(int other, TestInputArgument arg) {
            return arg.a;
        }
    }

    @Test
    public void inputObjectArgument() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestObjectInput.class);
        GraphQLArgument argument = object.getFieldDefinition("test").getArgument("arg");
        assertTrue(argument.getType() instanceof GraphQLInputObjectType);
        assertEquals(argument.getName(), "arg");

        GraphQLSchema schema = newSchema().query(object).build();
        ExecutionResult result = new GraphQL(schema).execute("{ test(arg: { a:\"ok\", b:2 }, other:0) }", new TestObjectInput());
        assertTrue(result.getErrors().isEmpty());
        Map<String, Object> v = (Map<String, Object>) result.getData();
        assertEquals(v.get("test"), "ok");
    }

    @Test
    public void inputObject() {
        GraphQLObjectType object = GraphQLAnnotations.object(TestObjectInput.class);
        GraphQLInputObjectType inputObjectType = GraphQLAnnotations.inputObject(object);
        assertEquals(inputObjectType.getFields().size(), object.getFieldDefinitions().size());
    }

    public static class UUIDTypeFunction implements TypeFunction {
        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            return GraphQLString;
        }

        @Override
        public Collection<Class<?>> getAcceptedTypes() {
            return Collections.singletonList(UUID.class);
        }
    }

    public static class OptionalTest {
        @GraphQLField
        public Optional<String> empty = Optional.empty();
        @GraphQLField
        public Optional<String> nonempty = Optional.of("test");

        public OptionalTest() {
        }

        public OptionalTest(Optional<String> empty, Optional<String> nonempty) {
            this.empty = empty;
            this.nonempty = nonempty;
        }

        @Override
        public String toString() {
            return "OptionalTest" +
                    "{empty=" + empty +
                    ", nonempty=" + nonempty +
                    '}';
        }
    }

    @Test
    public void queryOptional() {
        GraphQLObjectType object = GraphQLAnnotations.object(OptionalTest.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = new GraphQL(schema, new EnhancedExecutionStrategy()).execute("{empty, nonempty}", new OptionalTest());
        assertTrue(result.getErrors().isEmpty());
        Map<String, Object> v = (Map<String, Object>) result.getData();
        assertNull(v.get("empty"));
        assertEquals(v.get("nonempty"), "test");
    }

    @Test
    public void optionalInput() {
        GraphQLObjectType object = GraphQLAnnotations.object(OptionalTest.class);
        GraphQLInputObjectType inputObject = GraphQLAnnotations.inputObject(object);
        GraphQLObjectType mutation = GraphQLObjectType.newObject().name("mut").field(newFieldDefinition().name("test").type(object).
                argument(GraphQLArgument.newArgument().type(inputObject).name("input").build()).dataFetcher(environment -> {
            Map<String, String> input = environment.getArgument("input");
            return new OptionalTest(Optional.ofNullable(input.get("empty")), Optional.ofNullable(input.get("nonempty")));
        }).build()).build();
        GraphQLSchema schema = newSchema().query(object).mutation(mutation).build();

        ExecutionResult result = new GraphQL(schema, new EnhancedExecutionStrategy()).execute("mutation {test(input: {empty: \"test\"}) { empty nonempty } }", new OptionalTest());
        assertTrue(result.getErrors().isEmpty());
        Map<String, Object> v = (Map<String, Object>) ((Map<String, Object>) result.getData()).get("test");
        assertEquals(v.get("empty"), "test");
        assertNull(v.get("nonempty"));
    }

    public static class EnumTest {
        public enum E {A, B}

        @GraphQLField
        public E e;

        public EnumTest() {
        }

        public EnumTest(E e) {
            this.e = e;
        }

        @Override
        public String toString() {
            return "EnumTest{" + "e=" + e + '}';
        }
    }

    @Test
    public void queryEnum() {
        GraphQLObjectType object = GraphQLAnnotations.object(EnumTest.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = new GraphQL(schema, new EnhancedExecutionStrategy()).execute("{e}", new EnumTest(EnumTest.E.B));
        assertTrue(result.getErrors().isEmpty());
        Map<String, Object> v = (Map<String, Object>) result.getData();
        assertEquals(v.get("e"), "B");
    }

    public static class ParametrizedArgsTest {
        @GraphQLField
        public String first(List<String> l) {
            return l.get(0);
        }
    }

    @Test
    public void parametrizedArg() {
        GraphQLObjectType object = GraphQLAnnotations.object(ParametrizedArgsTest.class);
        GraphQLInputType t = object.getFieldDefinition("first").getArguments().get(0).getType();
        assertTrue(t instanceof GraphQLList);
        assertEquals(((GraphQLList) t).getWrappedType(), Scalars.GraphQLString);
    }

    @GraphQLField
    public static class InheritGraphQLFieldTest {
        public String inheritedOn;

        @GraphQLField(false)
        public String forcedOff;

        public String getOn() {
            return "on";
        }

        @GraphQLField(false)
        public String getOff() {
            return "off";
        }
    }

    @Test
    public void inheritGraphQLField() {
        GraphQLObjectType object = GraphQLAnnotations.object(InheritGraphQLFieldTest.class);
        assertNotNull(object.getFieldDefinition("on"));
        assertNull(object.getFieldDefinition("off"));
        assertNotNull(object.getFieldDefinition("inheritedOn"));
        assertNull(object.getFieldDefinition("forcedOff"));
    }

}
