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

import graphql.Scalars;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import lombok.SneakyThrows;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static graphql.schema.GraphQLEnumType.newEnum;

public class DefaultTypeFunction implements TypeFunction {

    public static TypeFunction instance = new DefaultTypeFunction();
    private static Map<String, BiFunction<Class<?>, AnnotatedType, GraphQLType>> registry;

    private static class StringFunction implements TypeFunction {

        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLString;
        }
    }

    private static class BooleanFunction implements TypeFunction {

        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLBoolean;
        }
    }

    private static class FloatFunction implements TypeFunction {

        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLFloat;
        }
    }

    private static class IntegerFunction implements TypeFunction {

        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLInt;
        }
    }

    private static class LongFunction implements TypeFunction {

        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLLong;
        }
    }

    private static class ListFunction implements TypeFunction {

        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            if (!(annotatedType instanceof AnnotatedParameterizedType)) {
                throw new IllegalArgumentException("List type parameter should be specified");
            }
            AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) annotatedType;
            AnnotatedType arg = parameterizedType.getAnnotatedActualTypeArguments()[0];
            Class<?> klass;
            if (arg.getType() instanceof ParameterizedType) {
                klass = (Class<?>)((ParameterizedType)(arg.getType())).getRawType();
            } else {
                klass = (Class<?>) arg.getType();
            }
            return new GraphQLList(DefaultTypeFunction.instance.apply(klass, arg));
        }
    }

    private static class StreamFunction implements TypeFunction {

        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            if (!(annotatedType instanceof AnnotatedParameterizedType)) {
                throw new IllegalArgumentException("Stream type parameter should be specified");
            }
            AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) annotatedType;
            AnnotatedType arg = parameterizedType.getAnnotatedActualTypeArguments()[0];
            Class<?> klass;
            if (arg.getType() instanceof ParameterizedType) {
                klass = (Class<?>)((ParameterizedType)(arg.getType())).getRawType();
            } else {
                klass = (Class<?>) arg.getType();
            }
            return new GraphQLList(DefaultTypeFunction.instance.apply(klass, arg));
        }
    }

    private static class OptionalFunction implements TypeFunction {

        @Override
        @SneakyThrows
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            if (!(annotatedType instanceof AnnotatedParameterizedType)) {
                throw new IllegalArgumentException("Optional type parameter should be specified");
            }
            AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) annotatedType;
            AnnotatedType arg = parameterizedType.getAnnotatedActualTypeArguments()[0];
            Class<?> klass;
            if (arg.getType() instanceof ParameterizedType) {
                klass = (Class<?>)((ParameterizedType)(arg.getType())).getRawType();
            } else {
                klass = (Class<?>) arg.getType();
            }
            return DefaultTypeFunction.instance.apply(klass, arg);
        }
    }

    private static class EnumFunction implements TypeFunction {

        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            Class<? extends Enum> enumClass = (Class<? extends Enum>) aClass;
            GraphQLEnumType.Builder builder = newEnum();

            GraphQLName name = aClass.getAnnotation(GraphQLName.class);
            builder.name(name == null ? aClass.getSimpleName() : name.value());

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
                    builder.value(name_, constant.ordinal(), fieldDescription == null ? name_ : fieldDescription.value());
                } catch (NoSuchFieldException e) {
                }
            });

            return builder.build();
        }
    }

    private static class ObjectFunction implements TypeFunction {

        private final Map<String, GraphQLTypeReference> processing = new ConcurrentHashMap<>();
        private final Map<String, GraphQLType> types = new ConcurrentHashMap<>();

        @Override
        @SneakyThrows
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            GraphQLName name = aClass.getAnnotation(GraphQLName.class);
            String typeName = name == null ? aClass.getSimpleName() : name.value();
            if (processing.containsKey(typeName)) {
                return processing.get(typeName);
            } else {
                processing.put(typeName, new GraphQLTypeReference(typeName));
                GraphQLType type;
                if (aClass.isInterface()) {
                    type = GraphQLAnnotations.iface(aClass);
                } else {
                    type = GraphQLAnnotations.object(aClass);
                }
                processing.remove(typeName);
                types.put(typeName, type);
                return type;
            }
        }
    }

    static {
        registry = new ConcurrentHashMap<>();

        register(String.class, new StringFunction());

        register(Boolean.class, new BooleanFunction());
        register(boolean.class, new BooleanFunction());

        register(Float.class, new FloatFunction());
        register(float.class, new FloatFunction());

        register(Integer.class, new IntegerFunction());
        register(int.class, new IntegerFunction());

        register(Long.class, new LongFunction());
        register(long.class, new LongFunction());

        register(AbstractList.class, new ListFunction());
        register(List.class, new ListFunction());
        register(Stream.class, new StreamFunction());

        register(Enum.class, new EnumFunction());

        register(Optional.class, new OptionalFunction());

        register(Object.class, new ObjectFunction());
    }

    public static Class<DefaultTypeFunction> register(Class<?> klass, TypeFunction function) {
        registry.put(klass.getName(), function);
        return DefaultTypeFunction.class;
    }

    @Override
    public GraphQLType apply(Class<?> klass, AnnotatedType annotatedType) {
        Class<?> t = klass;

        while (!registry.containsKey(t.getName())) {
            if (t.getSuperclass() == null && t.isInterface()) {
                t = Object.class;
                continue;
            }
            t = t.getSuperclass();
            if (t == null) {
                throw new IllegalArgumentException("unsupported type");
            }
        }

        GraphQLType result = registry.get(t.getName()).apply(klass, annotatedType);

        if (klass.getAnnotation(GraphQLNonNull.class) != null ||
            (annotatedType != null && annotatedType.getAnnotation(GraphQLNonNull.class) != null)) {
            result = new graphql.schema.GraphQLNonNull(result);
        }

        return result;
    }
}
