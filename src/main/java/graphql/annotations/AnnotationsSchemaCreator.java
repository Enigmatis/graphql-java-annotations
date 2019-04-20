package graphql.annotations;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static graphql.annotations.processor.GraphQLAnnotations.directive;
import static graphql.annotations.processor.GraphQLAnnotations.object;
import static graphql.schema.GraphQLCodeRegistry.newCodeRegistry;
import static graphql.schema.GraphQLSchema.newSchema;

public class AnnotationsSchemaCreator {

    public static Builder newAnnotationsSchema() {
        return new Builder();
    }

    public static class Builder {
        private Class<?> queryObject;
        private Class<?> mutationObject;
        private Class<?> subscriptionObject;
        private List<Class<?>> directivesObjectList = new ArrayList<>();
        private GraphQLCodeRegistry.Builder graphqlCodeRegistryBuilder = newCodeRegistry();

        public Builder queryObject(Class<?> object) {
            this.queryObject = object;
            return this;
        }

        public Builder mutationObject(Class<?> mutationObject) {
            this.mutationObject = mutationObject;
            return this;
        }

        public Builder subscriptionobject(Class<?> subscriptionObject) {
            this.subscriptionObject = subscriptionObject;
            return this;
        }

        public Builder directivesObjectList(List<Class<?>> directivesObjectList) {
            this.directivesObjectList.addAll(directivesObjectList);
            return this;
        }

        public Builder directiveObject(Class<?> directiveObject) {
            this.directivesObjectList.add(directiveObject);
            return this;
        }

        public GraphQLSchema build() {
            Set<GraphQLDirective> directives = directivesObjectList.stream().map(dir -> directive(dir)).collect(Collectors.toSet());
            GraphQLSchema.Builder builder = newSchema();
            builder.query(object(queryObject, graphqlCodeRegistryBuilder))
                    .mutation(object(mutationObject, graphqlCodeRegistryBuilder))
                    .codeRegistry(graphqlCodeRegistryBuilder.build());
            if (this.subscriptionObject != null) {
                builder.subscription(object(subscriptionObject, graphqlCodeRegistryBuilder));
            }
            if (!this.directivesObjectList.isEmpty()) {
                builder.additionalDirectives(directives);
            }
            return builder.build();
        }
    }
}
