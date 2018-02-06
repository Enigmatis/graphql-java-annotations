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
package graphql.annotations.processor;

import graphql.annotations.processor.graphQLProcessors.GraphQLInputProcessor;
import graphql.annotations.processor.graphQLProcessors.GraphQLOutputProcessor;
import graphql.annotations.processor.retrievers.GraphQLExtensionsHandler;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import org.osgi.service.component.annotations.*;

@Component(service = GraphQLAnnotationsComponent.class, immediate = true)
public class GraphQLAnnotationsComponent {

    private TypeFunction defaultTypeFunction;
    private GraphQLOutputProcessor outputTypeProcessor;
    private GraphQLInputProcessor inputTypeProcessor;
    private GraphQLExtensionsHandler extensionsHandler;

    public ProcessingElementsContainer createContainer() {
        return new ProcessingElementsContainer(defaultTypeFunction);
    }

    public GraphQLOutputProcessor getOutputTypeProcessor() {
        return outputTypeProcessor;
    }

    public GraphQLInputProcessor getInputTypeProcessor() {
        return inputTypeProcessor;
    }

    public GraphQLExtensionsHandler getExtensionsHandler() {
        return extensionsHandler;
    }

    @Reference(target = "(type=default)", policy=ReferencePolicy.DYNAMIC, policyOption= ReferencePolicyOption.GREEDY)
    public void setDefaultTypeFunction(TypeFunction defaultTypeFunction) {
        this.defaultTypeFunction = defaultTypeFunction;
    }

    public void unsetDefaultTypeFunction(TypeFunction defaultTypeFunction) {
        this.defaultTypeFunction = null;
    }

    @Reference(policy=ReferencePolicy.DYNAMIC, policyOption= ReferencePolicyOption.GREEDY)
    public void setOutputTypeProcessor(GraphQLOutputProcessor outputTypeProcessor) {
        this.outputTypeProcessor = outputTypeProcessor;
    }

    public void unsetOutputTypeProcessor(GraphQLOutputProcessor outputTypeProcessor) {
        this.outputTypeProcessor = null;
    }

    @Reference(policy=ReferencePolicy.DYNAMIC, policyOption= ReferencePolicyOption.GREEDY)
    public void setInputTypeProcessor(GraphQLInputProcessor inputTypeProcessor) {
        this.inputTypeProcessor = inputTypeProcessor;
    }

    public void unsetInputTypeProcessor(GraphQLInputProcessor inputTypeProcessor) {
        this.inputTypeProcessor = null;
    }

    @Reference(policy=ReferencePolicy.DYNAMIC, policyOption= ReferencePolicyOption.GREEDY)
    public void setExtensionsHandler(GraphQLExtensionsHandler extensionsHandler) {
        this.extensionsHandler = extensionsHandler;
    }

    public void unsetExtensionsHandler(GraphQLExtensionsHandler extensionsHandler) {
        this.extensionsHandler = null;
    }
}
