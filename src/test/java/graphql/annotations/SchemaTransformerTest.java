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

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLInvokeDetached;
import graphql.schema.*;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import org.testng.annotations.Test;

import static graphql.annotations.AnnotationsSchemaCreator.newAnnotationsSchema;
import static graphql.util.TreeTransformerUtil.changeNode;

public class SchemaTransformerTest {

    class A {
        @GraphQLField
        String x;
    }

    class Query {
        @GraphQLField
        @GraphQLInvokeDetached
        public A getA() {
            return new A();
        }
    }

    class Visitor implements GraphQLTypeVisitor {
        @Override
        public TraversalControl visitGraphQLArgument(GraphQLArgument node, TraverserContext<GraphQLSchemaElement> context) {
            return TraversalControl.CONTINUE;
        }

        @Override
        public TraversalControl visitGraphQLInterfaceType(GraphQLInterfaceType node, TraverserContext<GraphQLSchemaElement> context) {
            return TraversalControl.CONTINUE;
        }

        @Override
        public TraversalControl visitGraphQLEnumType(GraphQLEnumType node, TraverserContext<GraphQLSchemaElement> context) {
            return TraversalControl.CONTINUE;
        }

        @Override
        public TraversalControl visitGraphQLEnumValueDefinition(GraphQLEnumValueDefinition node, TraverserContext<GraphQLSchemaElement> context) {
            return TraversalControl.CONTINUE;
        }

        @Override
        public TraversalControl visitGraphQLFieldDefinition(GraphQLFieldDefinition node, TraverserContext<GraphQLSchemaElement> context) {
            if (node.getName().equals("x")) {
                GraphQLFieldDefinition y = node.transform(builder -> builder.name("y"));
                return changeNode(context, y);
            }
            return TraversalControl.CONTINUE;
        }

        @Override
        public TraversalControl visitGraphQLDirective(GraphQLDirective node, TraverserContext<GraphQLSchemaElement> context) {
            return TraversalControl.CONTINUE;
        }

        @Override
        public TraversalControl visitGraphQLInputObjectField(GraphQLInputObjectField node, TraverserContext<GraphQLSchemaElement> context) {
            return TraversalControl.CONTINUE;
        }

        @Override
        public TraversalControl visitGraphQLInputObjectType(GraphQLInputObjectType node, TraverserContext<GraphQLSchemaElement> context) {
            return TraversalControl.CONTINUE;
        }

        @Override
        public TraversalControl visitGraphQLList(GraphQLList node, TraverserContext<GraphQLSchemaElement> context) {
            return TraversalControl.CONTINUE;
        }

        @Override
        public TraversalControl visitGraphQLNonNull(GraphQLNonNull node, TraverserContext<GraphQLSchemaElement> context) {
            return TraversalControl.CONTINUE;
        }

        @Override
        public TraversalControl visitGraphQLObjectType(GraphQLObjectType node, TraverserContext<GraphQLSchemaElement> context) {
            return TraversalControl.CONTINUE;
        }

        @Override
        public TraversalControl visitGraphQLScalarType(GraphQLScalarType node, TraverserContext<GraphQLSchemaElement> context) {
            return TraversalControl.CONTINUE;
        }

        @Override
        public TraversalControl visitGraphQLTypeReference(GraphQLTypeReference node, TraverserContext<GraphQLSchemaElement> context) {
            return TraversalControl.CONTINUE;
        }

        @Override
        public TraversalControl visitGraphQLUnionType(GraphQLUnionType node, TraverserContext<GraphQLSchemaElement> context) {
            return TraversalControl.CONTINUE;
        }
    }

    @Test
    public void test() {
        GraphQLSchema schema = newAnnotationsSchema().query(Query.class).build();
        SchemaTransformer schemaTransformer = new SchemaTransformer();
        GraphQLSchema transformedSchema = schemaTransformer.transform(schema, new Visitor());
        System.out.println(transformedSchema.toString());
    }
}
