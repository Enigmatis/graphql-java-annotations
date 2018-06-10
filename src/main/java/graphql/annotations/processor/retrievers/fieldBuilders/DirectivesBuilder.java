package graphql.annotations.processor.retrievers.fieldBuilders;

import graphql.annotations.annotationTypes.GraphQLDirectives;
import graphql.annotations.directives.DirectiveInfo;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.schema.GraphQLDirective;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DirectivesBuilder implements Builder<GraphQLDirective[]> {
    private AnnotatedElement object;
    private TypeFunction typeFunction;
    private ProcessingElementsContainer container;

    public DirectivesBuilder(AnnotatedElement object, TypeFunction typeFunction, ProcessingElementsContainer container) {
        this.object = object;
        this.typeFunction = typeFunction;
        this.container = container;
    }

    @Override
    public GraphQLDirective[] build() {
        GraphQLDirectives directives = object.getAnnotation(GraphQLDirectives.class);
        if (directives == null) return new GraphQLDirective[]{};
        List<GraphQLDirective> graphQLDirectives = Arrays.stream(directives.value()).map(x -> {
            try {
                DirectiveInfo directiveInfo = x.info().newInstance();
                return directiveInfo.toDirective(typeFunction, container, x.argumentsValues());
            } catch (InstantiationException | IllegalAccessException e) {
                return null;
            }
        }).collect(Collectors.toList());
        return graphQLDirectives.toArray(new GraphQLDirective[graphQLDirectives.size()]);
    }
}
