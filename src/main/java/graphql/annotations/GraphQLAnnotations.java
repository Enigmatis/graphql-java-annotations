/**
 * Copyright 2016 Yurii Rashkovskii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations;

import graphql.relay.Relay;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldDataFetcher;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLUnionType;
import graphql.schema.PropertyDataFetcher;
import graphql.schema.TypeResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.validation.constraints.NotNull;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.annotations.ReflectionKit.constructNewInstance;
import static graphql.annotations.ReflectionKit.newInstance;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInterfaceType.newInterface;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLUnionType.newUnionType;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;

/**
 * A utility class for extracting GraphQL data structures from annotated
 * elements.
 */
@Component
public class GraphQLAnnotations implements GraphQLAnnotationsProcessor {

    private Map<String, graphql.schema.GraphQLType> typeRegistry = new HashMap<>();

    public GraphQLAnnotations() {
        defaultTypeFunction = new DefaultTypeFunction();
        ((DefaultTypeFunction) defaultTypeFunction).setAnnotationsProcessor(this);
    }

    public static GraphQLAnnotations instance = new GraphQLAnnotations();

    public static GraphQLAnnotations getInstance() {
        return instance;
    }

    @Override
    public graphql.schema.GraphQLType getInterface(Class<?> iface) throws GraphQLAnnotationsException {
        String typeName = getTypeName(iface);
        graphql.schema.GraphQLType type = typeRegistry.get(typeName);
        if (type != null) { // type already exists, do not build a new new one
            return type;
        }
        if (iface.getAnnotation(GraphQLUnion.class) != null) {
            type = getUnionBuilder(iface).build();
        } else if (!iface.isAnnotationPresent(GraphQLTypeResolver.class)) {
            type = getObject(iface);
        } else {
            type = getIfaceBuilder(iface).build();
        }
        typeRegistry.put(typeName, type);
        return type;
    }

    public static graphql.schema.GraphQLType iface(Class<?> iface) throws GraphQLAnnotationsException {
        return getInstance().getInterface(iface);
    }

    @Override
    public GraphQLUnionType.Builder getUnionBuilder(Class<?> iface) throws GraphQLAnnotationsException, IllegalArgumentException {
        if (!iface.isInterface()) {
            throw new IllegalArgumentException(iface + " is not an interface");
        }
        GraphQLUnionType.Builder builder = newUnionType();

        GraphQLUnion unionAnnotation = iface.getAnnotation(GraphQLUnion.class);
        builder.name(getTypeName(iface));
        GraphQLDescription description = iface.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            builder.description(description.value());
        }
        GraphQLType typeAnnotation = iface.getAnnotation(GraphQLType.class);

        TypeFunction typeFunction = defaultTypeFunction;

        if (typeAnnotation != null) {
            typeFunction = newInstance(typeAnnotation.value());
        }

        TypeFunction finalTypeFunction = typeFunction;
        Arrays.asList(unionAnnotation.possibleTypes()).stream()
                .map(new Function<Class<?>, graphql.schema.GraphQLType>() {
                    @Override
                    public graphql.schema.GraphQLType apply(Class<?> aClass) {
                        return finalTypeFunction.apply(aClass, null);
                    }
                })
                .map(v -> (GraphQLObjectType) v)
                .forEach(builder::possibleType);

        builder.typeResolver(new UnionTypeResolver(unionAnnotation.possibleTypes()));
        return builder;
    }

    public static GraphQLUnionType.Builder unionBuilder(Class<?> iface) throws GraphQLAnnotationsException {
        return getInstance().getUnionBuilder(iface);
    }

    public String getTypeName(Class<?> objectClass) {
        GraphQLName name = objectClass.getAnnotation(GraphQLName.class);
        return (name == null ? objectClass.getSimpleName() : name.value());
    }

    @Override
    public GraphQLInterfaceType.Builder getIfaceBuilder(Class<?> iface) throws GraphQLAnnotationsException,
            IllegalArgumentException {
        if (!iface.isInterface()) {
            throw new IllegalArgumentException(iface + " is not an interface");
        }
        GraphQLInterfaceType.Builder builder = newInterface();

        builder.name(getTypeName(iface));
        GraphQLDescription description = iface.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            builder.description(description.value());
        }
        for (Method method : getOrderedMethods(iface)) {
            boolean valid = !Modifier.isStatic(method.getModifiers()) &&
                    method.getAnnotation(GraphQLField.class) != null;
            if (valid) {
                builder.field(getField(method));
            }
        }
        GraphQLTypeResolver typeResolver = iface.getAnnotation(GraphQLTypeResolver.class);
        builder.typeResolver(newInstance(typeResolver.value()));
        return builder;
    }

    public static GraphQLInterfaceType.Builder ifaceBuilder(Class<?> iface) throws GraphQLAnnotationsException,
            IllegalAccessException {
        return getInstance().getIfaceBuilder(iface);
    }

    private static Boolean isGraphQLField(AnnotatedElement element) {
        GraphQLField annotation = element.getAnnotation(GraphQLField.class);
        if (annotation == null) {
            return null;
        }
        return annotation.value();
    }

    /**
     * breadthFirst parental ascent looking for closest method declaration with explicit annotation
     *
     * @param method The method to match
     * @return The closest GraphQLField annotation
     */
    private boolean breadthFirstSearch(Method method) {
        final List<Class<?>> queue = new LinkedList<>();
        final String methodName = method.getName();
        final Class<?>[] parameterTypes = method.getParameterTypes();
        queue.add(method.getDeclaringClass());
        do {
            Class<?> cls = queue.remove(0);

            try {
                method = cls.getDeclaredMethod(methodName, parameterTypes);
                Boolean gqf = isGraphQLField(method);
                if (gqf != null) {
                    return gqf;
                }
            } catch (NoSuchMethodException e) {
            }

            Boolean gqf = isGraphQLField(cls);
            if (gqf != null) {
                return gqf;
            }

            // add interfaces to places to search
            for (Class<?> iface : cls.getInterfaces()) {
                queue.add(iface);
            }
            // add parent class to places to search
            Class<?> nxt = cls.getSuperclass();
            if (nxt != null) {
                queue.add(nxt);
            }
        } while (!queue.isEmpty());
        return false;
    }

    /**
     * direct parental ascent looking for closest declaration with explicit annotation
     *
     * @param field The field to find
     * @return The closest GraphQLField annotation
     */
    private boolean parentalSearch(Field field) {
        Boolean gqf = isGraphQLField(field);
        if (gqf != null) {
            return gqf;
        }
        Class<?> cls = field.getDeclaringClass();

        do {
            gqf = isGraphQLField(cls);
            if (gqf != null) {
                return gqf;
            }
            cls = cls.getSuperclass();
        } while (cls != null);
        return false;
    }

    @Override
    public GraphQLObjectType getObject(Class<?> object) throws GraphQLAnnotationsException {
        GraphQLObjectType.Builder builder = getObjectBuilder(object);

        return new GraphQLObjectTypeWrapper(object, builder.build());
    }

    public static GraphQLObjectType object(Class<?> object) throws GraphQLAnnotationsException {
        return getInstance().getObject(object);
    }

    public static class GraphQLFieldDefinitionWrapper extends GraphQLFieldDefinition {

        public GraphQLFieldDefinitionWrapper(GraphQLFieldDefinition fieldDefinition) {
            super(fieldDefinition.getName(), fieldDefinition.getDescription(), fieldDefinition.getType(),
                    fieldDefinition.getDataFetcher(), fieldDefinition.getArguments(), fieldDefinition.getDeprecationReason());
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GraphQLFieldDefinition &&
                    ((GraphQLFieldDefinition) obj).getName().contentEquals(getName());
        }
    }

    @Override
    public GraphQLObjectType.Builder getObjectBuilder(Class<?> object) throws GraphQLAnnotationsException {
        GraphQLObjectType.Builder builder = newObject();
        builder.name(getTypeName(object));
        GraphQLDescription description = object.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            builder.description(description.value());
        }

        for (Method method : getOrderedMethods(object)) {
            if(method.isBridge() || method.isSynthetic()) {
                continue;
            }
            if (breadthFirstSearch(method)) {
                builder.field(getField(method));
            }
        }

        for (Field field : getAllFields(object).values()) {
            if(Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (parentalSearch(field)) {
                builder.field(getField(field));
            }
        }

        for (Class<?> iface : object.getInterfaces()) {
            if (iface.getAnnotation(GraphQLTypeResolver.class) != null) {
                builder.withInterface((GraphQLInterfaceType) getInterface(iface));
            }
        }
        return builder;
    }

    public static GraphQLObjectType.Builder objectBuilder(Class<?> object) throws GraphQLAnnotationsException {
        return getInstance().getObjectBuilder(object);
    }


    protected List<Method> getOrderedMethods(Class c) {
        return Arrays.stream(c.getMethods())
                .sorted(Comparator.comparing(Method::getName))
                .collect(Collectors.toList());
    }

    protected Map<String, Field> getAllFields(Class c) {
        Map<String, Field> fields;

        if (c.getSuperclass() != null) {
            fields = getAllFields(c.getSuperclass());
        } else {
            fields = new TreeMap<>();
        }

        for (Field f : c.getDeclaredFields()) {
            fields.put(f.getName(), f);
        }

        return fields;
    }


    protected GraphQLFieldDefinition getField(Field field) throws GraphQLAnnotationsException {
        GraphQLFieldDefinition.Builder builder = newFieldDefinition();
        GraphQLName name = field.getAnnotation(GraphQLName.class);
        builder.name(name == null ? field.getName() : name.value());
        GraphQLType annotation = field.getAnnotation(GraphQLType.class);

        TypeFunction typeFunction = defaultTypeFunction;

        if (annotation != null) {
            typeFunction = newInstance(annotation.value());
        }

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
        DataFetcher actualDataFetcher = null;
        if (nonNull(dataFetcher)) {
            final String[] args;
            if ( dataFetcher.firstArgIsTargetName() ) {
                args = Stream.concat(Stream.of(field.getName()), stream(dataFetcher.args())).toArray(String[]::new);
            } else {
                args = dataFetcher.args();
            }
            if (args.length == 0) {
                actualDataFetcher = newInstance(dataFetcher.value());
            } else {
                try {
                    final Constructor<? extends DataFetcher> ctr = dataFetcher.value().getDeclaredConstructor(
                      stream(args).map(v -> String.class).toArray(Class[]::new));
                    actualDataFetcher = constructNewInstance(ctr, (Object[]) args);
                } catch (final NoSuchMethodException e) {}
            }
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
                    actualDataFetcher = new PropertyDataFetcher(field.getName());
                }
            } else if (checkIfPrefixGetterExists(field.getDeclaringClass(), "get", field.getName())) {
                actualDataFetcher = new PropertyDataFetcher(field.getName());
            } else if (hasFluentGetter) {
                actualDataFetcher = new MethodDataFetcher(fluentMethod, typeFunction);
            }

            if (actualDataFetcher == null) {
                actualDataFetcher = new FieldDataFetcher(field.getName());
            }
        }


        if (isConnection) {
            actualDataFetcher = new ConnectionDataFetcher(field.getAnnotation(GraphQLConnection.class).connection(), actualDataFetcher);
        }

        builder.dataFetcher(actualDataFetcher);

        return new GraphQLFieldDefinitionWrapper(builder.build());
    }

    protected GraphQLFieldDefinition field(Field field) throws IllegalAccessException, InstantiationException {
        return getInstance().getField(field);
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

    private GraphQLOutputType getGraphQLConnection(boolean isConnection, AccessibleObject field, GraphQLOutputType type, GraphQLOutputType outputType, GraphQLFieldDefinition.Builder builder) {
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

    private boolean isConnection(AccessibleObject obj, Class<?> klass, GraphQLOutputType type) {
        return obj.isAnnotationPresent(GraphQLConnection.class) &&
                type instanceof GraphQLList &&
                ((GraphQLList) type).getWrappedType() instanceof GraphQLObjectType;
    }

    protected GraphQLFieldDefinition getField(Method method) throws GraphQLAnnotationsException {
        GraphQLFieldDefinition.Builder builder = newFieldDefinition();

        String name = method.getName().replaceFirst("^(is|get|set)(.+)", "$2");
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        GraphQLName nameAnn = method.getAnnotation(GraphQLName.class);
        builder.name(nameAnn == null ? name : nameAnn.value());

        GraphQLType annotation = method.getAnnotation(GraphQLType.class);
        TypeFunction typeFunction = defaultTypeFunction;

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

        GraphQLOutputType type = (GraphQLOutputType) outputTypeFunction.apply(method.getReturnType(), annotatedReturnType);
        GraphQLOutputType outputType = method.getAnnotation(NotNull.class) == null ? type : new GraphQLNonNull(type);

        boolean isConnection = isConnection(method, method.getReturnType(), type);
        outputType = getGraphQLConnection(isConnection, method, type, outputType, builder);

        builder.type(outputType);


        TypeFunction finalTypeFunction = typeFunction;
        List<GraphQLArgument> args = Arrays.asList(method.getParameters()).stream().
                filter(p -> !DataFetchingEnvironment.class.isAssignableFrom(p.getType())).
                map(parameter -> {
                    Class<?> t = parameter.getType();
                    graphql.schema.GraphQLType graphQLType = finalTypeFunction.apply(t, parameter.getAnnotatedType());
                    if (graphQLType instanceof GraphQLObjectType) {
                        GraphQLInputObjectType inputObject = getInputObject((GraphQLObjectType) graphQLType);
                        graphQLType = inputObject;
                    }
                    return getArgument(parameter, graphQLType);
                }).collect(Collectors.toList());

        GraphQLFieldDefinition relay = null;
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
        DataFetcher actualDataFetcher;
        if (dataFetcher == null && method.getAnnotation(GraphQLBatched.class) != null) {
            actualDataFetcher = new BatchedMethodDataFetcher(method, typeFunction);
        } else if (dataFetcher == null) {
            actualDataFetcher = new MethodDataFetcher(method, typeFunction);
        } else {
            actualDataFetcher = newInstance(dataFetcher.value());
        }

        if (method.isAnnotationPresent(GraphQLRelayMutation.class) && relay != null) {
            actualDataFetcher = new RelayMutationMethodDataFetcher(method, args, relay.getArgument("input").getType(), relay.getType());
        }

        if (isConnection) {
            actualDataFetcher = new ConnectionDataFetcher(method.getAnnotation(GraphQLConnection.class).connection(), actualDataFetcher);
        }

        builder.dataFetcher(actualDataFetcher);

        return new GraphQLFieldDefinitionWrapper(builder.build());
    }

    protected static GraphQLFieldDefinition field(Method method) throws InstantiationException, IllegalAccessException {
        return getInstance().getField(method);

    }

    @Override
    public GraphQLInputObjectType getInputObject(GraphQLObjectType graphQLType) {
        GraphQLObjectType object = graphQLType;
        return new GraphQLInputObjectType(object.getName(), object.getDescription(),
                object.getFieldDefinitions().stream().
                        map(field -> {
                            GraphQLOutputType type = field.getType();
                            GraphQLInputType inputType;
                            if (type instanceof GraphQLObjectType) {
                                inputType = getInputObject((GraphQLObjectType) type);
                            } else {
                                inputType = (GraphQLInputType) type;
                            }

                            return new GraphQLInputObjectField(field.getName(), field.getDescription(), inputType, null);
                        }).
                        collect(Collectors.toList()));
    }

    public static GraphQLInputObjectType inputObject(GraphQLObjectType graphQLType) {
        return getInstance().getInputObject(graphQLType);
    }

    protected GraphQLArgument getArgument(Parameter parameter, graphql.schema.GraphQLType t) throws
            GraphQLAnnotationsException {
        GraphQLArgument.Builder builder = newArgument();
        builder.type(parameter.getAnnotation(NotNull.class) == null ? (GraphQLInputType) t : new graphql.schema.GraphQLNonNull(t));
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
            builder.name(name.value());
        } else {
            builder.name(parameter.getName());
        }
        return builder.build();
    }

    protected TypeFunction defaultTypeFunction;

    @Reference(target = "(type=default)")
    public void setDefaultTypeFunction(TypeFunction function) {
        defaultTypeFunction = function;
        ((DefaultTypeFunction) defaultTypeFunction).setAnnotationsProcessor(this);
    }

    public void registerType(TypeFunction typeFunction) {
        ((DefaultTypeFunction) defaultTypeFunction).register(typeFunction);
    }

    public static void register(TypeFunction typeFunction) {
        getInstance().registerType(typeFunction);
    }

    public Map<String, graphql.schema.GraphQLType> getTypeRegistry() {
        return typeRegistry;
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

        @Override
        public Object get(DataFetchingEnvironment environment) {
            // Exclude arguments
            DataFetchingEnvironment env = new DataFetchingEnvironment(environment.getSource(), new HashMap<>(), environment.getContext(),
                    environment.getFields(), environment.getFieldType(), environment.getParentType(), environment.getGraphQLSchema());
            Connection conn = constructNewInstance(constructor, actualDataFetcher.get(env));
            return conn.get(environment);
        }
    }

    private class UnionTypeResolver implements TypeResolver {
        private final Map<Class<?>, graphql.schema.GraphQLType> types = new HashMap<>();

        public UnionTypeResolver(Class<?>[] classes) {
            Arrays.asList(classes).stream().
                    forEach(c -> types.put(c, defaultTypeFunction.apply(c, null)));
        }

        @Override
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
