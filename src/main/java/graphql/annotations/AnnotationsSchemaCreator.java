/**
 * Copyright 2016 Yurii Rashkovskii
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations;

import graphql.annotations.processor.GraphQLAnnotations;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.relay.Relay;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AnnotationsSchemaCreator {

    public static Builder newAnnotationsSchema() {
        return new Builder();
    }

    public static class Builder {
        private Class<?> queryObject;
        private Class<?> mutationObject;
        private Class<?> subscriptionObject;
        private Set<Class<?>> directivesObjectList = new HashSet<>();
        private Set<Class<?>> additionalTypesList = new HashSet<>();
        private GraphQLAnnotations graphQLAnnotations;
        private GraphQLSchema.Builder graphqlSchemaBuilder;

        public Builder setGraphQLSchemaBuilder(GraphQLSchema.Builder schemaBuilder) {
            this.graphqlSchemaBuilder = schemaBuilder;
            return this;
        }

        public Builder setAnnotationsProcessor(GraphQLAnnotations annotationsProcessor) {
            this.graphQLAnnotations = annotationsProcessor;
            return this;
        }

        public Builder query(Class<?> object) {
            this.queryObject = object;
            return this;
        }

        public Builder mutation(Class<?> mutationObject) {
            this.mutationObject = mutationObject;
            return this;
        }

        public Builder subscription(Class<?> subscriptionObject) {
            this.subscriptionObject = subscriptionObject;
            return this;
        }

        public Builder directives(Set<Class<?>> directivesObjectList) {
            this.directivesObjectList.addAll(directivesObjectList);
            return this;
        }

        public Builder directive(Class<?> directiveObject) {
            this.directivesObjectList.add(directiveObject);
            return this;
        }

        public Builder additionalType(Class<?> additionalObject) {
            this.additionalTypesList.add(additionalObject);
            return this;
        }

        public Builder additionalTypes(Set<Class<?>> additionalTypes) {
            this.additionalTypesList.addAll(additionalTypes);
            return this;
        }

        public Builder typeExtension(Class<?> typeExtension) {
            this.graphQLAnnotations.registerTypeExtension(typeExtension);
            return this;
        }

        public Builder typeFunction(TypeFunction typeFunction) {
            this.graphQLAnnotations.registerType(typeFunction);
            return this;
        }

        public Builder setAlwaysPrettify(Boolean shouldAlwaysPrettify) {
            this.graphQLAnnotations.getObjectHandler().getTypeRetriever().getGraphQLFieldRetriever().setAlwaysPrettify(shouldAlwaysPrettify);
            return this;
        }

        public Builder setRelay(Relay relay) {
            this.graphQLAnnotations.setRelay(relay);
            return this;
        }

        public GraphQLAnnotations getGraphQLAnnotations() {
            return this.graphQLAnnotations;
        }

        public GraphQLSchema build() {
            assert this.queryObject != null;

            if (this.graphQLAnnotations == null) {
                this.graphQLAnnotations = new GraphQLAnnotations();
            }

            if (this.graphqlSchemaBuilder == null) {
                this.graphqlSchemaBuilder = new GraphQLSchema.Builder();
            }

            Set<GraphQLDirective> directives = directivesObjectList.stream().map(dir -> graphQLAnnotations.directive(dir)).collect(Collectors.toSet());
            Set<GraphQLType> additionalTypes = additionalTypesList.stream().map(additionalType ->
                    additionalType.isInterface() ?
                            graphQLAnnotations.generateInterface(additionalType) : graphQLAnnotations.object(additionalType)).collect(Collectors.toSet());

            this.graphqlSchemaBuilder.query(graphQLAnnotations.object(queryObject));
            if (this.mutationObject != null) {
                this.graphqlSchemaBuilder.mutation(graphQLAnnotations.object(mutationObject));
            }
            if (this.subscriptionObject != null) {
                this.graphqlSchemaBuilder.subscription(graphQLAnnotations.object(subscriptionObject));
            }
            if (!this.directivesObjectList.isEmpty()) {
                graphqlSchemaBuilder.additionalDirectives(directives);
            }
            this.graphqlSchemaBuilder.additionalTypes(additionalTypes).additionalType(Relay.pageInfoType)
                    .codeRegistry(graphQLAnnotations.getContainer().getCodeRegistryBuilder().build());
            return this.graphqlSchemaBuilder.build();
        }
    }
}
