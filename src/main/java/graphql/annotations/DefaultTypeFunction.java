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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.annotations.ServiceScope;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static graphql.schema.GraphQLEnumType.newEnum;

@Component(scope = ServiceScope.SINGLETON, property = "type=default")
public class DefaultTypeFunction implements TypeFunction {

    @Reference(target = "(!(type=default))",
            policyOption = ReferencePolicyOption.GREEDY)
    protected List<TypeFunction> otherFunctions = new ArrayList<>();

    @Override
    public Collection<Class<?>> getAcceptedTypes() {
        List<Class<?>> list = registry.keySet().stream().collect(Collectors.toList());
        List<Class<?>> others = otherFunctions.stream().flatMap(tf -> tf.getAcceptedTypes().stream())
                .collect(Collectors.toList());
        list.addAll(others);
        return list;
    }

    private Map<Class<?>, BiFunction<Class<?>, AnnotatedType, GraphQLType>> registry;

    GraphQLAnnotationsProcessor annotationsProcessor;

    void setAnnotationsProcessor(GraphQLAnnotationsProcessor annotationsProcessor) {
        this.annotationsProcessor = annotationsProcessor;
    }

    private class StringFunction implements TypeFunction {

        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLString;
        }

        @Override
        public Collection<Class<?>> getAcceptedTypes() {
            return Collections.singletonList(String.class);
        }
    }

    private class BooleanFunction implements TypeFunction {

        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLBoolean;
        }

        @Override
        public Collection<Class<?>> getAcceptedTypes() {
            return Arrays.asList(Boolean.class, boolean.class);
        }
    }

    private class FloatFunction implements TypeFunction {

        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLFloat;
        }

        @Override
        public Collection<Class<?>> getAcceptedTypes() {
            return Arrays.asList(Float.class, float.class, Double.class, double.class);
        }
    }

    private class IntegerFunction implements TypeFunction {

        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLInt;
        }

        @Override
        public Collection<Class<?>> getAcceptedTypes() {
            return Arrays.asList(Integer.class, int.class);
        }
    }

    private class LongFunction implements TypeFunction {

        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLLong;
        }

        @Override
        public Collection<Class<?>> getAcceptedTypes() {
            return Arrays.asList(Long.class, long.class);
        }
    }

    /**
     * Support for the Iterable things like Lists / Sets / Collections and so on..
     */
    private class IterableFunction implements TypeFunction {

        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            if (!(annotatedType instanceof AnnotatedParameterizedType)) {
                throw new IllegalArgumentException("List type parameter should be specified");
            }
            AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) annotatedType;
            AnnotatedType arg = parameterizedType.getAnnotatedActualTypeArguments()[0];
            Class<?> klass;
            if (arg.getType() instanceof ParameterizedType) {
                klass = (Class<?>) ((ParameterizedType) (arg.getType())).getRawType();
            } else {
                klass = (Class<?>) arg.getType();
            }
            return new GraphQLList(DefaultTypeFunction.this.apply(klass, arg));
        }

        @Override
        public Collection<Class<?>> getAcceptedTypes() {
            return Arrays.asList(
                    List.class,
                    AbstractList.class,
                    Set.class,
                    AbstractSet.class,
                    Collection.class,
                    AbstractCollection.class,
                    Iterable.class
            );
        }
    }

    private class StreamFunction implements TypeFunction {

        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            if (!(annotatedType instanceof AnnotatedParameterizedType)) {
                throw new IllegalArgumentException("Stream type parameter should be specified");
            }
            AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) annotatedType;
            AnnotatedType arg = parameterizedType.getAnnotatedActualTypeArguments()[0];
            Class<?> klass;
            if (arg.getType() instanceof ParameterizedType) {
                klass = (Class<?>) ((ParameterizedType) (arg.getType())).getRawType();
            } else {
                klass = (Class<?>) arg.getType();
            }
            return new GraphQLList(DefaultTypeFunction.this.apply(klass, arg));
        }

        @Override
        public Collection<Class<?>> getAcceptedTypes() {
            return Collections.singletonList(Stream.class);
        }
    }

    private class OptionalFunction implements TypeFunction {

        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            if (!(annotatedType instanceof AnnotatedParameterizedType)) {
                throw new IllegalArgumentException("Optional type parameter should be specified");
            }
            AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) annotatedType;
            AnnotatedType arg = parameterizedType.getAnnotatedActualTypeArguments()[0];
            Class<?> klass;
            if (arg.getType() instanceof ParameterizedType) {
                klass = (Class<?>) ((ParameterizedType) (arg.getType())).getRawType();
            } else {
                klass = (Class<?>) arg.getType();
            }
            return DefaultTypeFunction.this.apply(klass, arg);
        }

        @Override
        public Collection<Class<?>> getAcceptedTypes() {
            return Collections.singletonList(Optional.class);
        }
    }

    private class EnumFunction implements TypeFunction {
        private final Map<String, GraphQLTypeReference> processing = new ConcurrentHashMap<>();
        private final Map<String, GraphQLType> types = new ConcurrentHashMap<>();

        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            GraphQLName name = aClass.getAnnotation(GraphQLName.class);
            String typeName = name == null ? aClass.getSimpleName() : name.value();

            if (types.containsKey(typeName)) {
                return types.get(typeName);
            } else if (processing.containsKey(typeName)) {
                return processing.getOrDefault(typeName, new GraphQLTypeReference(typeName));
            } else {

                processing.put(typeName, new GraphQLTypeReference(typeName));

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

                final GraphQLEnumType type = builder.build();
                types.put(typeName, type);
                //noinspection SuspiciousMethodCalls
                processing.remove(type);
                return type;
            }
        }

        @Override
        public Collection<Class<?>> getAcceptedTypes() {
            return Collections.singletonList(Enum.class);
        }
    }

    private class ObjectFunction implements TypeFunction {

        private final Map<String, GraphQLTypeReference> processing = new ConcurrentHashMap<>();
        private final Map<String, GraphQLType> types = new ConcurrentHashMap<>();

        @Override
        public GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
            GraphQLName name = aClass.getAnnotation(GraphQLName.class);
            String typeName = name == null ? aClass.getName() : name.value();
            if (types.containsKey(typeName)) {
                return types.get(typeName);
            } else if (processing.containsKey(typeName)) {
                return processing.getOrDefault(typeName, new GraphQLTypeReference(typeName));
            } else {
                processing.put(typeName, new GraphQLTypeReference(typeName));
                GraphQLType type;
                if (aClass.isInterface()) {
                    type = annotationsProcessor.getInterface(aClass);
                } else {
                    type = annotationsProcessor.getObject(aClass);
                }
                types.put(typeName, type);
                processing.remove(typeName);
                return type;
            }
        }

        @Override
        public Collection<Class<?>> getAcceptedTypes() {
            return Collections.singletonList(Object.class);
        }
    }

    public DefaultTypeFunction() {
        registry = new ConcurrentHashMap<>();

        register(new StringFunction());
        register(new BooleanFunction());
        register(new FloatFunction());
        register(new IntegerFunction());

        register(new LongFunction());

        register(new IterableFunction());
        register(new StreamFunction());

        register(new EnumFunction());

        register(new OptionalFunction());

        register(new ObjectFunction());
    }

    @Activate
    protected void activate() {
        otherFunctions.forEach(this::register);
    }

    public Class<DefaultTypeFunction> register(TypeFunction function) {
        function.getAcceptedTypes().forEach(t -> registry.put(t, function));
        return DefaultTypeFunction.class;
    }

    @Override
    public GraphQLType apply(Class<?> klass, AnnotatedType annotatedType) {
        Class<?> t = klass;

        while (!registry.containsKey(t)) {

            if (t.getSuperclass() == null && t.isInterface()) {
                t = Object.class;
                continue;
            }
            t = t.getSuperclass();
            if (t == null) {
                throw new IllegalArgumentException("unsupported type");
            }
        }

        GraphQLType result = registry.get(t).apply(klass, annotatedType);

        if (klass.getAnnotation(GraphQLNonNull.class) != null ||
                (annotatedType != null && annotatedType.getAnnotation(GraphQLNonNull.class) != null)) {
            result = new graphql.schema.GraphQLNonNull(result);
        }

        return result;
    }
}
