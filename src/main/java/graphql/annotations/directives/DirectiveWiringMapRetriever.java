/**
 * Copyright 2016 Yurii Rashkovskii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations.directives;

import graphql.annotations.annotationTypes.GraphQLDirectives;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.schema.GraphQLDirective;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DirectiveWiringMapRetriever {
    public Map<GraphQLDirective, AnnotationsDirectiveWiring> getDirectiveWiringMap(AnnotatedElement object, ProcessingElementsContainer container) {
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
