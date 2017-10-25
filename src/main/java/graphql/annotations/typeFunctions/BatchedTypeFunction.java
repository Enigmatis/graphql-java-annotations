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
package graphql.annotations.typeFunctions;

import graphql.annotations.ProcessingElementsContainer;
import graphql.annotations.TypeFunction;
import graphql.schema.GraphQLType;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.util.List;

public class BatchedTypeFunction implements TypeFunction {
    private TypeFunction defaultTypeFunction;

    public BatchedTypeFunction(TypeFunction defaultTypeFunction) {
        this.defaultTypeFunction = defaultTypeFunction;
    }

    @Override
    public String getTypeName(Class<?> aClass, AnnotatedType annotatedType) {
        return defaultTypeFunction.getTypeName(aClass, annotatedType);
    }

    @Override
    public boolean canBuildType(final Class<?> aClass, final AnnotatedType type) {
        return defaultTypeFunction.canBuildType(aClass, type);
    }

    @Override
    public GraphQLType buildType(boolean input, Class<?> aClass, AnnotatedType annotatedType, ProcessingElementsContainer container) {
        if (!aClass.isAssignableFrom(List.class)) {
            throw new IllegalArgumentException("Batched method should return a List");
        }
        if (!(annotatedType instanceof AnnotatedParameterizedType)) {
            throw new IllegalArgumentException("Batched should return parameterized type");
        }
        AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) annotatedType;
        AnnotatedType arg = parameterizedType.getAnnotatedActualTypeArguments()[0];
        Class<?> klass;
        if (arg.getType() instanceof ParameterizedType) {
            klass = (Class<?>) ((ParameterizedType) (arg.getType())).getRawType();
        } else {
            klass = (Class<?>) arg.getType();
        }
        return defaultTypeFunction.buildType(input, klass, arg,container);
    }
}
