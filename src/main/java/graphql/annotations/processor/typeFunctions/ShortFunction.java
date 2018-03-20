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
package graphql.annotations.processor.typeFunctions;

import graphql.Scalars;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.schema.GraphQLType;

import java.lang.reflect.AnnotatedType;

public class ShortFunction implements TypeFunction {
    @Override
    public String getTypeName(Class<?> aClass, AnnotatedType annotatedType) {
        return Scalars.GraphQLShort.getName();
    }

    @Override
    public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
        return aClass == Short.class;
    }

    @Override
    public GraphQLType buildType(boolean input, Class<?> aClass, AnnotatedType annotatedType, ProcessingElementsContainer container) {
        return buildType(input, aClass, annotatedType);
    }

    private GraphQLType buildType(boolean inputType, Class<?> aClass, AnnotatedType annotatedType) {
        return Scalars.GraphQLShort;
    }
}

