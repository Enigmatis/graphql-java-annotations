package graphql.annotations.processor;

import graphql.annotations.directives.AnnotationsDirectiveWiring;
import graphql.schema.GraphQLDirective;

public class DirectiveAndWiring {
    private GraphQLDirective directive;
    private Class<? extends AnnotationsDirectiveWiring> wiringClass;

    public DirectiveAndWiring(GraphQLDirective directive, Class<? extends AnnotationsDirectiveWiring> wiringClass) {
        this.directive = directive;
        this.wiringClass = wiringClass;
    }

    public GraphQLDirective getDirective() {
        return directive;
    }

    public Class<? extends AnnotationsDirectiveWiring> getWiringClass() {
        return wiringClass;
    }
}
