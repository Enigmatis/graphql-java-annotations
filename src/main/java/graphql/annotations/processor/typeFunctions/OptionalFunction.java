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
package graphql.annotations.processor.typeFunctions;

import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.schema.GraphQLType;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;

class OptionalFunction implements TypeFunction {

    private DefaultTypeFunction defaultTypeFunction;

    public OptionalFunction(DefaultTypeFunction defaultTypeFunction){
        this.defaultTypeFunction=defaultTypeFunction;
    }

    @Override
    public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
        return aClass == Optional.class;
    }

    @Override
    public String getTypeName(Class<?> aClass, AnnotatedType annotatedType) {
        AnnotatedType arg = getAnnotatedType(annotatedType);
        return defaultTypeFunction.getTypeName(getClass(annotatedType), arg);
    }

    @Override
    public GraphQLType buildType(boolean input, Class<?> aClass, AnnotatedType annotatedType, ProcessingElementsContainer container) {
        AnnotatedType arg = getAnnotatedType(annotatedType);
        return defaultTypeFunction.buildType(input, getClass(annotatedType), arg,container);
    }

    private AnnotatedType getAnnotatedType(AnnotatedType annotatedType) {
        if (!(annotatedType instanceof AnnotatedParameterizedType)) {
            throw new IllegalArgumentException("Optional type parameter should be specified");
        }
        AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) annotatedType;
        return parameterizedType.getAnnotatedActualTypeArguments()[0];
    }

    private Class<?> getClass(AnnotatedType annotatedType) {
        AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) annotatedType;
        AnnotatedType arg = parameterizedType.getAnnotatedActualTypeArguments()[0];
        if (arg.getType() instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) (arg.getType())).getRawType();
        } else {
            return (Class<?>) arg.getType();
        }
    }
}

