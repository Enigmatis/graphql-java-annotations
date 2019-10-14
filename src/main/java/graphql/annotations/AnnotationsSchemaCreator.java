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
        private Set<Class<?>> directiveContainerClasses = new HashSet<>();
        private Set<Class<?>> additionalTypesList = new HashSet<>();
        private Set<Class<?>> typeExtensions = new HashSet<>();
        private Set<TypeFunction> typeFunctions = new HashSet<>();
        private Boolean shouldAlwaysPrettify = null;
        private GraphQLAnnotations graphQLAnnotations;
        private GraphQLSchema.Builder graphqlSchemaBuilder;

        /**
         * You can set your own schema builder, but its optional
         *
         * @param schemaBuilder a graphql schema builder
         * @return the builder after setting the schema builder
         */
        public Builder setGraphQLSchemaBuilder(GraphQLSchema.Builder schemaBuilder) {
            this.graphqlSchemaBuilder = schemaBuilder;
            return this;
        }

        /**
         * You can set your own annotations processor
         *
         * @param annotationsProcessor the annotations processor which creates the GraphQLTypes
         * @return the builder after setting the annotations processor
         */
        public Builder setAnnotationsProcessor(GraphQLAnnotations annotationsProcessor) {
            this.graphQLAnnotations = annotationsProcessor;
            return this;
        }

        /**
         * Set the Query of the graphql schema
         * This method will generate a GraphQL Query type out of your java class using the annotations processor
         *
         * @param queryClass the Query java class
         * @return the builder after setting the query
         */
        public Builder query(Class<?> queryClass) {
            this.queryObject = queryClass;
            return this;
        }

        /**
         * Set the Mutation of the graphql schema
         * This method will generate a GraphQL Mutation type out of your java class using the annotations processor
         *
         * @param mutationClass the Mutation java class
         * @return the builder after setting the mutation
         */
        public Builder mutation(Class<?> mutationClass) {
            this.mutationObject = mutationClass;
            return this;
        }

        /**
         * Set the Subscription of the graphql schema
         * This method will generate a GraphQL Subscription type out of your java class using the annotations processor
         *
         * @param subscriptionClass the Subscription java class
         * @return the builder after setting the subscription
         */
        public Builder subscription(Class<?> subscriptionClass) {
            this.subscriptionObject = subscriptionClass;
            return this;
        }

        /**
         * Set the directives of the graphql schema
         * This method will generate a GraphQL Directive type out of your java classes using the annotations processor
         *
         * @param directiveClasses a set of directive classes
         * @return the builder after setting the directives
         */
        public Builder directives(Set<Class<?>> directiveClasses) {
            this.directivesObjectList.addAll(directiveClasses);
            return this;
        }

        /**
         * Add directive declaration class to create directives for the graphql schema
         * @param directiveContainerClass a directive container class (directives are defined as methods inside the class)
         * @return the builder after adding the directive container class to the list of directive container classes
         */
        public Builder directives(Class<?> directiveContainerClass){
            this.directiveContainerClasses.add(directiveContainerClass);
            return this;
        }

        /**
         * Add a directive to the graphql schema
         * This method will generate a GraphQL Directive type out of your java class using the annotations processor
         *
         * @param directiveClass a Directive java class
         * @return the builder after adding the directive
         */
        public Builder directive(Class<?> directiveClass) {
            this.directivesObjectList.add(directiveClass);
            return this;
        }

        /**
         * Add an additional type to the additional type list
         *
         * @param additionalTypeClass an additional type class
         * @return the builder after adding an additional type
         */
        public Builder additionalType(Class<?> additionalTypeClass) {
            this.additionalTypesList.add(additionalTypeClass);
            return this;
        }

        /**
         * Add a set of additional types to the additional type lise
         *
         * @param additionalTypes a set of additional type classes
         * @return the builder after adding the additional types
         */
        public Builder additionalTypes(Set<Class<?>> additionalTypes) {
            this.additionalTypesList.addAll(additionalTypes);
            return this;
        }

        /**
         * Register a type extensions to the graphql processor
         *
         * @param typeExtension a type extension class
         * @return the builder after registering the type extension in the graphql processor
         */
        public Builder typeExtension(Class<?> typeExtension) {
            this.typeExtensions.add(typeExtension);
            return this;
        }

        /**
         * Register a type function to the graphql processor
         *
         * @param typeFunction a type function
         * @return the builder after registering the type function in the graphql processor
         */
        public Builder typeFunction(TypeFunction typeFunction) {
            this.typeFunctions.add(typeFunction);
            return this;
        }

        /**
         * Set the always prettify property of the graphql annotations processor (whether or not to prettify the graphql names)
         *
         * @param shouldAlwaysPrettify a boolean flag
         * @return the builder after setting the property
         */
        public Builder setAlwaysPrettify(Boolean shouldAlwaysPrettify) {
            this.shouldAlwaysPrettify = shouldAlwaysPrettify;
            return this;
        }

        /**
         * Set the relay object in the graphql annotations processor
         *
         * @param relay a relay object
         * @return the builder after setting the relay object
         */
        public Builder setRelay(Relay relay) {
            this.graphQLAnnotations.setRelay(relay);
            return this;
        }

        /**
         * @return the graphql annotations processor
         */
        public GraphQLAnnotations getGraphQLAnnotations() {
            return this.graphQLAnnotations;
        }

        /**
         * Build a graphql schema according to the properties provided
         * The method generates the GraphQL objects, directives, additional types, etc using the graphql annotations processor and sets them into the GraphQL Schema
         *
         * @return a GraphQLSchema which contains generated GraphQL types out of the properties provided to the builder
         */
        public GraphQLSchema build() {
            assert this.queryObject != null;

            if (this.graphQLAnnotations == null) {
                this.graphQLAnnotations = new GraphQLAnnotations();
            }

            if (this.graphqlSchemaBuilder == null) {
                this.graphqlSchemaBuilder = new GraphQLSchema.Builder();
            }

            this.typeExtensions.forEach(typeExtension -> this.graphQLAnnotations.registerTypeExtension(typeExtension));
            this.typeFunctions.forEach(typeFunction -> this.graphQLAnnotations.registerTypeFunction(typeFunction));

            if (this.shouldAlwaysPrettify != null) {
                this.graphQLAnnotations.getObjectHandler().getTypeRetriever().getGraphQLFieldRetriever().setAlwaysPrettify(this.shouldAlwaysPrettify);
            }

            Set<GraphQLDirective> directives = directivesObjectList.stream().map(dir -> graphQLAnnotations.directive(dir)).collect(Collectors.toSet());
            directiveContainerClasses.forEach(dir->directives.addAll(graphQLAnnotations.directives(dir)));

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
            if (!directives.isEmpty()) {
                graphqlSchemaBuilder.additionalDirectives(directives);
            }
            this.graphqlSchemaBuilder.additionalTypes(additionalTypes).additionalType(Relay.pageInfoType)
                    .codeRegistry(graphQLAnnotations.getContainer().getCodeRegistryBuilder().build());
            return this.graphqlSchemaBuilder.build();
        }
    }
}
