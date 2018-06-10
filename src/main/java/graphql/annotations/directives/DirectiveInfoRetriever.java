package graphql.annotations.directives;

import graphql.annotations.annotationTypes.GraphQLDirectives;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DirectiveInfoRetriever {
    public DirectiveInfo[] getDirectiveInfos(AnnotatedElement object) {
        GraphQLDirectives directivesContainer = object.getAnnotation(GraphQLDirectives.class);
        if (directivesContainer == null) return new DirectiveInfo[]{};
        List<DirectiveInfo> graphQLDirectives = Arrays.stream(directivesContainer.value()).map(x -> {
            try {
                DirectiveInfo directiveInfo = x.info().newInstance();
                return directiveInfo;
            } catch (InstantiationException | IllegalAccessException e) {
                return null;
            }
        }).collect(Collectors.toList());
        return graphQLDirectives.toArray(new DirectiveInfo[graphQLDirectives.size()]);

    }

}
