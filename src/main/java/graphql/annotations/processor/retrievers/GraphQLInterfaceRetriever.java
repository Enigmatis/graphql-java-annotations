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
package graphql.annotations.processor.retrievers;

import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.ProcessingElementsContainer;

public class GraphQLInterfaceRetriever {

    private  GraphQLOutputObjectRetriever graphQLOutputObjectRetriever;

    public GraphQLInterfaceRetriever(GraphQLOutputObjectRetriever graphQLOutputObjectRetriever){
        this.graphQLOutputObjectRetriever=graphQLOutputObjectRetriever;
    }

    public GraphQLInterfaceRetriever(){
        this(new GraphQLOutputObjectRetriever());
    }
    public graphql.schema.GraphQLOutputType getInterface(Class<?> iface, ProcessingElementsContainer container) throws GraphQLAnnotationsException {
        return graphQLOutputObjectRetriever.getOutputType(iface,container);
    }
}
