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

import graphql.relay.Relay;
import graphql.schema.*;
import graphql.schema.GraphQLNonNull;
import lombok.SneakyThrows;

import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInterfaceType.newInterface;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLUnionType.newUnionType;

/**
 * A utility class for extracting GraphQL data structures from annotated
 * elements.
 */
public class GraphQLAnnotations {

    /**
     * Extract GraphQLInterfaceType from an interface
     * @param iface interface
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException if <code>iface</code> is not an interface or doesn't have <code>@GraphTypeResolver</code> annotation
     */
    public static graphql.schema.GraphQLType iface(Class<?> iface) throws IllegalAccessException, InstantiationException {
        if (iface.getAnnotation(GraphQLUnion.class) != null) {
            return unionBuilder(iface).build();
        } else {
            return ifaceBuilder(iface).build();
        }
    }

    public static GraphQLUnionType.Builder unionBuilder(Class<?> iface) throws InstantiationException, IllegalAccessException {
        if (!iface.isInterface()) {
            throw new IllegalArgumentException(iface + " is not an interface");
        }
        GraphQLUnionType.Builder builder = newUnionType();

        GraphQLUnion unionAnnotation = iface.getAnnotation(GraphQLUnion.class);
        GraphQLName name = iface.getAnnotation(GraphQLName.class);
        builder.name(name == null ? iface.getSimpleName() : name.value());
        GraphQLDescription description = iface.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            builder.description(description.value());
        }
        GraphQLType typeAnnotation = iface.getAnnotation(GraphQLType.class);
        if (typeAnnotation == null) {
            typeAnnotation = new defaultGraphQLType();
        }
        TypeFunction typeFunction = typeAnnotation.value().newInstance();

        Arrays.asList(unionAnnotation.possibleTypes()).stream()
                .map(new Function<Class<?>, graphql.schema.GraphQLType>() {
                    @Override
                    @SneakyThrows
                    public graphql.schema.GraphQLType apply(Class<?> aClass) {
                        return typeFunction.apply(aClass, null);
                    }
                })
                .map(v -> (GraphQLObjectType)v)
                .forEach(builder::possibleType);

        builder.typeResolver(new UnionTypeResolver(unionAnnotation.possibleTypes()));
        return builder;
    }

    public static GraphQLInterfaceType.Builder ifaceBuilder(Class<?> iface) throws InstantiationException, IllegalAccessException {
        if (!iface.isInterface()) {
            throw new IllegalArgumentException(iface + " is not an interface");
        }
        GraphQLInterfaceType.Builder builder = newInterface();

        GraphQLName name = iface.getAnnotation(GraphQLName.class);
        builder.name(name == null ? iface.getSimpleName() : name.value());
        GraphQLDescription description = iface.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            builder.description(description.value());
        }
        for (Method method : iface.getMethods()) {
            boolean valid = !Modifier.isStatic(method.getModifiers()) &&
                    method.getAnnotation(GraphQLField.class) != null;
            if (valid) {
                builder.field(field(method));
            }
        }
        GraphQLTypeResolver typeResolver = iface.getAnnotation(GraphQLTypeResolver.class);
        if (typeResolver == null) {
            throw new IllegalArgumentException(iface + " should have @GraphQLTypeResolver annotation defined");
        }
        builder.typeResolver(typeResolver.value().newInstance());
        return builder;
    }

    private static Class<?> getDeclaringClass(Method method) {
        Class<?> object = method.getDeclaringClass();
        Class<?> declaringClass = object;
        for (Class<?> iface : object.getInterfaces()) {
            try {
                iface.getMethod(method.getName(), method.getParameterTypes());
                declaringClass = iface;
            } catch (NoSuchMethodException e) {
            }
        }

        try {
            if (object.getSuperclass() != null) {
                object.getSuperclass().getMethod(method.getName(), method.getParameterTypes());
                declaringClass = object.getSuperclass();
            }
        } catch (NoSuchMethodException e) {
        }
        return declaringClass;

    }

    /**
     * Extract GraphQLObjectType from a class
     * @param object
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     */
    public static GraphQLObjectType object(Class<?> object) throws IllegalAccessException, InstantiationException, NoSuchMethodException {
        GraphQLObjectType.Builder builder = objectBuilder(object);

        return new GraphQLObjectTypeWrapper(builder.build());
    }

    public static class GraphQLObjectTypeWrapper extends GraphQLObjectType {

        public GraphQLObjectTypeWrapper(GraphQLObjectType objectType) {
            super(objectType.getName(), objectType.getDescription(), objectType.getFieldDefinitions(),
                    objectType.getInterfaces());
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GraphQLObjectType &&
                   ((GraphQLObjectType) obj).getName().contentEquals(getName()) &&
                   ((GraphQLObjectType) obj).getFieldDefinitions().equals(getFieldDefinitions());
        }
    }

    public static class GraphQLFieldDefinitionWrapper extends GraphQLFieldDefinition {

        public GraphQLFieldDefinitionWrapper(GraphQLFieldDefinition fieldDefinition) {
            super(fieldDefinition.getName(), fieldDefinition.getDescription(), fieldDefinition.getType(),
                    fieldDefinition.getDataFetcher(),fieldDefinition.getArguments(), fieldDefinition.getDeprecationReason());
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GraphQLFieldDefinition &&
                    ((GraphQLFieldDefinition) obj).getName().contentEquals(getName());
        }
    }

    public static GraphQLObjectType.Builder objectBuilder(Class<?> object) throws NoSuchMethodException, InstantiationException, IllegalAccessException {
        GraphQLObjectType.Builder builder = newObject();
        GraphQLName name = object.getAnnotation(GraphQLName.class);
        builder.name(name == null ? object.getSimpleName() : name.value());
        GraphQLDescription description = object.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            builder.description(description.value());
        }
        for (Method method : object.getMethods()) {

            Class<?> declaringClass = getDeclaringClass(method);

            boolean valid = (method.getAnnotation(GraphQLField.class) != null ||
                     declaringClass.getMethod(method.getName(), method.getParameterTypes()).getAnnotation(GraphQLField.class) != null);

            if (valid) {
                builder.field(field(method));
            }
        }
        for (Field field : object.getFields()) {
            boolean valid = !Modifier.isStatic(field.getModifiers()) &&
                    field.getAnnotation(GraphQLField.class) != null;
            if (valid) {
                builder.field(field(field));
            }
        }
        for (Class<?> iface : object.getInterfaces()) {
            if (iface.getAnnotation(GraphQLTypeResolver.class) != null) {
                builder.withInterface((GraphQLInterfaceType) iface(iface));
            }
        }
        return builder;
    }


    protected static GraphQLFieldDefinition field(Field field) throws IllegalAccessException, InstantiationException {
        GraphQLFieldDefinition.Builder builder = newFieldDefinition();
        GraphQLName name = field.getAnnotation(GraphQLName.class);
        builder.name(name == null ? field.getName() : name.value());
        GraphQLType annotation = field.getAnnotation(GraphQLType.class);
        if (annotation == null) {
            annotation = new defaultGraphQLType();
        }

        TypeFunction typeFunction = annotation.value().newInstance();
        GraphQLOutputType type = (GraphQLOutputType) typeFunction.apply(field.getType(), field.getAnnotatedType());

        GraphQLOutputType outputType = field.getAnnotation(NotNull.class) == null ? type : new GraphQLNonNull(type);

        boolean isConnection = isConnection(field, field.getType(), type);
        outputType = getGraphQLConnection(isConnection, field, type, outputType, builder);

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
        DataFetcher actualDataFetcher = dataFetcher == null ? new FieldDataFetcher(field.getName()) : dataFetcher.value().newInstance();


        if (isConnection) {
            actualDataFetcher = new ConnectionDataFetcher(field.getAnnotation(GraphQLConnection.class).connection(), actualDataFetcher);
        }

        builder.dataFetcher(actualDataFetcher);

        return new GraphQLFieldDefinitionWrapper(builder.build());
    }

    private static GraphQLOutputType getGraphQLConnection(boolean isConnection, AccessibleObject field, GraphQLOutputType type, GraphQLOutputType outputType, GraphQLFieldDefinition.Builder builder) {
        if (isConnection) {
            if (type instanceof GraphQLList) {
                graphql.schema.GraphQLType wrappedType = ((GraphQLList) type).getWrappedType();
                assert wrappedType instanceof GraphQLObjectType;
                String annValue = field.getAnnotation(GraphQLConnection.class).name();
                String connectionName = annValue.isEmpty() ? wrappedType.getName() : annValue;
                Relay relay = new Relay();
                GraphQLObjectType edgeType = relay.edgeType(connectionName, (GraphQLOutputType) wrappedType, null, Collections.<GraphQLFieldDefinition>emptyList());
                outputType = relay.connectionType(connectionName, edgeType, Collections.emptyList());
                builder.argument(relay.getConnectionFieldArguments());
            }
        }
        return outputType;
    }

    private static boolean isConnection(AccessibleObject obj, Class<?> klass, GraphQLOutputType type) {
        return obj.isAnnotationPresent(GraphQLConnection.class) &&
                               type instanceof GraphQLList &&
                               ((GraphQLList) type).getWrappedType() instanceof GraphQLObjectType;
    }

    protected static GraphQLFieldDefinition field(Method method) throws InstantiationException, IllegalAccessException {
        GraphQLFieldDefinition.Builder builder = newFieldDefinition();

        String name = method.getName().replaceFirst("^(is|get|set)(.+)", "$2");
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        GraphQLName nameAnn = method.getAnnotation(GraphQLName.class);
        builder.name(nameAnn == null ? name : nameAnn.value());

        GraphQLType annotation = method.getAnnotation(GraphQLType.class);
        if (annotation == null) {
            annotation = new defaultGraphQLType();
        }
        TypeFunction typeFunction = annotation.value().newInstance();
        AnnotatedType annotatedReturnType = method.getAnnotatedReturnType();

        GraphQLOutputType type = (GraphQLOutputType) typeFunction.apply(method.getReturnType(), annotatedReturnType);

        GraphQLOutputType outputType = method.getAnnotation(NotNull.class) == null ? type : new GraphQLNonNull(type);

        boolean isConnection = isConnection(method, method.getReturnType(), type);
        outputType = getGraphQLConnection(isConnection, method, type, outputType, builder);

        builder.type(outputType);


        List<GraphQLArgument> args = Arrays.asList(method.getParameters()).stream().
                filter(p -> !DataFetchingEnvironment.class.isAssignableFrom(p.getType())).
                map(new Function<Parameter, GraphQLArgument>() {
                    @Override @SneakyThrows
                    public GraphQLArgument apply(Parameter parameter) {
                        Class<?> t = parameter.getType();
                        graphql.schema.GraphQLType graphQLType = typeFunction.apply(t, parameter.getAnnotatedType());
                        if (graphQLType instanceof GraphQLObjectType) {
                            GraphQLInputObjectType inputObject = inputObject((GraphQLObjectType) graphQLType);
                            graphQLType = inputObject;
                        }
                        return argument(parameter, graphQLType);
                    }
                }).collect(Collectors.toList());

        GraphQLFieldDefinition relay = null;
        if (method.isAnnotationPresent(GraphQLRelayMutation.class)) {
            if (!(outputType instanceof GraphQLObjectType || outputType instanceof GraphQLInterfaceType)) {
                throw new RuntimeException("outputType should be an object or an interface");
            }
            StringBuffer titleBuffer = new StringBuffer(method.getName());
            titleBuffer.setCharAt(0, Character.toUpperCase(titleBuffer.charAt(0)));
            String title = titleBuffer.toString();
            List<GraphQLFieldDefinition> fieldDefinitions = outputType instanceof GraphQLObjectType ?
                    ((GraphQLObjectType) outputType).getFieldDefinitions() :
                    ((GraphQLInterfaceType) outputType).getFieldDefinitions();
            relay = new Relay().mutationWithClientMutationId(title, method.getName(),
                     args.stream().
                             map(t -> newInputObjectField().name(t.getName()).type(t.getType()).description(t.getDescription()).build()).
                            collect(Collectors.toList()), fieldDefinitions, null);
            builder.argument(relay.getArguments());
            builder.type(relay.getType());
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
        DataFetcher actualDataFetcher = dataFetcher == null ? new MethodDataFetcher(method) : dataFetcher.value().newInstance();

        if (method.isAnnotationPresent(GraphQLRelayMutation.class) && relay != null) {
            actualDataFetcher = new RelayMutationMethodDataFetcher(method, args, relay.getArgument("input").getType(), relay.getType());
        }

        if (isConnection) {
            actualDataFetcher = new ConnectionDataFetcher(method.getAnnotation(GraphQLConnection.class).connection(), actualDataFetcher);
        }

        builder.dataFetcher(actualDataFetcher);

        return new GraphQLFieldDefinitionWrapper(builder.build());
    }

    public static GraphQLInputObjectType inputObject(GraphQLObjectType graphQLType) {
        GraphQLObjectType object = graphQLType;
        return new GraphQLInputObjectType(object.getName(), object.getDescription(),
                object.getFieldDefinitions().stream().
                        map(field -> {
                            GraphQLOutputType type = field.getType();
                            GraphQLInputType inputType;
                            if (type instanceof GraphQLObjectType) {
                                inputType = inputObject((GraphQLObjectType) type);
                            } else {
                                inputType = (GraphQLInputType) type;
                            }

                            return new GraphQLInputObjectField(field.getName(), field.getDescription(), inputType, null);
                        }).
                        collect(Collectors.toList()));
    }

    protected static GraphQLArgument argument(Parameter parameter, graphql.schema.GraphQLType t) throws IllegalAccessException, InstantiationException {
        GraphQLArgument.Builder builder = newArgument();
        builder.type(parameter.getAnnotation(NotNull.class) == null ? (GraphQLInputType) t : new graphql.schema.GraphQLNonNull(t));
        GraphQLDescription description = parameter.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            builder.description(description.value());
        }
        GraphQLDefaultValue defaultValue = parameter.getAnnotation(GraphQLDefaultValue.class);
        if (defaultValue != null) {
            builder.defaultValue(defaultValue.value().newInstance().get());
        }
        GraphQLName name = parameter.getAnnotation(GraphQLName.class);
        if ( name != null ) {
            builder.name(name.value());
        } else {
            builder.name(parameter.getName());
        }
        return builder.build();
    }

    private static class defaultGraphQLType implements GraphQLType {

        @Override
        public Class<? extends Annotation> annotationType() {
            return GraphQLType.class;
        }

        @Override
        public Class<? extends TypeFunction> value() {
            return DefaultTypeFunction.class;
        }
    }

    private static class ConnectionDataFetcher implements DataFetcher {
        private final Class<? extends Connection> connection;
        private final DataFetcher actualDataFetcher;
        private final Constructor<Connection> constructor;

        public ConnectionDataFetcher(Class<? extends Connection> connection, DataFetcher actualDataFetcher) {
            this.connection = connection;
            Optional<Constructor<Connection>> constructor =
                    Arrays.asList(connection.getConstructors()).stream().
                            filter(c -> c.getParameterCount() == 1).
                            map(c -> (Constructor<Connection>) c).
                            findFirst();
            if (constructor.isPresent()) {
                this.constructor = constructor.get();
            } else {
                throw new IllegalArgumentException(connection + " doesn't have a single argument constructor");
            }
            this.actualDataFetcher = actualDataFetcher;
        }

        @Override @SneakyThrows
        public Object get(DataFetchingEnvironment environment) {
            // Exclude arguments
            DataFetchingEnvironment env = new DataFetchingEnvironment(environment.getSource(), new HashMap<>(), environment.getContext(),
                    environment.getFields(), environment.getFieldType(), environment.getParentType(), environment.getGraphQLSchema());
            Connection conn = constructor.newInstance(actualDataFetcher.get(env));
            return conn.get(environment);
        }
    }

    private static class UnionTypeResolver implements TypeResolver {
        private final Map<Class<?>, graphql.schema.GraphQLType> types = new HashMap<>();

        public UnionTypeResolver(Class<?>[] classes) {
            Arrays.asList(classes).stream().
                    forEach(c -> types.put(c, DefaultTypeFunction.instance.apply(c, null)));
        }

        @Override @SneakyThrows
        public GraphQLObjectType getType(Object object) {
            Optional<Map.Entry<Class<?>, graphql.schema.GraphQLType>> maybeType = types.entrySet().
                    stream().filter(e -> e.getKey().isAssignableFrom(object.getClass())).findFirst();
            if (maybeType.isPresent()) {
                return (GraphQLObjectType) maybeType.get().getValue();
            } else {
                throw new RuntimeException("Unknown type " + object.getClass());
            }
        }
    }
}
