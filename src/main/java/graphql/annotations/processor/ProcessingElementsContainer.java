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
import graphql.annotations.processor.typeFunctions.DefaultTypeFunction;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.relay.Relay;
import graphql.schema.GraphQLDirective;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static graphql.annotations.processor.util.InputPropertiesUtil.DEFAULT_INPUT_SUFFIX;
import static graphql.annotations.processor.util.InputPropertiesUtil.DEFAULT_INPUT_PREFIX;

public class ProcessingElementsContainer {

    private TypeFunction defaultTypeFunction;
    private graphql.relay.Relay relay;
    private Map<String, graphql.schema.GraphQLType> typeRegistry;
    private Map<String, graphql.schema.GraphQLDirective> directiveRegistry;
    private Map<Class<?>, Set<Class<?>>> extensionsTypeRegistry;
    private Stack<String> processing;
    private String inputPrefix = DEFAULT_INPUT_PREFIX;
    private String inputSuffix = DEFAULT_INPUT_SUFFIX;

    public Map<String, GraphQLDirective> getDirectiveRegistry() {
        return directiveRegistry;
    }

    public void setDirectiveRegistry(Map<String, GraphQLDirective> directiveRegistry) {
        this.directiveRegistry = directiveRegistry;
    }

    public ProcessingElementsContainer(TypeFunction defaultTypeFunction, Relay relay, Map<String, graphql.schema.GraphQLType> typeRegistry, Map<String, graphql.schema.GraphQLDirective> directiveRegistry, Map<Class<?>, Set<Class<?>>> extensionsTypeRegistry, Stack<String> processing) {
        this.defaultTypeFunction = defaultTypeFunction;
        this.relay = relay;
        this.typeRegistry = typeRegistry;
        this.directiveRegistry = directiveRegistry;
        this.extensionsTypeRegistry = extensionsTypeRegistry;
        this.processing = processing;
    }

    public ProcessingElementsContainer(TypeFunction typeFunction) {
        this(typeFunction, new Relay(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new Stack<>());
    }

    public ProcessingElementsContainer() {
        this(new DefaultTypeFunction(new GraphQLInputProcessor(), new GraphQLOutputProcessor()), new Relay(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new Stack<>());
    }

    public Relay getRelay() {
        return this.relay;
    }

    public void setRelay(Relay relay) {
        this.relay = relay;
    }

    public Map<String, graphql.schema.GraphQLType> getTypeRegistry() {
        return this.typeRegistry;
    }

    public void setTypeRegistry(Map<String, graphql.schema.GraphQLType> typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    public Map<Class<?>, Set<Class<?>>> getExtensionsTypeRegistry() {
        return this.extensionsTypeRegistry;
    }

    public void setExtensionsTypeRegistry(Map<Class<?>, Set<Class<?>>> extensionsTypeRegistry) {
        this.extensionsTypeRegistry = extensionsTypeRegistry;
    }

    public TypeFunction getDefaultTypeFunction() {
        return defaultTypeFunction;
    }

    public void setDefaultTypeFunction(TypeFunction defaultTypeFunction) {
        this.defaultTypeFunction = defaultTypeFunction;
    }

    public Stack<String> getProcessing() {
        return processing;
    }

    public void setProcessing(Stack<String> processing) {
        this.processing = processing;
    }

    public String getInputPrefix() {
        return inputPrefix;
    }

    public void setInputPrefix(String inputPrefix) {
        this.inputPrefix = inputPrefix;
    }

    public String getInputSuffix() {
        return inputSuffix;
    }

    public void setInputSuffix(String inputSuffix) {
        this.inputSuffix = inputSuffix;
    }
}
