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
package graphql.annotations.processor.util;

import graphql.annotations.directives.AnnotationsWiringEnvironment;
import graphql.schema.*;

import java.util.function.BiFunction;

import static graphql.schema.GraphQLObjectType.newObject;

public class CodeRegistryUtil {
    /**
     * This util method helps you wrap your datafetcher with some lambda code
     *
     * @param fieldDefinition The field you want to wrap its datafetcher
     * @param environment     the environment object of the Wiring process
     * @param mapFunction     the lambda expression to wrap with
     */
    public static void wrapDataFetcher(GraphQLFieldDefinition fieldDefinition, AnnotationsWiringEnvironment environment,
                                       BiFunction<DataFetchingEnvironment, Object, Object> mapFunction) {
        DataFetcher originalDataFetcher = getDataFetcher(environment.getCodeRegistryBuilder(), environment.getParentName(), fieldDefinition);
        DataFetcher wrappedDataFetcher = DataFetcherFactories.wrapDataFetcher(originalDataFetcher, mapFunction);
        environment.getCodeRegistryBuilder()
                .dataFetcher(FieldCoordinates.coordinates(environment.getParentName(), fieldDefinition.getName()), wrappedDataFetcher);
    }

    /**
     * this util method helps you retrieve the data fetcher from the code registry if you do not have the whole parent object (only parent name)
     *
     * @param codeRegistryBuilder the code registry builder
     * @param parentName          the parent name
     * @param fieldDefinition     the field definition which the data fetcher is linked to
     * @return the data fetcher
     */
    public static DataFetcher getDataFetcher(GraphQLCodeRegistry.Builder codeRegistryBuilder, String parentName, GraphQLFieldDefinition fieldDefinition) {
        return codeRegistryBuilder.getDataFetcher(newObject().name(parentName).build(), fieldDefinition);
    }
}
