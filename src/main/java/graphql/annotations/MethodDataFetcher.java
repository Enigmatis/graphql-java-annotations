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

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static graphql.annotations.ReflectionKit.constructNewInstance;
import static graphql.annotations.ReflectionKit.constructor;
import static graphql.annotations.ReflectionKit.newInstance;

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
            if (graphQLType instanceof GraphQLInputObjectType) {
                Constructor<?> constructor = constructor(paramType, HashMap.class);
                result.add(constructNewInstance(constructor, envArgs.next()));

            } else {
                result.add(envArgs.next());
            }
        }
        return result.toArray();
    }
}
