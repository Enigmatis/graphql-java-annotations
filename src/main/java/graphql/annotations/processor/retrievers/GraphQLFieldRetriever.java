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
import graphql.annotations.connection.GraphQLConnection;
import graphql.annotations.connection.TypesConnectionChecker;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.retrievers.fieldBuilders.ArgumentBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.DeprecateBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.DescriptionBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.field.FieldDataFetcherBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.field.FieldNameBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.method.MethodDataFetcherBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.method.MethodNameBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.method.MethodTypeBuilder;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.annotations.processor.util.ConnectionUtil;
import graphql.annotations.processor.util.DataFetcherConstructor;
import graphql.relay.Relay;
import graphql.schema.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static graphql.annotations.processor.util.ReflectionKit.newInstance;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;

@Component(service = GraphQLFieldRetriever.class, immediate = true)
public class GraphQLFieldRetriever {

    private DataFetcherConstructor dataFetcherConstructor;

    public GraphQLFieldRetriever(DataFetcherConstructor dataFetcherConstructor) {
        this.dataFetcherConstructor = dataFetcherConstructor;
    }

    public GraphQLFieldRetriever() {
        this(new DataFetcherConstructor());
    }

    public GraphQLFieldDefinition getField(Method method, ProcessingElementsContainer container) throws GraphQLAnnotationsException {
        GraphQLFieldDefinition.Builder builder = newFieldDefinition();
        TypeFunction typeFunction = getTypeFunction(method, container);
        builder.name(new MethodNameBuilder(method).build());
        GraphQLOutputType outputType = (GraphQLOutputType) new MethodTypeBuilder(method, typeFunction, container, false).build();

        TypesConnectionChecker typesConnectionChecker = new TypesConnectionChecker();
        boolean isConnection = ConnectionUtil.isConnection(method, outputType);
        if (isConnection) {
            typesConnectionChecker.setConnection(true);
            outputType = getGraphQLConnection(method, outputType, container.getRelay(), container.getTypeRegistry());
            builder.argument(container.getRelay().getConnectionFieldArguments());
        }
        boolean isSimpleConnection = ConnectionUtil.isSimpleConnection(method, outputType);
        if (isSimpleConnection) {
            typesConnectionChecker.setSimpleConnection(true);
            builder.argument(container.getRelay().getConnectionFieldArguments());
        }

        builder.type(outputType);
        List<GraphQLArgument> args = new ArgumentBuilder(method, typeFunction, builder, container, outputType).build();
        GraphQLFieldDefinition relayFieldDefinition = handleRelayArguments(method, container, builder, outputType, args);
        builder.description(new DescriptionBuilder(method).build())
                .deprecate(new DeprecateBuilder(method).build())
                .dataFetcher(new MethodDataFetcherBuilder(method, outputType, typeFunction, container, relayFieldDefinition, args, dataFetcherConstructor, typesConnectionChecker).build());
        return new GraphQLFieldDefinitionWrapper(builder.build());
    }

    public GraphQLFieldDefinition getField(Field field, ProcessingElementsContainer container) throws GraphQLAnnotationsException {
        GraphQLFieldDefinition.Builder builder = newFieldDefinition();
        builder.name(new FieldNameBuilder(field).build());
        TypeFunction typeFunction = getTypeFunction(field, container);

        GraphQLType outputType = typeFunction.buildType(field.getType(), field.getAnnotatedType(), container);

        TypesConnectionChecker typesConnectionChecker = new TypesConnectionChecker();
        boolean isConnection = ConnectionUtil.isConnection(field, outputType);
        if (isConnection) {
            typesConnectionChecker.setConnection(true);
            outputType = getGraphQLConnection(field, outputType, container.getRelay(), container.getTypeRegistry());
            builder.argument(container.getRelay().getConnectionFieldArguments());
        }
        boolean isSimpleConnection = ConnectionUtil.isSimpleConnection(field, outputType);
        if (isSimpleConnection) {
            typesConnectionChecker.setSimpleConnection(true);
            builder.argument(container.getRelay().getConnectionFieldArguments());
        }

        builder.type((GraphQLOutputType) outputType).description(new DescriptionBuilder(field).build())
                .deprecate(new DeprecateBuilder(field).build())
                .dataFetcher(new FieldDataFetcherBuilder(field, dataFetcherConstructor, outputType, typeFunction, container, typesConnectionChecker).build());

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
        GraphQLType graphQLType = typeFunction.buildType(true, field.getType(), field.getAnnotatedType(), container);
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

    private TypeFunction getTypeFunction(Method method, ProcessingElementsContainer container) {
        graphql.annotations.annotationTypes.GraphQLType annotation = method.getAnnotation(graphql.annotations.annotationTypes.GraphQLType.class);
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
        graphql.annotations.annotationTypes.GraphQLType annotation = field.getAnnotation(graphql.annotations.annotationTypes.GraphQLType.class);

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

    private GraphQLObjectType getActualType(GraphQLObjectType type, Map<String, graphql.schema.GraphQLType> typeRegistry) {
        if (typeRegistry.containsKey(type.getName())) {
            type = (GraphQLObjectType) typeRegistry.get(type.getName());
        } else {
            typeRegistry.put(type.getName(), type);
        }
        return type;
    }

    @Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setDataFetcherConstructor(DataFetcherConstructor dataFetcherConstructor) {
        this.dataFetcherConstructor = dataFetcherConstructor;
    }

    public void unsetDataFetcherConstructor(DataFetcherConstructor dataFetcherConstructor) {
        this.dataFetcherConstructor = new DataFetcherConstructor();
    }
}
