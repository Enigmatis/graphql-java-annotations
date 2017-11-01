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

import graphql.TypeResolutionEnvironment;
import graphql.annotations.connection.ConnectionDataFetcher;
import graphql.annotations.connection.ConnectionTypeValidator;
import graphql.annotations.connection.GraphQLConnection;
import graphql.relay.Relay;
import graphql.schema.*;
import graphql.schema.GraphQLNonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.annotations.ReflectionKit.constructNewInstance;
import static graphql.annotations.ReflectionKit.newInstance;
import static graphql.annotations.util.NamingKit.toGraphqlName;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLEnumType.newEnum;
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

    private static final List<Class> TYPES_FOR_CONNECTION = Arrays.asList(GraphQLObjectType.class, GraphQLInterfaceType.class, GraphQLUnionType.class, GraphQLTypeReference.class);

    private static final String DEFAULT_INPUT_PREFIX = "Input";

    private Map<String, graphql.schema.GraphQLType> typeRegistry = new HashMap<>();
    private Map<Class<?>, Set<Class<?>>> extensionsTypeRegistry = new HashMap<>();
    private final Stack<String> processing = new Stack<>();
    private Relay relay = new Relay();

    public GraphQLAnnotations() {
        this(new DefaultTypeFunction());
        ((DefaultTypeFunction) defaultTypeFunction).setAnnotationsProcessor(this);
    }

    public GraphQLAnnotations(TypeFunction defaultTypeFunction) {
        this.defaultTypeFunction = defaultTypeFunction;
    }

    public static GraphQLAnnotations instance = new GraphQLAnnotations();

    public static GraphQLAnnotations getInstance() {
        return instance;
    }

    public void setRelay(Relay relay) {
        this.relay = relay;
    }

    @Override
    public graphql.schema.GraphQLOutputType getInterface(Class<?> iface) throws GraphQLAnnotationsException {
        return getOutputType(iface);
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
                        return finalTypeFunction.buildType(aClass, null);
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
        return toGraphqlName(name == null ? objectClass.getSimpleName() : name.value());
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
        List<String> definedFields = new ArrayList<>();
        for (Method method : getOrderedMethods(iface)) {
            boolean valid = !Modifier.isStatic(method.getModifiers()) &&
                    method.getAnnotation(GraphQLField.class) != null;
            if (valid) {
                GraphQLFieldDefinition gqlField = getField(method);
                definedFields.add(gqlField.getName());
                builder.field(gqlField);
            }
        }
        builder.fields(getExtensionFields(iface, definedFields));

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
        GraphQLOutputType type = getOutputType(object);
        if (type instanceof GraphQLObjectType) {
            return (GraphQLObjectType) type;
        } else {
            throw new IllegalArgumentException("Object resolve to a " + type.getClass().getSimpleName());
        }
    }

    @Override
    public GraphQLOutputType getOutputType(Class<?> object) throws GraphQLAnnotationsException {
        // because the TypeFunction can call back to this processor and
        // Java classes can be circular, we need to protect against
        // building the same type twice because graphql-java 3.x requires
        // all type instances to be unique singletons
        String typeName = getTypeName(object);

        GraphQLOutputType type = (GraphQLOutputType) typeRegistry.get(typeName);
        if (type != null) { // type already exists, do not build a new new one
            return type;
        }

        processing.push(typeName);
        if (object.getAnnotation(GraphQLUnion.class) != null) {
            type = getUnionBuilder(object).build();
        } else if (object.isAnnotationPresent(GraphQLTypeResolver.class)) {
            type = getIfaceBuilder(object).build();
        } else if (Enum.class.isAssignableFrom(object)) {
            type = getEnumBuilder(object).build();
        } else {
            type = getObjectBuilder(object).build();
        }

        typeRegistry.put(typeName, type);
        processing.pop();

        return type;
    }

    public static GraphQLOutputType outputType(Class<?> object) {
        return getInstance().getOutputType(object);
    }

    public GraphQLEnumType.Builder getEnumBuilder(Class<?> aClass) {
        String typeName = getTypeName(aClass);
        //noinspection unchecked
        Class<? extends Enum> enumClass = (Class<? extends Enum>) aClass;
        GraphQLEnumType.Builder builder = newEnum();
        builder.name(typeName);

        GraphQLDescription description = aClass.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            builder.description(description.value());
        }

        List<Enum> constants = Arrays.asList(enumClass.getEnumConstants());

        Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).forEachOrdered(n -> {
            try {
                Field field = aClass.getField(n);
                GraphQLName fieldName = field.getAnnotation(GraphQLName.class);
                GraphQLDescription fieldDescription = field.getAnnotation(GraphQLDescription.class);
                Enum constant = constants.stream().filter(c -> c.name().contentEquals(n)).findFirst().get();
                String name_ = fieldName == null ? n : fieldName.value();
                builder.value(name_, constant, fieldDescription == null ? name_ : fieldDescription.value());
            } catch (NoSuchFieldException ignore) {
            }
        });
        return builder;
    }

    public static GraphQLEnumType.Builder enumBuilder(Class<?> object) throws GraphQLAnnotationsException {
        return getInstance().getEnumBuilder(object);
    }

    public GraphQLOutputType getObjectOrRef(Class<?> object) throws GraphQLAnnotationsException {
        return getOutputTypeOrRef(object);
    }

    @Override
    public GraphQLOutputType getOutputTypeOrRef(Class<?> object) throws GraphQLAnnotationsException {
        String typeName = getTypeName(object);
        if (processing.contains(typeName)) {
            return new GraphQLTypeReference(typeName);
        }

        return getOutputType(object);
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
        List<String> fieldsDefined = new ArrayList<>();
        for (Method method : getOrderedMethods(object)) {
            if (method.isBridge() || method.isSynthetic()) {
                continue;
            }
            if (breadthFirstSearch(method)) {
                GraphQLFieldDefinition gqlField = getField(method);
                fieldsDefined.add(gqlField.getName());
                builder.field(gqlField);
            }
        }

        for (Field field : getAllFields(object).values()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (parentalSearch(field)) {
                GraphQLFieldDefinition gqlField = getField(field);
                fieldsDefined.add(gqlField.getName());
                builder.field(gqlField);
            }
        }

        for (Class<?> iface : object.getInterfaces()) {
            if (iface.getAnnotation(GraphQLTypeResolver.class) != null) {
                String ifaceName = getTypeName(iface);
                if (processing.contains(ifaceName)) {
                    builder.withInterface(new GraphQLTypeReference(ifaceName));
                } else {
                    builder.withInterface((GraphQLInterfaceType) getInterface(iface));
                }
                builder.fields(getExtensionFields(iface, fieldsDefined));
            }
        }

        builder.fields(getExtensionFields(object, fieldsDefined));

        return builder;
    }

    private List<GraphQLFieldDefinition> getExtensionFields(Class<?> object, List<String> fieldsDefined) {
        List<GraphQLFieldDefinition> fields = new ArrayList<>();
        if (extensionsTypeRegistry.containsKey(object)) {
            for (Class<?> aClass : extensionsTypeRegistry.get(object)) {
                for (Method method : getOrderedMethods(aClass)) {
                    if (method.isBridge() || method.isSynthetic()) {
                        continue;
                    }
                    if (breadthFirstSearch(method)) {
                        addExtensionField(getField(method), fields, fieldsDefined);
                    }
                }
                for (Field field : getAllFields(aClass).values()) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    if (parentalSearch(field)) {
                        addExtensionField(getField(field), fields, fieldsDefined);
                    }
                }
            }
        }
        return fields;
    }

    private void addExtensionField(GraphQLFieldDefinition gqlField, List<GraphQLFieldDefinition> fields, List<String> fieldsDefined) {
        if (!fieldsDefined.contains(gqlField.getName())) {
            fieldsDefined.add(gqlField.getName());
            fields.add(gqlField);
        } else {
            throw new GraphQLAnnotationsException("Duplicate field found in extension : " + gqlField.getName(), null);
        }
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
        builder.name(toGraphqlName(name == null ? field.getName() : name.value()));
        GraphQLType annotation = field.getAnnotation(GraphQLType.class);

        TypeFunction typeFunction = defaultTypeFunction;

        if (annotation != null) {
            typeFunction = newInstance(annotation.value());
        }

        GraphQLOutputType outputType = (GraphQLOutputType) typeFunction.buildType(field.getType(), field.getAnnotatedType());

        boolean isConnection = isConnection(field, outputType);
        if (isConnection) {
            outputType = getGraphQLConnection(field, outputType);
            builder.argument(relay.getConnectionFieldArguments());
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
                actualDataFetcher = new MethodDataFetcher(fluentMethod, typeFunction);
            }

            if (actualDataFetcher == null) {
                actualDataFetcher = new ExtensionDataFetcherWrapper(field.getDeclaringClass(), new FieldDataFetcher(field.getName()));
            }
        }


        if (isConnection) {
            actualDataFetcher = new ConnectionDataFetcher(field.getAnnotation(GraphQLConnection.class).connection(), actualDataFetcher);
        }

        builder.dataFetcher(actualDataFetcher);

        return new GraphQLFieldDefinitionWrapper(builder.build());
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

    private GraphQLOutputType getGraphQLConnection(AccessibleObject field, GraphQLOutputType type) {
        if (type instanceof GraphQLNonNull) {
            GraphQLList listType = (GraphQLList) ((GraphQLNonNull) type).getWrappedType();
            return new GraphQLNonNull(internalGetGraphQLConnection(field, listType));
        } else {
            return internalGetGraphQLConnection(field, (GraphQLList) type);
        }
    }

    private GraphQLOutputType internalGetGraphQLConnection(AccessibleObject field, GraphQLList listType) {
        GraphQLOutputType wrappedType = (GraphQLOutputType) listType.getWrappedType();
        String connectionName = field.getAnnotation(GraphQLConnection.class).name();
        connectionName = connectionName.isEmpty() ? wrappedType.getName() : connectionName;
        GraphQLObjectType edgeType = getActualType(relay.edgeType(connectionName, wrappedType, null, Collections.<GraphQLFieldDefinition>emptyList()));
        return getActualType(relay.connectionType(connectionName, edgeType, Collections.emptyList()));
    }

    private GraphQLObjectType getActualType(GraphQLObjectType type) {
        if (typeRegistry.containsKey(type.getName())) {
            type = (GraphQLObjectType) typeRegistry.get(type.getName());
        } else {
            typeRegistry.put(type.getName(), type);
        }
        return type;
    }

    private boolean isConnection(AccessibleObject obj, GraphQLOutputType type) {
        ConnectionTypeValidator validator = new ConnectionTypeValidator();
        if (type instanceof GraphQLNonNull) {
            type = (GraphQLOutputType) ((GraphQLNonNull) type).getWrappedType();
        }
        final GraphQLOutputType actualType = type;

        boolean isValidGraphQLTypeForConnection = obj.isAnnotationPresent(GraphQLConnection.class) &&
                actualType instanceof GraphQLList && TYPES_FOR_CONNECTION.stream().anyMatch(aClass ->
                aClass.isInstance(((GraphQLList) actualType).getWrappedType()));

        if(isValidGraphQLTypeForConnection) {
            validator.validate(obj);
            return true;
        } else {
            return false;
        }
    }

    protected GraphQLFieldDefinition getField(Method method) throws GraphQLAnnotationsException {
        GraphQLFieldDefinition.Builder builder = newFieldDefinition();

        String name = method.getName().replaceFirst("^(is|get|set)(.+)", "$2");
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        GraphQLName nameAnn = method.getAnnotation(GraphQLName.class);
        builder.name(toGraphqlName(nameAnn == null ? name : nameAnn.value()));

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

        GraphQLOutputType outputType = (GraphQLOutputType) outputTypeFunction.buildType(method.getReturnType(), annotatedReturnType);

        boolean isConnection = isConnection(method, outputType);
        if (isConnection) {
            outputType = getGraphQLConnection(method, outputType);
            builder.argument(relay.getConnectionFieldArguments());
        }

        builder.type(outputType);


        TypeFunction finalTypeFunction = typeFunction;
        List<GraphQLArgument> args = Arrays.asList(method.getParameters()).stream().
                filter(p -> !DataFetchingEnvironment.class.isAssignableFrom(p.getType())).
                map(parameter -> {
                    Class<?> t = parameter.getType();
                    graphql.schema.GraphQLInputType graphQLType = getInputObject(finalTypeFunction.buildType(t, parameter.getAnnotatedType()), DEFAULT_INPUT_PREFIX);
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
            relayFieldDefinition = relay.mutationWithClientMutationId(title, method.getName(),
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
            actualDataFetcher = new BatchedMethodDataFetcher(method, typeFunction);
        } else if (dataFetcher == null) {
            actualDataFetcher = new MethodDataFetcher(method, typeFunction);
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

        return new GraphQLFieldDefinitionWrapper(builder.build());
    }

    protected static GraphQLFieldDefinition field(Method method) throws InstantiationException, IllegalAccessException {
        return getInstance().getField(method);

    }

    public GraphQLInputObjectType getInputObject(Class<?> object) {
        String typeName = DEFAULT_INPUT_PREFIX + getTypeName(object);
        if (typeRegistry.containsKey(typeName)) {
            return (GraphQLInputObjectType) typeRegistry.get(typeName);
        } else {
            graphql.schema.GraphQLType graphQLType = getObject(object);
            GraphQLInputObjectType inputObject = (GraphQLInputObjectType) getInputObject(graphQLType, DEFAULT_INPUT_PREFIX);
            typeRegistry.put(inputObject.getName(), inputObject);
            return inputObject;
        }
    }

    @Override
    public GraphQLInputType getInputObject(graphql.schema.GraphQLType graphQLType, String newNamePrefix) {
        if (graphQLType instanceof GraphQLObjectType) {
            GraphQLObjectType object = (GraphQLObjectType) graphQLType;
            if (typeRegistry.containsKey(newNamePrefix + object.getName()) && typeRegistry.get(newNamePrefix + object.getName()) instanceof GraphQLInputType) {
                return (GraphQLInputType) typeRegistry.get(newNamePrefix + object.getName());
            }
            GraphQLInputObjectType inputObjectType = new GraphQLInputObjectType(newNamePrefix + object.getName(), object.getDescription(),
                    object.getFieldDefinitions().stream().
                            map(field -> {
                                GraphQLOutputType type = field.getType();
                                GraphQLInputType inputType = getInputObject(type, newNamePrefix);
                                return new GraphQLInputObjectField(field.getName(), field.getDescription(), inputType, null);
                            }).
                            collect(Collectors.toList()));
            typeRegistry.put(inputObjectType.getName(), inputObjectType);
            return inputObjectType;
        } else if (graphQLType instanceof GraphQLList) {
            return new GraphQLList(getInputObject(((GraphQLList) graphQLType).getWrappedType(), newNamePrefix));
        } else if (graphQLType instanceof GraphQLNonNull) {
            return new GraphQLNonNull(getInputObject(((GraphQLNonNull) graphQLType).getWrappedType(), newNamePrefix));
        } else if (graphQLType instanceof GraphQLTypeReference) {
            return new GraphQLTypeReference(newNamePrefix + ((GraphQLTypeReference) graphQLType).getName());
        } else if (graphQLType instanceof GraphQLInputType) {
            return (GraphQLInputType) graphQLType;
        }
        throw new IllegalArgumentException("Cannot convert type to input : " + graphQLType);
    }

    public static GraphQLInputObjectType inputObject(GraphQLObjectType graphQLType, String newNamePrefix) {
        return (GraphQLInputObjectType) getInstance().getInputObject(graphQLType, newNamePrefix);
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

    protected TypeFunction defaultTypeFunction;

    @Reference(target = "(type=default)")
    public void setDefaultTypeFunction(TypeFunction function) {
        defaultTypeFunction = function;
        ((DefaultTypeFunction) defaultTypeFunction).setAnnotationsProcessor(this);
    }

    public void registerTypeExtension(Class<?> objectClass) {
        GraphQLTypeExtension typeExtension = objectClass.getAnnotation(GraphQLTypeExtension.class);
        if (typeExtension == null) {
            throw new GraphQLAnnotationsException("Class is not annotated with GraphQLTypeExtension", null);
        } else {
            Class<?> aClass = typeExtension.value();
            if (!extensionsTypeRegistry.containsKey(aClass)) {
                extensionsTypeRegistry.put(aClass, new HashSet<>());
            }
            extensionsTypeRegistry.get(aClass).add(objectClass);
        }
    }

    public void unregisterTypeExtension(Class<?> objectClass) {
        GraphQLTypeExtension typeExtension = objectClass.getAnnotation(GraphQLTypeExtension.class);
        if (typeExtension == null) {
            throw new GraphQLAnnotationsException("Class is not annotated with GraphQLTypeExtension", null);
        } else {
            Class<?> aClass = typeExtension.value();
            if (extensionsTypeRegistry.containsKey(aClass)) {
                extensionsTypeRegistry.get(aClass).remove(objectClass);
            }
        }
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

    private class UnionTypeResolver implements TypeResolver {
        private final Map<Class<?>, graphql.schema.GraphQLType> types = new HashMap<>();

        public UnionTypeResolver(Class<?>[] classes) {
            Arrays.stream(classes).
                    forEach(c -> types.put(c, defaultTypeFunction.buildType(c, null)));
        }

        @Override
        public GraphQLObjectType getType(TypeResolutionEnvironment env) {
            Object object = env.getObject();
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
