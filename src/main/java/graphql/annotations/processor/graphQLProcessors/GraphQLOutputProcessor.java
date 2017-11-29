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
package graphql.annotations.processor.graphQLProcessors;


import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.retrievers.GraphQLTypeRetriever;
import graphql.schema.GraphQLOutputType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

@Component(service = GraphQLOutputProcessor.class, immediate = true)
public class GraphQLOutputProcessor {

    private GraphQLTypeRetriever graphQLTypeRetriever;


    public GraphQLOutputProcessor(GraphQLTypeRetriever graphQLTypeRetriever) {
        this.graphQLTypeRetriever = graphQLTypeRetriever;
    }

    public GraphQLOutputProcessor() {
        this(new GraphQLTypeRetriever());
    }

    /**
     * This will examine the object class and return a {@link GraphQLOutputType} representation
     * which may be a {@link GraphQLOutputType} or a {@link graphql.schema.GraphQLTypeReference}
     *
     * @param object the object class to examine
     * @param container a class that hold several members that are required in order to build schema

     * @return a {@link GraphQLOutputType} that represents that object class
     *
     * @throws GraphQLAnnotationsException if the object class cannot be examined
     */
    public GraphQLOutputType getOutputTypeOrRef(Class<?> object, ProcessingElementsContainer container) throws GraphQLAnnotationsException {
        return (GraphQLOutputType) graphQLTypeRetriever.getGraphQLType(object, container, false);
    }

    @Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setGraphQLTypeRetriever(GraphQLTypeRetriever graphQLTypeRetriever) {
        this.graphQLTypeRetriever = graphQLTypeRetriever;
    }

    public void unsetGraphQLTypeRetriever(GraphQLTypeRetriever graphQLTypeRetriever) {
        this.graphQLTypeRetriever = new GraphQLTypeRetriever();
    }
}
