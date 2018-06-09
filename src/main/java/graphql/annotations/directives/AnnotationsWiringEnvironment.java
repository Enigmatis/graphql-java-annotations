package graphql.annotations.directives;

import graphql.schema.GraphQLDirective;

public interface AnnotationsWiringEnvironment<T> {
    /**
     * @return the runtime element in play
     */
    T getElement();

    /**
     * @return the directive that is being examined
     */
    GraphQLDirective getDirective();
}
