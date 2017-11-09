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

import graphql.annotations.annotationTypes.GraphQLBatched;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.retrievers.fieldBuilders.Builder;
import graphql.annotations.processor.typeFunctions.BatchedTypeFunction;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.schema.GraphQLOutputType;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;

public class MethodTypeBuilder implements Builder<GraphQLOutputType> {
    private Method method;
    private TypeFunction typeFunction;
    private ProcessingElementsContainer container;

    public MethodTypeBuilder(Method method, TypeFunction typeFunction, ProcessingElementsContainer container) {
        this.method = method;
        this.typeFunction = typeFunction;
        this.container = container;
    }

    @Override
    public GraphQLOutputType build() {
        AnnotatedType annotatedReturnType = method.getAnnotatedReturnType();

        TypeFunction outputTypeFunction;
        if (method.getAnnotation(GraphQLBatched.class) != null) {
            outputTypeFunction = new BatchedTypeFunction(typeFunction);
        } else {
            outputTypeFunction = typeFunction;
        }

        return (GraphQLOutputType) outputTypeFunction.buildType(method.getReturnType(), annotatedReturnType, container);
    }

}
