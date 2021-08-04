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
package graphql.annotations.processor.retrievers.fieldBuilders.method;

import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.retrievers.fieldBuilders.Builder;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;

public class MethodTypeBuilder implements Builder<GraphQLType> {
    private Method method;
    private TypeFunction typeFunction;
    private ProcessingElementsContainer container;
    private boolean isInput;

    public MethodTypeBuilder(Method method, TypeFunction typeFunction, ProcessingElementsContainer container, boolean isInput) {
        this.method = method;
        this.typeFunction = typeFunction;
        this.container = container;
        this.isInput = isInput;
    }

    @Override
    public GraphQLType build() {
        AnnotatedType annotatedReturnType = method.getAnnotatedReturnType();

        return this.typeFunction.buildType(isInput,method.getReturnType(), annotatedReturnType, container);
    }

}
