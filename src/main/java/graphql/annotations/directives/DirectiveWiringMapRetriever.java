/**
 * Copyright 2016 Yurii Rashkovskii
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations.directives;

import graphql.annotations.annotationTypes.directives.activation.GraphQLDirectives;
import graphql.annotations.processor.DirectiveAndWiring;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.util.DirectiveJavaAnnotationUtil;
import graphql.schema.GraphQLDirective;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class DirectiveWiringMapRetriever {
    public HashMap<GraphQLDirective, AnnotationsDirectiveWiring> getDirectiveWiringMap(AnnotatedElement object, ProcessingElementsContainer container) {
        LinkedHashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new LinkedHashMap<>();
        DirectiveJavaAnnotationUtil.getDirectiveAnnotations(object).sequential().forEach(x ->
                putInMap(container, map, DirectiveJavaAnnotationUtil.getName(x)));

        GraphQLDirectives directivesContainer = object.getAnnotation(GraphQLDirectives.class);
        if (directivesContainer == null) return map;
        Arrays.stream(directivesContainer.value()).sequential().forEach(x -> {
            putInMap(container, map, x.name());
        });

        return map;
    }

    private void putInMap(ProcessingElementsContainer container, LinkedHashMap<GraphQLDirective, AnnotationsDirectiveWiring> map, String name) {
        if (!container.getDirectiveRegistry().containsKey(name)) {
            throw new GraphQLAnnotationsException(String.format("No directive named %s is found in the directive registry", name), null);
        }
        DirectiveAndWiring directiveAndWiring = container.getDirectiveRegistry().get(name);
        if (directiveAndWiring.getWiringClass() == null) {
            throw new GraphQLAnnotationsException("No wiring class was supplied to directive " + directiveAndWiring.getDirective().getName(), null);
        }
        try {
            map.put(directiveAndWiring.getDirective(), directiveAndWiring.getWiringClass().newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new GraphQLAnnotationsException("Cannot create an instance of the wiring class " + directiveAndWiring.getWiringClass().getSimpleName(), e);
        }
    }
}
