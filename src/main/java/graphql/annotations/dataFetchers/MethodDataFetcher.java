/**
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
package graphql.annotations.dataFetchers;

import static graphql.annotations.processor.util.NamingKit.toGraphqlName;
import static graphql.annotations.processor.util.PrefixesUtil.addPrefixToPropertyName;
import static graphql.annotations.processor.util.PrefixesUtil.extractPrefixedName;
import static graphql.annotations.processor.util.ReflectionKit.constructNewInstance;
import static graphql.annotations.processor.util.ReflectionKit.newInstance;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import graphql.annotations.annotationTypes.GraphQLConstructor;
import graphql.annotations.annotationTypes.GraphQLInvokeDetached;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.annotations.type.GraphQLUndefined;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLType;


/**
 * This class is determining how to return value of a method from an api entity
 * The order of the mapping:
 * 1. If no source is provided to map between - invoking the method implementation
 * 2. If annotated with @GraphQLInvokeDetached - invoking the method implementation
 * 3. else If source is provided, and method name is matching a method name in the source object - execute source implementation
 * i.e method name is: `name` ; existing method in the source object with name: `name`
 * 4. else If source is provided, and method name is matching a method name with a `get` prefix in the source object - execute source implementation
 * i.e method name is: `name` ; existing method in the source object with name: `getName`
 * 5. else If source is provided, and method name is matching a method name with a `is` prefix in the source object - execute source implementation
 * i.e method name is: `name` ; existing method in the source object with name: isName
 * 6. else If source is provided, and method name is matching a field name in the source object - return field value from the source object
 * i.e method name is: `name` ; field name in source object is: `name`
 * 7. else If source is provided, and method name is prefixed with `get` or `is` - and it matches to a field name (without the prefix) in the source object - return field value from the source object
 * i.e method name is: `getName` ; field name in source object is: `name`
 *
 * @param <T> type of the returned value
 */
public class MethodDataFetcher<T> implements DataFetcher<T> {
    private final Method method;
    private final ProcessingElementsContainer container;
    private final TypeFunction typeFunction;


    public MethodDataFetcher(Method method, TypeFunction typeFunction, ProcessingElementsContainer container) {
        this.method = method;
        this.typeFunction = typeFunction;
        this.container = container;
    }

    @Override
    public T get(DataFetchingEnvironment environment) {
        try {
            T obj;
            if (Modifier.isStatic(method.getModifiers())) {
                return (T) method.invoke(null, invocationArgs(environment, container));
            } else if (method.isAnnotationPresent(GraphQLInvokeDetached.class)) {
                obj = newInstance((Class<T>) method.getDeclaringClass());
            } else if (!method.getDeclaringClass().isInstance(environment.getSource())) {
                obj = newInstance((Class<T>) method.getDeclaringClass(), environment.getSource());
            } else {
                obj = environment.getSource();
                if (obj == null) {
                    return null;
                }
            }

            if (obj == null && environment.getSource() != null) {
                Object value = getGraphQLFieldValue(environment.getSource(), method.getName());
                return (T) value;
            }

            return (T) method.invoke(obj, invocationArgs(environment, container));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private Object[] invocationArgs(DataFetchingEnvironment environment, ProcessingElementsContainer container) {
        List<Object> result = new ArrayList<>();
        Map<String, Object> envArgs = environment.getArguments();
        for (Parameter p : method.getParameters()) {
            String parameterName;
            GraphQLName name = p.getAnnotation(GraphQLName.class);
            if (name != null) {
                parameterName = toGraphqlName(name.value());
            } else {
                parameterName = toGraphqlName(p.getName());
            }

            Class<?> paramType = p.getType();
            if (DataFetchingEnvironment.class.isAssignableFrom(paramType)) {
                result.add(environment);
                continue;
            }

            graphql.schema.GraphQLType graphQLType = typeFunction.buildType(true, paramType, p.getAnnotatedType(), container);
            if (envArgs.containsKey(parameterName)) {
                result.add(buildArg(p.getParameterizedType(), graphQLType, envArgs.get(parameterName)));
            } else {
                result.add(null);
            }
        }
        return result.toArray();
    }

    @SuppressWarnings("ConstantConditions")
    private Object buildArg(Type p, GraphQLType graphQLType, Object arg) {
        Optional<Object> optionalArg = Optional.ofNullable(arg);
        if (!optionalArg.isPresent()) {
            return null;
        }
        if (graphQLType instanceof graphql.schema.GraphQLNonNull) {
            graphQLType = ((graphql.schema.GraphQLNonNull) graphQLType).getWrappedType();
        }

        if (p instanceof Class<?> && graphQLType instanceof GraphQLInputObjectType) {
            Constructor<?>[] constructors = ((Class) p).getConstructors();
            Constructor<?> constructor = getBuildArgConstructor(constructors);
            Parameter[] parameters = constructor.getParameters();

            if (parameters.length == 1 && optionalArg.isPresent() && parameters[0].getType().isAssignableFrom(optionalArg.get().getClass())) {
                if (parameters[0].getType().isAssignableFrom(Optional.class)) {
                    return constructNewInstance(constructor, arg);
                } else {
                    return constructNewInstance(constructor, optionalArg.orElse(null));
                }
            } else {
                List<Object> objects = new ArrayList<>();
                Map map = (Map) optionalArg.orElseGet(Collections::emptyMap);
                for (Parameter parameter : parameters) {
                    String name = toGraphqlName(parameter.getAnnotation(GraphQLName.class) != null ? parameter.getAnnotation(GraphQLName.class).value() : parameter.getName());
                    if (!map.containsKey(name)) {
                        objects.add(null);
                    } else {
                        objects.add(buildArg(parameter.getParameterizedType(), ((GraphQLInputObjectType) graphQLType).getField(name).getType(), map.get(name)));
                    }
                }
                return constructNewInstance(constructor, objects.toArray(new Object[objects.size()]));
            }
        } else if (p instanceof ParameterizedType && graphQLType instanceof GraphQLList) {
            if (((ParameterizedType) p).getRawType() == Optional.class) {
                if (!optionalArg.isPresent()) {
                    return null;
                } else {
                    Type subType = ((ParameterizedType) p).getActualTypeArguments()[0];
                    return Optional.ofNullable(buildArg(subType, graphQLType, arg));
                }
            } else {
                List<Object> list = new ArrayList<>();
                Type subType = ((ParameterizedType) p).getActualTypeArguments()[0];
                GraphQLType wrappedType = ((GraphQLList) graphQLType).getWrappedType();

                for (Object item : ((List) optionalArg.orElseGet(Collections::emptyList))) {
                    list.add(buildArg(subType, wrappedType, item));
                }
                return list;
            }
        } else if (p instanceof ParameterizedType && ((ParameterizedType) p).getRawType() == Optional.class) {
            Type subType = ((ParameterizedType) p).getActualTypeArguments()[0];
            if (arg == null) {
                return null;
            } else {
                return Optional.ofNullable(buildArg(subType, new GraphQLUndefined(), arg));
            }
        } else {
            return optionalArg.orElse(null);
        }
    }


    /***
     * return the constructor to call in order to build the object
     * @param constructors Object constructors
     * @return the annotated constructor if present else return the first constructor
     */
    private Constructor getBuildArgConstructor(Constructor<?> constructors[]) {
        if (constructors != null) {
            for (Constructor<?> constructor : constructors) {
                if (constructor.isAnnotationPresent(GraphQLConstructor.class)) {
                    return constructor;
                }
            }
            return constructors[0];
        }
        return null;
    }

    private Object getGraphQLFieldValue(Object source, String fieldName) throws IllegalAccessException, NoSuchFieldException, InvocationTargetException {
        Object methodValue = getValueFromMethod(source, fieldName);
        if (methodValue != null) return methodValue;

        Object fieldValue = getValueFromField(source, fieldName);
        if (fieldValue != null) return fieldValue;

        throw new NoSuchFieldException("No GraphQL field found");
    }

    private Object getValueFromField(Object source, String fieldName) throws IllegalAccessException {
        List<String> namesToSearchFor = Arrays.asList(fieldName, extractPrefixedName(fieldName));
        for (String name : namesToSearchFor) {
            Field field = getField(source.getClass(), name);
            if (isFieldContainsValue(field)) {
                return field.get(source);
            }
        }
        return null;
    }

    private boolean isFieldContainsValue(Field field) throws IllegalAccessException {
        if (field != null) {
            field.setAccessible(true);
            return true;
        }
        return false;
    }

    private Field getField(Class<?> clazz, String name) {
        Field field = null;
        while (clazz != null && field == null) {
            try {
                field = clazz.getDeclaredField(name);
            } catch (Exception ignored) {
            }
            clazz = clazz.getSuperclass();
        }
        return field;
    }

    private Object getValueFromMethod(Object source, String fieldName) throws IllegalAccessException, InvocationTargetException {
        String[] orderedPrefixes = new String[]{"", "get", "is"};
        for (String orderedPrefix : orderedPrefixes) {
            Method method = getMethod(source.getClass(), fieldName, orderedPrefix);
            if (method != null) {
                return method.invoke(source);
            }
        }
        return null;
    }

    private Method getMethod(Class<?> clazz, String name, String prefix) {
        String prefixedName;
        if (prefix.isEmpty()) {
            prefixedName = name;
        } else {
            prefixedName = addPrefixToPropertyName(prefix, name);
        }

        Method method = null;
        while (clazz != null && method == null) {
            try {
                method = clazz.getDeclaredMethod(prefixedName);
            } catch (Exception ignored) {
            }
            clazz = clazz.getSuperclass();
        }

        return method;
    }

}