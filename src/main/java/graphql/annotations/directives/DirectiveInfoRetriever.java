package graphql.annotations.directives;

import graphql.annotations.annotationTypes.GraphQLDirectives;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DirectiveInfoRetriever {
    public DirectiveInfo[] getDirectiveInfos(AnnotatedElement object) {
        GraphQLDirectives directives = object.getAnnotation(GraphQLDirectives.class);
        if (directives == null) return new DirectiveInfo[]{};
        List<DirectiveInfo> graphQLDirectives = Arrays.stream(directives.value()).map(x -> {
            try {
                DirectiveInfo directiveInfo = x.newInstance();
                return directiveInfo;
            } catch (InstantiationException | IllegalAccessException e) {
                return null;
            }
        }).collect(Collectors.toList());
        return graphQLDirectives.toArray(new DirectiveInfo[graphQLDirectives.size()]);

    }

}
