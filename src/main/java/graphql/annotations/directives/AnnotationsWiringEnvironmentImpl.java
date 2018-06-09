package graphql.annotations.directives;

import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLDirectiveContainer;

public class AnnotationsWiringEnvironmentImpl<T extends GraphQLDirectiveContainer> implements AnnotationsWiringEnvironment<T> {
    private final T element;
    private final GraphQLDirective directive;

    public AnnotationsWiringEnvironmentImpl(T element, GraphQLDirective directive) {
        this.element = element;
        this.directive = directive;
    }

    @Override
    public T getElement() {
        return element;
    }

    @Override
    public GraphQLDirective getDirective() {
        return directive;
    }
}
