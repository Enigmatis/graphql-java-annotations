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

import graphql.schema.*;
import graphql.schema.GraphQLType;

import java.lang.reflect.*;
import java.util.*;

import static graphql.annotations.ReflectionKit.constructNewInstance;
import static graphql.annotations.ReflectionKit.constructor;
import static graphql.annotations.ReflectionKit.newInstance;
import static graphql.annotations.util.NamingKit.toGraphqlName;

class MethodDataFetcher implements DataFetcher {
    private final Method method;
    private final TypeFunction typeFunction;

    public MethodDataFetcher(Method method) {
        this(method, new DefaultTypeFunction());
    }

    public MethodDataFetcher(Method method, TypeFunction typeFunction) {
        this.method = method;
        this.typeFunction = typeFunction;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        try {
            Object obj;

            if (Modifier.isStatic(method.getModifiers())) {
                obj = null;
            } else if (method.getAnnotation(GraphQLInvokeDetached.class) != null) {
                obj = newInstance(method.getDeclaringClass());
            } else if (!method.getDeclaringClass().isInstance(environment.getSource())) {
                obj = newInstance(method.getDeclaringClass(), environment.getSource());
            } else {
                obj = environment.getSource();
                if (obj == null) {
                    return null;
                }
            }
            return method.invoke(obj, invocationArgs(environment));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Object[] invocationArgs(DataFetchingEnvironment environment) {
        List<Object> result = new ArrayList<>();
        Iterator envArgs = environment.getArguments().values().iterator();
        for (Parameter p : method.getParameters()) {
            Class<?> paramType = p.getType();
            if (DataFetchingEnvironment.class.isAssignableFrom(paramType)) {
                result.add(environment);
                continue;
            }
            graphql.schema.GraphQLType graphQLType = typeFunction.buildType(paramType, p.getAnnotatedType());
            Object arg = envArgs.next();
            result.add(buildArg(p.getParameterizedType(), graphQLType, arg));
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
                        objects.add(buildArg(parameter.getType(), ((GraphQLInputObjectType)graphQLType).getField(name).getType(),map.get(name)));
                    }
                    return constructNewInstance(constructor, objects.toArray(new Object[objects.size()]));
                }
            }
            return null;
        } else if (p instanceof ParameterizedType && graphQLType instanceof GraphQLList) {
            List<Object> list = new ArrayList<>();
            Type subType = ((ParameterizedType)p).getActualTypeArguments()[0];
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
