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
package graphql.annotations.dataFetchers;

import graphql.annotations.annotationTypes.GraphQLInvokeDetached;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.schema.*;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static graphql.annotations.processor.util.NamingKit.toGraphqlName;
import static graphql.annotations.processor.util.ReflectionKit.constructNewInstance;
import static graphql.annotations.processor.util.ReflectionKit.newInstance;

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
                obj = null;
            } else if (method.getAnnotation(GraphQLInvokeDetached.class) != null) {
                obj = newInstance((Class<T>) method.getDeclaringClass());
            } else if (!method.getDeclaringClass().isInstance(environment.getSource())) {
                obj = newInstance((Class<T>) method.getDeclaringClass(), environment.getSource());
            } else {
                obj = environment.getSource();
                if (obj == null) {
                    return null;
                }
            }
            return (T) method.invoke(obj, invocationArgs(environment, container));
        } catch (IllegalAccessException | InvocationTargetException e) {
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

    private Object buildArg(Type p, GraphQLType graphQLType, Object arg) {
        if (arg == null) {
            return null;
        }
        if (graphQLType instanceof graphql.schema.GraphQLNonNull) {
            graphQLType = ((graphql.schema.GraphQLNonNull) graphQLType).getWrappedType();
        }
        if (p instanceof Class<?> && graphQLType instanceof GraphQLInputObjectType) {
            Constructor<?> constructors[] = ((Class) p).getConstructors();
            for (Constructor<?> constructor : constructors) {
                Parameter[] parameters = constructor.getParameters();
                if (parameters.length == 1 && parameters[0].getType().isAssignableFrom(arg.getClass())) {
                    return constructNewInstance(constructor, arg);
                } else {
                    List<Object> objects = new ArrayList<>();
                    Map map = (Map) arg;
                    for (Parameter parameter : parameters) {
                        String name = toGraphqlName(parameter.getAnnotation(GraphQLName.class) != null ? parameter.getAnnotation(GraphQLName.class).value() : parameter.getName());
                        objects.add(buildArg(parameter.getParameterizedType(), ((GraphQLInputObjectType) graphQLType).getField(name).getType(), map.get(name)));
                    }
                    return constructNewInstance(constructor, objects.toArray(new Object[objects.size()]));
                }
            }
            return null;
        } else if (p instanceof ParameterizedType && graphQLType instanceof GraphQLList) {
            List<Object> list = new ArrayList<>();
            Type subType = ((ParameterizedType) p).getActualTypeArguments()[0];
            GraphQLType wrappedType = ((GraphQLList) graphQLType).getWrappedType();

            for (Object item : ((List) arg)) {
                list.add(buildArg(subType, wrappedType, item));
            }

            return list;
        } else {
            return arg;
        }
    }
}
