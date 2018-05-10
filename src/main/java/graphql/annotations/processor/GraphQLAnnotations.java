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

import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.graphQLProcessors.GraphQLAnnotationsProcessor;
import graphql.annotations.processor.graphQLProcessors.GraphQLInputProcessor;
import graphql.annotations.processor.graphQLProcessors.GraphQLOutputProcessor;
import graphql.annotations.processor.retrievers.*;
import graphql.annotations.processor.searchAlgorithms.BreadthFirstSearch;
import graphql.annotations.processor.searchAlgorithms.ParentalSearch;
import graphql.annotations.processor.typeFunctions.DefaultTypeFunction;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.annotations.processor.util.DataFetcherConstructor;
import graphql.relay.Relay;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;

import java.util.Map;
import java.util.Set;

import static graphql.annotations.processor.util.NamingKit.toGraphqlName;

/**
 * A utility class for extracting GraphQL data structures from annotated
 * elements.
 */
public class GraphQLAnnotations implements GraphQLAnnotationsProcessor {

    private GraphQLObjectHandler graphQLObjectHandler;
    private GraphQLExtensionsHandler graphQLExtensionsHandler;
    private GraphQLAdditionalTypesHandler graphQLAdditionalTypesHandler;

    private ProcessingElementsContainer container;

    public GraphQLAnnotations() {
        GraphQLObjectHandler objectHandler = new GraphQLObjectHandler();
        GraphQLTypeRetriever typeRetriever = new GraphQLTypeRetriever();
        GraphQLObjectInfoRetriever objectInfoRetriever = new GraphQLObjectInfoRetriever();
        GraphQLInterfaceRetriever interfaceRetriever = new GraphQLInterfaceRetriever();
        GraphQLFieldRetriever fieldRetriever = new GraphQLFieldRetriever();
        GraphQLInputProcessor inputProcessor = new GraphQLInputProcessor();
        GraphQLOutputProcessor outputProcessor = new GraphQLOutputProcessor();
        BreadthFirstSearch methodSearchAlgorithm = new BreadthFirstSearch(objectInfoRetriever);
        ParentalSearch fieldSearchAlgorithm = new ParentalSearch(objectInfoRetriever);
        DataFetcherConstructor dataFetcherConstructor = new DataFetcherConstructor();
        GraphQLExtensionsHandler extensionsHandler = new GraphQLExtensionsHandler();
        DefaultTypeFunction defaultTypeFunction = new DefaultTypeFunction(inputProcessor, outputProcessor);

        objectHandler.setTypeRetriever(typeRetriever);
        typeRetriever.setGraphQLObjectInfoRetriever(objectInfoRetriever);
        typeRetriever.setGraphQLInterfaceRetriever(interfaceRetriever);
        typeRetriever.setMethodSearchAlgorithm(methodSearchAlgorithm);
        typeRetriever.setFieldSearchAlgorithm(fieldSearchAlgorithm);
        typeRetriever.setExtensionsHandler(extensionsHandler);
        typeRetriever.setGraphQLFieldRetriever(fieldRetriever);
        interfaceRetriever.setGraphQLTypeRetriever(typeRetriever);
        fieldRetriever.setDataFetcherConstructor(dataFetcherConstructor);
        inputProcessor.setGraphQLTypeRetriever(typeRetriever);
        outputProcessor.setGraphQLTypeRetriever(typeRetriever);
        extensionsHandler.setGraphQLObjectInfoRetriever(objectInfoRetriever);
        extensionsHandler.setFieldSearchAlgorithm(fieldSearchAlgorithm);
        extensionsHandler.setMethodSearchAlgorithm(methodSearchAlgorithm);
        extensionsHandler.setFieldRetriever(fieldRetriever);

        GraphQLAdditionalTypesHandler additionalTypesHandler = new GraphQLAdditionalTypesHandler();
        additionalTypesHandler.setGraphQLInterfaceRetriever(interfaceRetriever);
        additionalTypesHandler.setGraphQLObjectInfoRetriever(objectInfoRetriever);
        additionalTypesHandler.setMethodSearchAlgorithm(methodSearchAlgorithm);
        additionalTypesHandler.setFieldSearchAlgorithm(fieldSearchAlgorithm);

        this.graphQLAdditionalTypesHandler = additionalTypesHandler;
        this.graphQLObjectHandler = objectHandler;
        this.graphQLExtensionsHandler = extensionsHandler;
        this.container = new ProcessingElementsContainer(defaultTypeFunction);
    }

    public GraphQLAnnotations(TypeFunction defaultTypeFunction, GraphQLObjectHandler graphQLObjectHandler, GraphQLExtensionsHandler graphQLExtensionsHandler) {
        this.graphQLObjectHandler = graphQLObjectHandler;
        this.graphQLExtensionsHandler = graphQLExtensionsHandler;
        this.container = new ProcessingElementsContainer(defaultTypeFunction);
    }

    public static GraphQLAnnotations instance = new GraphQLAnnotations();

    public static GraphQLAnnotations getInstance() {
        return instance;
    }

    public void setRelay(Relay relay) {
        this.container.setRelay(relay);
    }

    public String getTypeName(Class<?> objectClass) {
        GraphQLName name = objectClass.getAnnotation(GraphQLName.class);
        return toGraphqlName(name == null ? objectClass.getSimpleName() : name.value());
    }

    public static GraphQLObjectType object(Class<?> object) throws GraphQLAnnotationsException {
        GraphQLAnnotations instance = getInstance();
        return instance.graphQLObjectHandler.getObject(object, instance.getContainer());
    }

    public static Set<GraphQLType> additionalTypes(Class<?> root) {
        GraphQLAnnotations instance = getInstance();
        return instance.graphQLAdditionalTypesHandler.getAdditionalInterfacesImplementations(root, instance.getContainer());

    }

    public void registerTypeExtension(Class<?> objectClass) {
        graphQLExtensionsHandler.registerTypeExtension(objectClass, container);
    }

    public void registerType(TypeFunction typeFunction) {
        ((DefaultTypeFunction) container.getDefaultTypeFunction()).register(typeFunction);
    }

    public static void register(TypeFunction typeFunction) {
        getInstance().registerType(typeFunction);
    }

    public Map<String, GraphQLType> getTypeRegistry() {
        return container.getTypeRegistry();
    }

    public GraphQLObjectHandler getObjectHandler() {
        return graphQLObjectHandler;
    }

    public GraphQLExtensionsHandler getExtensionsHandler() {
        return graphQLExtensionsHandler;
    }

    public ProcessingElementsContainer getContainer() {
        return container;
    }

    public void setContainer(ProcessingElementsContainer container) {
        this.container = container;
    }

    public void setDefaultTypeFunction(TypeFunction function) {
        this.container.setDefaultTypeFunction(function);
    }

}
