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
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLType;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;

public class ArrayFunction implements TypeFunction {

    private DefaultTypeFunction defaultTypeFunction;

    public ArrayFunction(DefaultTypeFunction defaultTypeFunction) {
        this.defaultTypeFunction = defaultTypeFunction;
    }

    @Override
    public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
        return aClass.isArray();
    }

    @Override
    public GraphQLType buildType(boolean input, Class<?> aClass, AnnotatedType annotatedType, ProcessingElementsContainer container) {
        if (!(annotatedType instanceof AnnotatedArrayType)) {
            throw new IllegalArgumentException("Array type parameter should be specified");
        }
        AnnotatedArrayType parameterizedType = (AnnotatedArrayType) annotatedType;
        AnnotatedType arg = parameterizedType.getAnnotatedGenericComponentType();
        Class<?> klass;
        if (arg.getType() instanceof ParameterizedType) {
            klass = (Class<?>) ((ParameterizedType) (arg.getType())).getRawType();
        } else {
            klass = (Class<?>) arg.getType();
        }
        return new GraphQLList(defaultTypeFunction.buildType(input, klass, arg, container));
    }
}
