package graphql.annotations;

import graphql.annotations.processor.GraphQLAnnotations;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        private List<Class<?>> additionalTypesList = new ArrayList<>();
        private GraphQLAnnotations graphQLAnnotations = new GraphQLAnnotations();

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

        public Builder directives(List<Class<?>> directivesObjectList) {
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

        public GraphQLSchema build() {
            assert this.queryObject != null;

            Set<GraphQLDirective> directives = directivesObjectList.stream().map(dir -> graphQLAnnotations.directive(dir)).collect(Collectors.toSet());
            Set<GraphQLType> additionalTypes = additionalTypesList.stream().map(x -> x.isInterface() ?
                    graphQLAnnotations.generateInterface(x) : graphQLAnnotations.object(x)).collect(Collectors.toSet());
            GraphQLSchema.Builder builder = newSchema();
            builder.query(graphQLAnnotations.object(queryObject));
            if (this.mutationObject != null) {
                builder.mutation(graphQLAnnotations.object(mutationObject));
            }
            if (this.subscriptionObject != null) {
                builder.subscription(graphQLAnnotations.object(subscriptionObject));
            }
            if (!this.directivesObjectList.isEmpty()) {
                builder.additionalDirectives(directives);
            }
            builder.additionalTypes(additionalTypes)
                    .codeRegistry(graphQLAnnotations.getContainer().getCodeRegistryBuilder().build());
            return builder.build();
        }
    }
}
