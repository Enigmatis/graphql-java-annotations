package graphql.annotations.directives;

import graphql.annotations.annotationTypes.GraphQLDirectives;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.schema.GraphQLDirective;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DirectiveInfoRetriever {
    public Map<GraphQLDirective, AnnotationsDirectiveWiring> getDirectiveInfos(AnnotatedElement object, ProcessingElementsContainer container) {
        GraphQLDirectives directivesContainer = object.getAnnotation(GraphQLDirectives.class);
        Map<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        if (directivesContainer == null) return map;
        Arrays.stream(directivesContainer.value()).forEach(x -> {
            try {
                map.put(container.getDirectiveRegistry().get(x.name()), x.wiringClass().newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new GraphQLAnnotationsException("Cannot create an instance of the wiring class " + x.wiringClass().toString(), e);
            }
        });
        return map;
    }

}
