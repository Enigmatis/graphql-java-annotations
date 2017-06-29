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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import static graphql.annotations.util.NamingKit.toGraphqlName;
import static graphql.schema.GraphQLEnumType.newEnum;

@Component(scope = ServiceScope.SINGLETON, property = "type=default")
public class DefaultTypeFunction implements TypeFunction {

    @Reference(target = "(!(type=default))",
            policyOption = ReferencePolicyOption.GREEDY)
    protected List<TypeFunction> otherFunctions = new ArrayList<>();

    private CopyOnWriteArrayList<TypeFunction> typeFunctions;

    GraphQLAnnotationsProcessor annotationsProcessor;

    @Override
    public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
        return getTypeFunction(aClass, annotatedType) != null;
    }

    void setAnnotationsProcessor(GraphQLAnnotationsProcessor annotationsProcessor) {
        this.annotationsProcessor = annotationsProcessor;
    }

    private class StringFunction implements TypeFunction {

        @Override
        public String getTypeName(Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLString.getName();
        }

        @Override
        public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
            return aClass == String.class;
        }

        @Override
        public GraphQLType buildType(String typeName, Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLString;
        }
    }

    private class BooleanFunction implements TypeFunction {

        @Override
        public String getTypeName(Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLBoolean.getName();
        }

        @Override
        public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
            return aClass == Boolean.class || aClass == boolean.class;
        }

        @Override
        public GraphQLType buildType(String typeName, Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLBoolean;
        }
    }

    private class FloatFunction implements TypeFunction {

        @Override
        public String getTypeName(Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLFloat.getName();
        }

        @Override
        public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
            return aClass == Float.class || aClass == float.class || aClass == Double.class || aClass == double.class;
        }

        @Override
        public GraphQLType buildType(String typeName, Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLFloat;
        }
    }

    private class IntegerFunction implements TypeFunction {

        @Override
        public String getTypeName(Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLInt.getName();
        }

        @Override
        public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
            return aClass == Integer.class || aClass == int.class;
        }

        @Override
        public GraphQLType buildType(String typeName, Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLInt;
        }
    }

    private class LongFunction implements TypeFunction {

        @Override
        public String getTypeName(Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLLong.getName();
        }

        @Override
        public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
            return aClass == Long.class || aClass == long.class;
        }

        @Override
        public GraphQLType buildType(String typeName, Class<?> aClass, AnnotatedType annotatedType) {
            return Scalars.GraphQLLong;
        }
    }

    /**
     * Support for the Iterable things like Lists / Sets / Collections and so on..
     */
    private class IterableFunction implements TypeFunction {

        @Override
        public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
            return Iterable.class.isAssignableFrom(aClass);
        }

        @Override
        public GraphQLType buildType(String typeName, Class<?> aClass, AnnotatedType annotatedType) {
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
            return new GraphQLList(DefaultTypeFunction.this.buildType(klass, arg));
        }
    }

    private class StreamFunction implements TypeFunction {

        @Override
        public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
            return Stream.class.isAssignableFrom(aClass);
        }

        @Override
        public GraphQLType buildType(String typeName, Class<?> aClass, AnnotatedType annotatedType) {
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
            return new GraphQLList(DefaultTypeFunction.this.buildType(klass, arg));
        }
    }

    private class OptionalFunction implements TypeFunction {

        @Override
        public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
            return aClass == Optional.class;
        }

        @Override
        public String getTypeName(Class<?> aClass, AnnotatedType annotatedType) {
            AnnotatedType arg = getAnnotatedType(annotatedType);
            return DefaultTypeFunction.this.getTypeName(getClass(annotatedType), arg);
        }

        @Override
        public GraphQLType buildType(String typeName, Class<?> aClass, AnnotatedType annotatedType) {
            AnnotatedType arg = getAnnotatedType(annotatedType);
            return DefaultTypeFunction.this.buildType(typeName, getClass(annotatedType), arg);
        }

        private AnnotatedType getAnnotatedType(AnnotatedType annotatedType) {
            if (!(annotatedType instanceof AnnotatedParameterizedType)) {
                throw new IllegalArgumentException("Optional type parameter should be specified");
            }
            AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) annotatedType;
            return parameterizedType.getAnnotatedActualTypeArguments()[0];
        }

        private Class<?> getClass(AnnotatedType annotatedType) {
            AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) annotatedType;
            AnnotatedType arg = parameterizedType.getAnnotatedActualTypeArguments()[0];
            if (arg.getType() instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) (arg.getType())).getRawType();
            } else {
                return (Class<?>) arg.getType();
            }
        }
    }

    private class EnumFunction implements TypeFunction {
        private final Map<String, GraphQLTypeReference> processing = new ConcurrentHashMap<>();
        private final Map<String, GraphQLType> types = new ConcurrentHashMap<>();

        @Override
        public String getTypeName(Class<?> aClass, AnnotatedType annotatedType) {
            GraphQLName name = aClass.getAnnotation(GraphQLName.class);
            return toGraphqlName(name == null ? aClass.getSimpleName() : name.value());
        }

        @Override
        public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
            return Enum.class.isAssignableFrom(aClass);
        }

        @Override
        public GraphQLType buildType(String typeName, Class<?> aClass, AnnotatedType annotatedType) {
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
    }

    private class ObjectFunction implements TypeFunction {

        private final Map<String, GraphQLTypeReference> processing = new ConcurrentHashMap<>();
        private final Map<String, GraphQLType> types = new ConcurrentHashMap<>();

        @Override
        public String getTypeName(Class<?> aClass, AnnotatedType annotatedType) {
            GraphQLName name = aClass.getAnnotation(GraphQLName.class);
            return toGraphqlName(name == null ? aClass.getSimpleName() : name.value());
        }

        @Override
        public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
            return true;
        }

        @Override
        public GraphQLType buildType(String typeName, Class<?> aClass, AnnotatedType annotatedType) {
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
                    type = annotationsProcessor.getObjectOrRef(aClass);
                }
                types.put(typeName, type);
                processing.remove(typeName);
                return type;
            }
        }
    }

    public DefaultTypeFunction() {
        typeFunctions = new CopyOnWriteArrayList<>();

        typeFunctions.add(new StringFunction());
        typeFunctions.add(new BooleanFunction());
        typeFunctions.add(new FloatFunction());
        typeFunctions.add(new IntegerFunction());

        typeFunctions.add(new LongFunction());

        typeFunctions.add(new IterableFunction());
        typeFunctions.add(new StreamFunction());

        typeFunctions.add(new EnumFunction());

        typeFunctions.add(new OptionalFunction());

        typeFunctions.add(new ObjectFunction());
    }

    @Activate
    protected void activate() {
        for (TypeFunction function : otherFunctions) {
            register(function);
        }
    }

    public Class<DefaultTypeFunction> register(TypeFunction function) {
        typeFunctions.add(0, function);
        return DefaultTypeFunction.class;
    }

    @Override
    public String getTypeName(Class<?> aClass, AnnotatedType annotatedType) {
        TypeFunction typeFunction = getTypeFunction(aClass, annotatedType);
        if (typeFunction == null) {
            throw new IllegalArgumentException("unsupported type");
        }
        return typeFunction.getTypeName(aClass, annotatedType);
    }

    @Override
    public GraphQLType buildType(String typeName, Class<?> aClass, AnnotatedType annotatedType) {
        TypeFunction typeFunction = getTypeFunction(aClass, annotatedType);
        if (typeFunction == null) {
            throw new IllegalArgumentException("unsupported type");
        }

        GraphQLType result = typeFunction.buildType(typeName, aClass, annotatedType);
        if (aClass.getAnnotation(GraphQLNonNull.class) != null ||
                (annotatedType != null && annotatedType.getAnnotation(GraphQLNonNull.class) != null)) {
            result = new graphql.schema.GraphQLNonNull(result);
        }
        return result;
    }

    private TypeFunction getTypeFunction(Class<?> aClass, AnnotatedType annotatedType) {
        for (TypeFunction typeFunction : typeFunctions) {
            if (typeFunction.canBuildType(aClass, annotatedType)) {
                return typeFunction;
            }
        }
        return null;
    }
}
