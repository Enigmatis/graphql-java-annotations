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

import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.graphQLProcessors.GraphQLInputProcessor;
import graphql.annotations.processor.graphQLProcessors.GraphQLOutputProcessor;
import graphql.schema.GraphQLType;

import java.lang.reflect.AnnotatedType;

import static graphql.annotations.processor.util.NamingKit.toGraphqlName;


public class ObjectFunction implements TypeFunction {

    private GraphQLInputProcessor graphQLInputProcessor;
    private GraphQLOutputProcessor graphQLOutputProcessor;

    public ObjectFunction(GraphQLInputProcessor graphQLInputProcessor, GraphQLOutputProcessor graphQLOutputProcessor) {
        this.graphQLInputProcessor = graphQLInputProcessor;
        this.graphQLOutputProcessor = graphQLOutputProcessor;
    }


    @Override
    public String getTypeName(Class<?> aClass, AnnotatedType annotatedType) {
        GraphQLName name = aClass.getAnnotation(GraphQLName.class);
        return toGraphqlName(name == null ? aClass.getSimpleName() : name.value());
    }

    @Override
    public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
        return true;
    }

    @Override
    public GraphQLType buildType(boolean inputType, Class<?> aClass, AnnotatedType annotatedType, ProcessingElementsContainer container) {
        if (inputType) {
            return graphQLInputProcessor.getInputTypeOrRef(aClass, container);
        } else {
            return graphQLOutputProcessor.getOutputTypeOrRef(aClass, container);
        }
    }

}
