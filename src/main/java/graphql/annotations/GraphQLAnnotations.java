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

import graphql.schema.*;

import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInterfaceType.newInterface;
import static graphql.schema.GraphQLObjectType.newObject;

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
    public static GraphQLInterfaceType iface(Class<?> iface) throws IllegalAccessException, InstantiationException {
        GraphQLInterfaceType.Builder builder = ifaceBuilder(iface);
        return builder.build();
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

        return builder.build();
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

            boolean valid = !Modifier.isStatic(method.getModifiers()) &&
                    (method.getAnnotation(GraphQLField.class) != null ||
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
                builder.withInterface(iface(iface));
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
        builder.type(field.getAnnotation(NotNull.class) == null ? type : new graphql.schema.GraphQLNonNull(type));

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
        builder.dataFetcher(dataFetcher == null ? new FieldDataFetcher(field.getName()) : dataFetcher.value().newInstance());

        return builder.build();
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
        builder.type(method.getAnnotation(NotNull.class) == null ? type : new graphql.schema.GraphQLNonNull(type));

        for (Parameter parameter : method.getParameters()) {
            Class<?> t = parameter.getType();
            if (!DataFetchingEnvironment.class.isAssignableFrom(t)) {
                graphql.schema.GraphQLType graphQLType = typeFunction.apply(t, annotatedReturnType);
                if (graphQLType instanceof GraphQLObjectType) {
                    GraphQLInputObjectType inputObject = inputObject((GraphQLObjectType) graphQLType);
                    graphQLType = inputObject;
                }
                builder.argument(argument(parameter, graphQLType));
            }
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
        builder.dataFetcher(dataFetcher == null? new MethodDataFetcher(method) : dataFetcher.value().newInstance());

        return builder.build();
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
        builder.name(parameter.getName());
        builder.type(parameter.getAnnotation(NotNull.class) == null ? (GraphQLInputType) t : new graphql.schema.GraphQLNonNull(t));
        GraphQLDescription description = parameter.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            builder.description(description.value());
        }
        GraphQLDefaultValue defaultValue = parameter.getAnnotation(GraphQLDefaultValue.class);
        if (defaultValue != null) {
            builder.defaultValue(defaultValue.value().newInstance().get());
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

    private static class MethodDataFetcher implements DataFetcher {
        private final Method method;
        private final int envIndex;

        public MethodDataFetcher(Method method) {
            this.method = method;
            List<Class<?>> parameterTypes = Arrays.asList(method.getParameters()).stream().
                    map(Parameter::getType).
                    collect(Collectors.toList());
            envIndex = parameterTypes.indexOf(DataFetchingEnvironment.class);
        }

        @Override
        public Object get(DataFetchingEnvironment environment) {
            if (environment.getSource() == null) return null;
            try {
                ArrayList args = new ArrayList<>(environment.getArguments().values());
                if (envIndex >= 0) {
                    args.add(envIndex, environment);
                }
                return method.invoke(environment.getSource(), args.toArray());
            } catch (IllegalAccessException | InvocationTargetException e) {
                return null;
            }
        }
    }
}
