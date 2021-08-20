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

import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.annotationTypes.GraphQLRelayMutation;
import graphql.annotations.connection.GraphQLConnection;
import graphql.annotations.dataFetchers.MethodDataFetcher;
import graphql.annotations.dataFetchers.RelayMutationMethodDataFetcher;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.retrievers.fieldBuilders.Builder;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.annotations.processor.util.DataFetcherConstructor;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;

import java.lang.reflect.Method;
import java.util.List;

import static graphql.annotations.processor.util.ConnectionUtil.getConnectionDataFetcher;

public class MethodDataFetcherBuilder implements Builder<DataFetcher> {
    private Method method;
    private GraphQLOutputType outputType;
    private TypeFunction typeFunction;
    private ProcessingElementsContainer container;
    private GraphQLFieldDefinition relayFieldDefinition;
    private List<GraphQLArgument> args;
    private DataFetcherConstructor dataFetcherConstructor;
    private boolean isConnection;

    public MethodDataFetcherBuilder(Method method, GraphQLOutputType outputType, TypeFunction typeFunction,
                                    ProcessingElementsContainer container, GraphQLFieldDefinition relayFieldDefinition,
                                    List<GraphQLArgument> args, DataFetcherConstructor dataFetcherConstructor, boolean isConnection) {
        this.method = method;
        this.outputType = outputType;
        this.typeFunction = typeFunction;
        this.container = container;
        this.relayFieldDefinition = relayFieldDefinition;
        this.args = args;
        this.dataFetcherConstructor = dataFetcherConstructor;
        this.isConnection = isConnection;
    }

    @Override
    public DataFetcher build() {
        GraphQLDataFetcher dataFetcher = method.getAnnotation(GraphQLDataFetcher.class);
        DataFetcher actualDataFetcher;
        if (dataFetcher == null) {
            actualDataFetcher = new MethodDataFetcher(method, typeFunction, container);
        } else {
            actualDataFetcher = dataFetcherConstructor.constructDataFetcher(method.getName(), dataFetcher);
        }

        if (method.isAnnotationPresent(GraphQLRelayMutation.class) && relayFieldDefinition != null) {
            actualDataFetcher = new RelayMutationMethodDataFetcher(method, args, relayFieldDefinition.getArgument("input").getType(), relayFieldDefinition.getType());
        }

        if (isConnection){
            actualDataFetcher = getConnectionDataFetcher(method.getAnnotation(GraphQLConnection.class), actualDataFetcher);
        }
        return actualDataFetcher;
    }
}
