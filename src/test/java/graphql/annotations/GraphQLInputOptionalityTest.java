/**
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
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.schema.GraphQLSchema;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static graphql.annotations.AnnotationsSchemaCreator.newAnnotationsSchema;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class GraphQLInputOptionalityTest {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class SingleOptionalField {

        public SingleOptionalField(@GraphQLName("optionalField") Optional<String> one) {
            this.optionalField = one;
        }

        @GraphQLField
        public Optional<String> optionalField;

        public String toString() {
            return "SingleOptionalField{" +
                    "optionalField=" + optionalField +
                    '}';
        }
    }

    public static class QuerySingleOptionalField {

        @SuppressWarnings({"unused"})
        @GraphQLField
        public String getSingleOptionalField(@GraphQLName("field") SingleOptionalField field) {
            return field.toString();
        }
    }



    @Test
    public void testQueryWithSingleOptionalField() {
        String query =  "{ getSingleOptionalField(field: {optionalField:\"a\"}) }";
        runTest(new QuerySingleOptionalField(), query, "getSingleOptionalField", "SingleOptionalField{optionalField=Optional[a]}");
    }
    @Test
    public void testQueryWithSingleOptionalFieldUndefined() {
        String query =  "{ getSingleOptionalField(field: {}) }";
        runTest(new QuerySingleOptionalField(), query, "getSingleOptionalField", "SingleOptionalField{optionalField=null}");
    }
    @Test
    public void testQueryWithSingleOptionalFieldNull() {
        String query =  "{ getSingleOptionalField(field: {optionalField:null}) }";
        runTest(new QuerySingleOptionalField(), query, "getSingleOptionalField", "SingleOptionalField{optionalField=Optional.empty}");
    }



    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class OptionalAndRequiredFields {

        public OptionalAndRequiredFields(@GraphQLName("optionalField") Optional<String> one, @GraphQLName("requiredField") String two) {
            this.optionalField = one;
            this.requiredField = two;
        }

        @GraphQLField
        public Optional<String> optionalField;

        @GraphQLField
        private final String requiredField;

        public String toString() {
            return "OptionalAndRequiredFields{" +
                    "optionalField=" + optionalField +
                    ", requiredField=" + requiredField +
                    '}';
        }
    }

    public static class QueryOptionalAndRequiredFields {
        @SuppressWarnings({"unused"})
        @GraphQLField
        public String getOptionalAndRequiredFields(@GraphQLName("fields") OptionalAndRequiredFields fields) {
            return fields.toString();
        }
    }

    @Test
    public void testQueryWithRequiredField() {
        String query =  "{ getOptionalAndRequiredFields(fields: {requiredField:\"a\"}) }";
        runTest(new QueryOptionalAndRequiredFields(), query, "getOptionalAndRequiredFields", "OptionalAndRequiredFields{optionalField=null, requiredField=a}");
    }
    @Test
    public void testQueryWithRequiredFieldUndefined() {
        String query =  "{ getOptionalAndRequiredFields(fields: {}) }";
        runTest(new QueryOptionalAndRequiredFields(), query, "getOptionalAndRequiredFields", "OptionalAndRequiredFields{optionalField=null, requiredField=null}");
    }
    @Test
    public void testQueryWithRequiredFieldNull() {
        String query =  "{ getOptionalAndRequiredFields(fields: {requiredField:null}) }";
        runTest(new QueryOptionalAndRequiredFields(), query, "getOptionalAndRequiredFields", "OptionalAndRequiredFields{optionalField=null, requiredField=null}");
    }

    public static class QueryListOptionalAndRequiredFields {

        @SuppressWarnings({"unused", "OptionalAssignedToNull", "OptionalUsedAsFieldOrParameterType"})
        @GraphQLField
        public String getListOfOptionalAndRequiredFields(@GraphQLName("fieldsList") Optional<List<OptionalAndRequiredFields>> fieldsList) {
            return fieldsList == null ? "was null" : (fieldsList.map(list -> list.stream().collect(Collectors.toList()).toString()).orElse("was empty"));
        }
    }

    @Test
    public void testQueryListOptionalAndRequiredFields() {
        String query =  "{ getListOfOptionalAndRequiredFields }";
        runTest(new QueryListOptionalAndRequiredFields(), query, "getListOfOptionalAndRequiredFields", "was null");
    }
    @Test
    public void testQueryListOptionalAndRequiredFieldsNullInList() {
        String query =  "{ getListOfOptionalAndRequiredFields(fieldsList: [{optionalField:\"a\"}, null, {requiredField:\"b\"}, {}]) }";
        String expected = "[OptionalAndRequiredFields{optionalField=Optional[a], requiredField=null}, null, OptionalAndRequiredFields{optionalField=null, requiredField=b}, OptionalAndRequiredFields{optionalField=null, requiredField=null}]";
        runTest(new QueryListOptionalAndRequiredFields(), query, "getListOfOptionalAndRequiredFields", expected);
    }

    public static class QueryOptionalList {
        @SuppressWarnings({"unused", "OptionalAssignedToNull", "OptionalUsedAsFieldOrParameterType"})
        @GraphQLField
        public String list(@GraphQLName("options") Optional<List<String>> options) {
            return options == null ? "was null" : (options.map(anotherCodes -> anotherCodes.stream().reduce("", (a, b) -> a + b)).orElseThrow());
        }
    }

    @Test
    public void testQueryWithOptionalList() {
        String query =  "{ list(options: [\"a\", \"b\", \"c\"]) }";
        runTest(new QueryOptionalList(), query, "list", "abc");
    }

    @Test
    public void testQueryWithoutList() {
        String query =  "{ list }";
        runTest(new QueryOptionalList(), query, "list", "was null");
    }

    @Test
    public void testQueryWithEmptyList() {
        String query =  "{ list(options:[]) }";
        runTest(new QueryOptionalList(), query, "list", "");
        GraphQLSchema schema = newAnnotationsSchema().query(QueryOptionalList.class).build();
    }


    public static class OptionalListInConstructor{
        @SuppressWarnings({"OptionalUsedAsFieldOrParameterType"})
        @GraphQLField
        public Optional<List<String>> listOfStrings;

        @SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
        public OptionalListInConstructor(@GraphQLName("listOfStrings") Optional<List<String>> listOfStrings) {
            this.listOfStrings = listOfStrings;
        }
    }

    public static class QueryOptionalListInConstructor{
        @SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
        @GraphQLField
        public String getOptionalListInConstructor(@GraphQLName("listOfLists") Optional<List<OptionalListInConstructor>> listOfLists) {
            return listOfLists.map(listOfListUnwrapped -> listOfListUnwrapped.stream().map(list -> "{strings=" + list.listOfStrings + "}").reduce("", (a, b) -> a + b)).orElseThrow();
        }
    }

    @Test
    public void testQueryOptionalListInConstructor() {
        String query =  "{ getOptionalListInConstructor(listOfLists: [{listOfStrings: [\"a\", \"b\", \"c\"]}, {}, {listOfStrings: [\"d\"]}]) }";
        runTest(new QueryOptionalListInConstructor(), query, "getOptionalListInConstructor", "{strings=Optional[[a, b, c]]}{strings=null}{strings=Optional[[d]]}");
    }

    private void runTest(Object queryObject, String query, String field, String expected) {
        GraphQLSchema schema = newAnnotationsSchema().query(queryObject.getClass()).build();
        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = graphQL.execute(GraphQLHelper.createExecutionInput(query, queryObject ));
        assertTrue(result.getErrors().isEmpty(), result.getErrors().toString());
        assertEquals(((Map<String, String>) result.getData()).get(field), expected);
    }
}
