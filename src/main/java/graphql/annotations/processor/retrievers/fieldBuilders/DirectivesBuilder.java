package graphql.annotations.processor.retrievers.fieldBuilders;

import graphql.annotations.annotationTypes.GraphQLDirectives;
import graphql.annotations.directives.DirectiveInfo;
import graphql.schema.GraphQLDirective;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DirectivesBuilder implements Builder<GraphQLDirective[]> {
    private AnnotatedElement object;

    public DirectivesBuilder(AnnotatedElement object) {
        this.object = object;
    }

    @Override
    public GraphQLDirective[] build() {
        GraphQLDirectives directives = object.getAnnotation(GraphQLDirectives.class);
        if (directives == null) return new GraphQLDirective[]{};
        List<GraphQLDirective> graphQLDirectives = Arrays.stream(directives.value()).map(x -> {
            try {
                DirectiveInfo directiveInfo = x.newInstance();
                return directiveInfo.toDirective();
            } catch (InstantiationException | IllegalAccessException e) {
                return null;
            }
        }).collect(Collectors.toList());
        return graphQLDirectives.toArray(new GraphQLDirective[graphQLDirectives.size()]);
    }
}
