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
package graphql.annotations;


import graphql.annotations.searchAlgorithms.BreadthFirstSearch;
import graphql.annotations.searchAlgorithms.CannotCastMemberException;
import graphql.annotations.searchAlgorithms.ParentalSearch;
import graphql.annotations.typeFunctions.BatchedTypeFunction;
import graphql.annotations.util.GraphQLObjectInfoRetriever;
import graphql.relay.Relay;
import graphql.schema.*;
import graphql.schema.GraphQLNonNull;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.annotations.ReflectionKit.constructNewInstance;
import static graphql.annotations.ReflectionKit.newInstance;
import static graphql.annotations.util.NamingKit.toGraphqlName;
import static graphql.annotations.util.ObjectUtil.getAllFields;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;

public class GraphQLFieldRetriever {

    private static final List<Class> TYPES_FOR_CONNECTION = Arrays.asList(GraphQLObjectType.class, GraphQLInterfaceType.class, GraphQLUnionType.class, GraphQLTypeReference.class);
    private static final String DEFAULT_INPUT_PREFIX = "Input";

    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;
    private BreadthFirstSearch breadthFirstSearch;
    private ParentalSearch parentalSearch;
    private GraphQLInputObjectRetriever graphQLInputObjectRetriever;

    public GraphQLFieldRetriever(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever, BreadthFirstSearch breadthFirstSearch, ParentalSearch parentalSearch, GraphQLInputObjectRetriever graphQLInputObjectRetriever) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
        this.breadthFirstSearch = breadthFirstSearch;
        this.parentalSearch = parentalSearch;
        this.graphQLInputObjectRetriever = graphQLInputObjectRetriever;
    }

    public GraphQLFieldRetriever() {
        this(new GraphQLObjectInfoRetriever(), new BreadthFirstSearch(new GraphQLObjectInfoRetriever()), new ParentalSearch(new GraphQLObjectInfoRetriever()), new GraphQLInputObjectRetriever() );
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
                        addExtensionField(getField(method,container), fields, fieldsDefined);
                    }
                }
                for (Field field : getAllFields(aClass).values()) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    if (parentalSearch.isFound(field)) {
                        addExtensionField(getField(field,container), fields, fieldsDefined);
                    }
                }
            }
        }
        return fields;
    }

    public GraphQLFieldDefinition getField(Method method, ProcessingElementsContainer container) throws GraphQLAnnotationsException {
        GraphQLFieldDefinition.Builder builder = newFieldDefinition();

        buildName(method, builder);

        GraphQLType annotation = method.getAnnotation(GraphQLType.class);
        TypeFunction typeFunction = container.getDefaultTypeFunction();

        if (annotation != null) {
            typeFunction = newInstance(annotation.value());
        }
        AnnotatedType annotatedReturnType = method.getAnnotatedReturnType();

        TypeFunction outputTypeFunction;
        if (method.getAnnotation(GraphQLBatched.class) != null) {
            outputTypeFunction = new BatchedTypeFunction(typeFunction);
        } else {
            outputTypeFunction = typeFunction;
        }

        GraphQLOutputType outputType = (GraphQLOutputType) outputTypeFunction.buildType(method.getReturnType(), annotatedReturnType,container);

        boolean isConnection = isConnection(method, outputType);
        if (isConnection) {
            outputType = getGraphQLConnection(method, outputType, container.getRelay(), container.getTypeRegistry());
            builder.argument(container.getRelay().getConnectionFieldArguments());
        }

        builder.type(outputType);


        TypeFunction finalTypeFunction = typeFunction;
        List<GraphQLArgument> args = Arrays.asList(method.getParameters()).stream().
                filter(p -> !DataFetchingEnvironment.class.isAssignableFrom(p.getType())).
                map(parameter -> {
                    Class<?> t = parameter.getType();
                    graphql.schema.GraphQLInputType graphQLType = graphQLInputObjectRetriever.getInputObject(finalTypeFunction.buildType(t, parameter.getAnnotatedType(),container), DEFAULT_INPUT_PREFIX,container.getTypeRegistry());
                    return getArgument(parameter, graphQLType);
                }).collect(Collectors.toList());

        GraphQLFieldDefinition relayFieldDefinition = null;
        if (method.isAnnotationPresent(GraphQLRelayMutation.class)) {
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
            builder.argument(relayFieldDefinition.getArguments());
            builder.type(relayFieldDefinition.getType());
        } else {
            builder.argument(args);
        }

        GraphQLDescription description = method.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            builder.description(description.value());
        }

        GraphQLDeprecate deprecate = method.getAnnotation(GraphQLDeprecate.class);
        if (deprecate != null) {
            builder.deprecate(deprecate.value());
        }
        if (method.getAnnotation(Deprecated.class) != null) {
            builder.deprecate("Deprecated");
        }

        GraphQLDataFetcher dataFetcher = method.getAnnotation(GraphQLDataFetcher.class);
        DataFetcher actualDataFetcher;
        if (dataFetcher == null && method.getAnnotation(GraphQLBatched.class) != null) {
            actualDataFetcher = new BatchedMethodDataFetcher(method,typeFunction, container);
        } else if (dataFetcher == null) {
            actualDataFetcher = new MethodDataFetcher(method,typeFunction, container);
        } else {
            actualDataFetcher = constructDataFetcher(method.getName(), dataFetcher);
        }

        if (method.isAnnotationPresent(GraphQLRelayMutation.class) && relayFieldDefinition != null) {
            actualDataFetcher = new RelayMutationMethodDataFetcher(method, args, relayFieldDefinition.getArgument("input").getType(), relayFieldDefinition.getType());
        }

        if (isConnection) {
            actualDataFetcher = new ConnectionDataFetcher(method.getAnnotation(GraphQLConnection.class).connection(), actualDataFetcher);
        }

        builder.dataFetcher(actualDataFetcher);

        return new GraphQLAnnotations.GraphQLFieldDefinitionWrapper(builder.build());
    }

    public GraphQLFieldDefinition getField(Field field,ProcessingElementsContainer container) throws GraphQLAnnotationsException {
        GraphQLFieldDefinition.Builder builder = newFieldDefinition();
        GraphQLName name = field.getAnnotation(GraphQLName.class);
        builder.name(toGraphqlName(name == null ? field.getName() : name.value()));
        GraphQLType annotation = field.getAnnotation(GraphQLType.class);

        TypeFunction typeFunction = container.getDefaultTypeFunction();

        if (annotation != null) {
            typeFunction = newInstance(annotation.value());
        }

        GraphQLOutputType outputType = (GraphQLOutputType) typeFunction.buildType(field.getType(), field.getAnnotatedType(),container);

        boolean isConnection = isConnection(field, outputType);
        if (isConnection) {
            outputType = getGraphQLConnection(field, outputType,container.getRelay(),container.getTypeRegistry());
            builder.argument(container.getRelay().getConnectionFieldArguments());
        }

        builder.type(outputType);

        GraphQLDescription description = field.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            builder.description(description.value());
        }

        GraphQLDeprecate deprecate = field.getAnnotation(GraphQLDeprecate.class);
        if (deprecate != null) {
            builder.deprecate(deprecate.value());
        }
        if (field.getAnnotation(Deprecated.class) != null) {
            builder.deprecate("Deprecated");
        }

        GraphQLDataFetcher dataFetcher = field.getAnnotation(GraphQLDataFetcher.class);
        DataFetcher actualDataFetcher = null;
        if (nonNull(dataFetcher)) {
            actualDataFetcher = constructDataFetcher(field.getName(), dataFetcher);
        }

        if (actualDataFetcher == null) {

            StringBuilder fluentBuffer = new StringBuilder(field.getName());
            fluentBuffer.setCharAt(0, Character.toLowerCase(fluentBuffer.charAt(0)));
            String fluentGetter = fluentBuffer.toString();

            boolean hasFluentGetter = false;
            Method fluentMethod = null;
            try {
                fluentMethod = field.getDeclaringClass().getMethod(fluentGetter);
                hasFluentGetter = true;
            } catch (NoSuchMethodException x) {
            }

            // if there is getter for fields type, use propertyDataFetcher, otherwise use method directly
            if (outputType == GraphQLBoolean || (outputType instanceof GraphQLNonNull && ((GraphQLNonNull) outputType).getWrappedType() == GraphQLBoolean)) {
                if (checkIfPrefixGetterExists(field.getDeclaringClass(), "is", field.getName()) ||
                        checkIfPrefixGetterExists(field.getDeclaringClass(), "get", field.getName())) {
                    actualDataFetcher = new ExtensionDataFetcherWrapper(field.getDeclaringClass(), new PropertyDataFetcher(field.getName()));
                }
            } else if (checkIfPrefixGetterExists(field.getDeclaringClass(), "get", field.getName())) {
                actualDataFetcher = new ExtensionDataFetcherWrapper(field.getDeclaringClass(), new PropertyDataFetcher(field.getName()));
            } else if (hasFluentGetter) {
                actualDataFetcher = new MethodDataFetcher(fluentMethod, typeFunction,container);
            }

            if (actualDataFetcher == null) {
                actualDataFetcher = new ExtensionDataFetcherWrapper(field.getDeclaringClass(), new FieldDataFetcher(field.getName()));
            }
        }


        if (isConnection) {
            actualDataFetcher = new ConnectionDataFetcher(field.getAnnotation(GraphQLConnection.class).connection(), actualDataFetcher);
        }

        builder.dataFetcher(actualDataFetcher);

        return new GraphQLAnnotations.GraphQLFieldDefinitionWrapper(builder.build());
    }

    private void buildName(Method method, GraphQLFieldDefinition.Builder builder) {
        String name = method.getName().replaceFirst("^(is|get|set)(.+)", "$2");
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        GraphQLName nameAnn = method.getAnnotation(GraphQLName.class);
        builder.name(toGraphqlName(nameAnn == null ? name : nameAnn.value()));
    }

    private boolean isConnection(AccessibleObject obj, GraphQLOutputType type) {
        if (type instanceof graphql.schema.GraphQLNonNull) {
            type = (GraphQLOutputType) ((GraphQLNonNull) type).getWrappedType();
        }
        final GraphQLOutputType actualType = type;
        return obj.isAnnotationPresent(GraphQLConnection.class) &&
                actualType instanceof GraphQLList && TYPES_FOR_CONNECTION.stream().anyMatch(aClass -> aClass.isInstance(((GraphQLList) actualType).getWrappedType()));
    }

    private GraphQLOutputType getGraphQLConnection(AccessibleObject field, GraphQLOutputType type, Relay relay, Map<String, graphql.schema.GraphQLType> typeRegistry) {
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

    protected GraphQLArgument getArgument(Parameter parameter, graphql.schema.GraphQLInputType t) throws
            GraphQLAnnotationsException {
        GraphQLArgument.Builder builder = newArgument().type(t);
        GraphQLDescription description = parameter.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            builder.description(description.value());
        }
        GraphQLDefaultValue defaultValue = parameter.getAnnotation(GraphQLDefaultValue.class);
        if (defaultValue != null) {
            builder.defaultValue(newInstance(defaultValue.value()).get());
        }
        GraphQLName name = parameter.getAnnotation(GraphQLName.class);
        if (name != null) {
            builder.name(toGraphqlName(name.value()));
        } else {
            builder.name(toGraphqlName(parameter.getName()));
        }
        return builder.build();
    }

    private DataFetcher constructDataFetcher(String fieldName, GraphQLDataFetcher annotatedDataFetcher) {
        final String[] args;
        if (annotatedDataFetcher.firstArgIsTargetName()) {
            args = Stream.concat(Stream.of(fieldName), stream(annotatedDataFetcher.args())).toArray(String[]::new);
        } else {
            args = annotatedDataFetcher.args();
        }
        if (args.length == 0) {
            return newInstance(annotatedDataFetcher.value());
        } else {
            try {
                final Constructor<? extends DataFetcher> ctr = annotatedDataFetcher.value().getDeclaredConstructor(
                        stream(args).map(v -> String.class).toArray(Class[]::new));
                return constructNewInstance(ctr, (Object[]) args);
            } catch (final NoSuchMethodException e) {
                throw new GraphQLAnnotationsException("Unable to instantiate DataFetcher via constructor for: " + fieldName, e);
            }
        }
    }


    // check if there is getter for field, basic functionality taken from PropertyDataFetcher
    private boolean checkIfPrefixGetterExists(Class c, String prefix, String propertyName) {
        String getterName = prefix + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        try {
            Method method = c.getMethod(getterName);
        } catch (NoSuchMethodException x) {
            return false;
        }

        return true;
    }

}
