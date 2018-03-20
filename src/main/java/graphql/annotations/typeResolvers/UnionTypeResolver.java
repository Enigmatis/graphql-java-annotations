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
package graphql.annotations.typeResolvers;

import graphql.TypeResolutionEnvironment;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.TypeResolver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UnionTypeResolver implements TypeResolver {
    private final Map<Class<?>, graphql.schema.GraphQLType> types = new HashMap<>();

    public UnionTypeResolver(Class<?>[] classes, ProcessingElementsContainer container) {
        Arrays.stream(classes).
                forEach(c -> types.put(c,container.getDefaultTypeFunction().buildType(c, null, container)));
    }

    @Override
    public GraphQLObjectType getType(TypeResolutionEnvironment env) {
        Object object = env.getObject();
        Optional<Map.Entry<Class<?>, GraphQLType>> maybeType = types.entrySet().
                stream().filter(e -> e.getKey().isAssignableFrom(object.getClass())).findFirst();
        if (maybeType.isPresent()) {
            return (GraphQLObjectType) maybeType.get().getValue();
        } else {
            throw new RuntimeException("Unknown type " + object.getClass());
        }
    }
}
