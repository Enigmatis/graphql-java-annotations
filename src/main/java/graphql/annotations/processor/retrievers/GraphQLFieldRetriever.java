/**
 * Copyright 2016 Yurii Rashkovskii
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations.processor.retrievers;


import graphql.annotations.GraphQLFieldDefinitionWrapper;
import graphql.annotations.annotationTypes.GraphQLRelayMutation;
import graphql.annotations.annotationTypes.GraphQLType;
import graphql.annotations.connection.GraphQLConnection;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.CannotCastMemberException;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.retrievers.fieldBuilders.ArgumentBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.DeprecateBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.DescriptionBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.field.FieldDataFetcherBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.field.FieldNameBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.method.MethodDataFetcherBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.method.MethodNameBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.method.MethodTypeBuilder;
import graphql.annotations.processor.searchAlgorithms.BreadthFirstSearch;
import graphql.annotations.processor.searchAlgorithms.ParentalSearch;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.annotations.processor.util.ConnectionUtil;
import graphql.annotations.processor.util.DataFetcherConstructor;
import graphql.relay.Relay;
import graphql.schema.*;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static graphql.annotations.processor.util.ObjectUtil.getAllFields;
import static graphql.annotations.processor.util.ReflectionKit.newInstance;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;

public class GraphQLFieldRetriever {


    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;
    private BreadthFirstSearch breadthFirstSearch;
    private ParentalSearch parentalSearch;
    private GraphQLInputObjectRetriever graphQLInputObjectRetriever;
    private DataFetcherConstructor dataFetcherConstructor;

    public GraphQLFieldRetriever(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever, BreadthFirstSearch breadthFirstSearch, ParentalSearch parentalSearch,
                                 GraphQLInputObjectRetriever graphQLInputObjectRetriever, DataFetcherConstructor dataFetcherConstructor) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
        this.breadthFirstSearch = breadthFirstSearch;
        this.parentalSearch = parentalSearch;
        this.graphQLInputObjectRetriever = graphQLInputObjectRetriever;
        this.dataFetcherConstructor = dataFetcherConstructor;
    }

    public GraphQLFieldRetriever() {
        this(new GraphQLObjectInfoRetriever(), new BreadthFirstSearch(new GraphQLObjectInfoRetriever()), new ParentalSearch(new GraphQLObjectInfoRetriever()), new GraphQLInputObjectRetriever(), new DataFetcherConstructor());
    }

    public List<GraphQLFieldDefinition> getExtensionFields(Class<?> object, List<String> fieldsDefined, ProcessingElementsContainer container) throws CannotCastMemberException {
        List<GraphQLFieldDefinition> fields = new ArrayList<>();
        if (container.getExtensionsTypeRegistry().containsKey(object)) {
            for (Class<?> aClass : container.getExtensionsTypeRegistry().get(object)) {
                for (Method method : graphQLObjectInfoRetriever.getOrderedMethods(aClass)) {
                    if (method.isBridge() || method.isSynthetic()) {
                        continue;
                    }
                    if (breadthFirstSearch.isFound(method)) {
                        addExtensionField(getField(method, container), fields, fieldsDefined);
                    }
                }
                for (Field field : getAllFields(aClass).values()) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    if (parentalSearch.isFound(field)) {
                        addExtensionField(getField(field, container), fields, fieldsDefined);
                    }
                }
            }
        }
        return fields;
    }

    public GraphQLFieldDefinition getField(Method method, ProcessingElementsContainer container) throws GraphQLAnnotationsException {
        GraphQLFieldDefinition.Builder builder = newFieldDefinition();
        TypeFunction typeFunction = getTypeFunction(method, container);
        builder.name(new MethodNameBuilder(method).build());
        GraphQLOutputType outputType = (GraphQLOutputType) new MethodTypeBuilder(method, typeFunction, container, false).build();

        boolean isConnection = ConnectionUtil.isConnection(method, outputType);
        if (isConnection) {
            outputType = getGraphQLConnection(method, outputType, container.getRelay(), container.getTypeRegistry());
        }
        builder.type(outputType);
        handleConnectionArgument(container, builder, isConnection);
        List<GraphQLArgument> args = new ArgumentBuilder(method, typeFunction, graphQLInputObjectRetriever, builder, container, outputType).build();
        GraphQLFieldDefinition relayFieldDefinition = handleRelayArguments(method, container, builder, outputType, args);
        builder.description(new DescriptionBuilder(method).build())
                .deprecate(new DeprecateBuilder(method).build())
                .dataFetcher(new MethodDataFetcherBuilder(method, outputType, typeFunction, container, relayFieldDefinition, args, dataFetcherConstructor, isConnection).build());
        return new GraphQLFieldDefinitionWrapper(builder.build());
    }

    public GraphQLFieldDefinition getField(Field field, ProcessingElementsContainer container) throws GraphQLAnnotationsException {
        GraphQLFieldDefinition.Builder builder = newFieldDefinition();
        builder.name(new FieldNameBuilder(field).build());
        TypeFunction typeFunction = getTypeFunction(field, container);

        graphql.schema.GraphQLType outputType = typeFunction.buildType(field.getType(), field.getAnnotatedType(), container);
        boolean isConnection = ConnectionUtil.isConnection(field, outputType);
        if (isConnection) {
            outputType = getGraphQLConnection(field, outputType, container.getRelay(), container.getTypeRegistry());
            builder.argument(container.getRelay().getConnectionFieldArguments());
        }

        builder.type((GraphQLOutputType) outputType).description(new DescriptionBuilder(field).build())
                .deprecate(new DeprecateBuilder(field).build())
                .dataFetcher(new FieldDataFetcherBuilder(field, dataFetcherConstructor, outputType, typeFunction, container, isConnection).build());

        return new GraphQLFieldDefinitionWrapper(builder.build());
    }

    public GraphQLInputObjectField getInputField(Method method, ProcessingElementsContainer container) throws GraphQLAnnotationsException {
        GraphQLInputObjectField.Builder builder = newInputObjectField();
        builder.name(new MethodNameBuilder(method).build());
        TypeFunction typeFunction = getTypeFunction(method, container);
        GraphQLInputType inputType = (GraphQLInputType) new MethodTypeBuilder(method, typeFunction, container, true).build();
        return builder.type(inputType).description(new DescriptionBuilder(method).build()).build();
    }

    public GraphQLInputObjectField getInputField(Field field, ProcessingElementsContainer container) throws GraphQLAnnotationsException {
        GraphQLInputObjectField.Builder builder = newInputObjectField();
        builder.name(new FieldNameBuilder(field).build());
        TypeFunction typeFunction = getTypeFunction(field, container);
        graphql.schema.GraphQLType graphQLType = typeFunction.buildType(true,field.getType(), field.getAnnotatedType(), container);
        return builder.type((GraphQLInputType) graphQLType).description(new DescriptionBuilder(field).build()).build();
    }

    private GraphQLFieldDefinition handleRelayArguments(Method method, ProcessingElementsContainer container, GraphQLFieldDefinition.Builder builder, GraphQLOutputType outputType, List<GraphQLArgument> args) {
        GraphQLFieldDefinition relayFieldDefinition = null;
        if (method.isAnnotationPresent(GraphQLRelayMutation.class)) {
            relayFieldDefinition = buildRelayMutation(method, container, builder, outputType, args);
        } else {
            builder.argument(args);
        }
        return relayFieldDefinition;
    }

    private void handleConnectionArgument(ProcessingElementsContainer container, GraphQLFieldDefinition.Builder builder, boolean isConnection) {
        if (isConnection) {
            builder.argument(container.getRelay().getConnectionFieldArguments());
        }
    }

    private TypeFunction getTypeFunction(Method method, ProcessingElementsContainer container) {
        GraphQLType annotation = method.getAnnotation(GraphQLType.class);
        TypeFunction typeFunction = container.getDefaultTypeFunction();

        if (annotation != null) {
            typeFunction = newInstance(annotation.value());
        }
        return typeFunction;
    }

    private GraphQLFieldDefinition buildRelayMutation(Method method, ProcessingElementsContainer container, GraphQLFieldDefinition.Builder builder, GraphQLOutputType outputType, List<GraphQLArgument> args) {
        GraphQLFieldDefinition relayFieldDefinition;
        if (!(outputType instanceof GraphQLObjectType || outputType instanceof GraphQLInterfaceType)) {
            throw new RuntimeException("outputType should be an object or an interface");
        }
        StringBuilder titleBuffer = new StringBuilder(method.getName());
        titleBuffer.setCharAt(0, Character.toUpperCase(titleBuffer.charAt(0)));
        String title = titleBuffer.toString();
        List<GraphQLFieldDefinition> fieldDefinitions = outputType instanceof GraphQLObjectType ?
                ((GraphQLObjectType) outputType).getFieldDefinitions() :
                ((GraphQLInterfaceType) outputType).getFieldDefinitions();
        relayFieldDefinition = container.getRelay().mutationWithClientMutationId(title, method.getName(),
                args.stream().
                        map(t -> newInputObjectField().name(t.getName()).type(t.getType()).description(t.getDescription()).build()).
                        collect(Collectors.toList()), fieldDefinitions, new StaticDataFetcher(null));
        builder.argument(relayFieldDefinition.getArguments()).type(relayFieldDefinition.getType());
        return relayFieldDefinition;
    }


    private TypeFunction getTypeFunction(Field field, ProcessingElementsContainer container) {
        GraphQLType annotation = field.getAnnotation(GraphQLType.class);

        TypeFunction typeFunction = container.getDefaultTypeFunction();

        if (annotation != null) {
            typeFunction = newInstance(annotation.value());
        }
        return typeFunction;
    }

    private GraphQLOutputType getGraphQLConnection(AccessibleObject field, graphql.schema.GraphQLType type, Relay relay, Map<String, graphql.schema.GraphQLType> typeRegistry) {
        if (type instanceof GraphQLNonNull) {
            GraphQLList listType = (GraphQLList) ((GraphQLNonNull) type).getWrappedType();
            return new GraphQLNonNull(internalGetGraphQLConnection(field, listType, relay, typeRegistry));
        } else {
            return internalGetGraphQLConnection(field, (GraphQLList) type, relay, typeRegistry);
        }
    }

    private GraphQLOutputType internalGetGraphQLConnection(AccessibleObject field, GraphQLList listType, Relay relay, Map<String, graphql.schema.GraphQLType> typeRegistry) {
        GraphQLOutputType wrappedType = (GraphQLOutputType) listType.getWrappedType();
        String connectionName = field.getAnnotation(GraphQLConnection.class).name();
        connectionName = connectionName.isEmpty() ? wrappedType.getName() : connectionName;
        GraphQLObjectType edgeType = getActualType(relay.edgeType(connectionName, wrappedType, null, Collections.<GraphQLFieldDefinition>emptyList()), typeRegistry);
        return getActualType(relay.connectionType(connectionName, edgeType, Collections.emptyList()), typeRegistry);
    }

    private void addExtensionField(GraphQLFieldDefinition gqlField, List<GraphQLFieldDefinition> fields, List<String> fieldsDefined) {
        if (!fieldsDefined.contains(gqlField.getName())) {
            fieldsDefined.add(gqlField.getName());
            fields.add(gqlField);
        } else {
            throw new GraphQLAnnotationsException("Duplicate field found in extension : " + gqlField.getName(), null);
        }
    }

    private GraphQLObjectType getActualType(GraphQLObjectType type, Map<String, graphql.schema.GraphQLType> typeRegistry) {
        if (typeRegistry.containsKey(type.getName())) {
            type = (GraphQLObjectType) typeRegistry.get(type.getName());
        } else {
            typeRegistry.put(type.getName(), type);
        }
        return type;
    }

}
