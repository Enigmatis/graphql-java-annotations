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
package graphql.annotations.processor.typeFunctions;

import graphql.annotations.processor.graphQLProcessors.GraphQLAnnotationsProcessor;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import graphql.annotations.processor.graphQLProcessors.GraphQLInputProcessor;
import graphql.annotations.processor.graphQLProcessors.GraphQLOutputProcessor;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.schema.GraphQLType;
import org.osgi.service.component.annotations.*;

import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component(property = "type=default")
public class DefaultTypeFunction implements TypeFunction {

    protected List<TypeFunction> otherFunctions = new ArrayList<>();

    private CopyOnWriteArrayList<TypeFunction> typeFunctions;

    GraphQLAnnotationsProcessor annotationsProcessor;

    @Override
    public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
        return getTypeFunction(aClass, annotatedType) != null;
    }

    public void setAnnotationsProcessor(GraphQLAnnotationsProcessor annotationsProcessor) {
        this.annotationsProcessor = annotationsProcessor;
    }


    public DefaultTypeFunction(GraphQLInputProcessor graphQLInputProcessor, GraphQLOutputProcessor graphQLOutputProcessor) {
        typeFunctions = new CopyOnWriteArrayList<>();

        typeFunctions.add(new IDFunction());
        typeFunctions.add(new StringFunction());
        typeFunctions.add(new BooleanFunction());
        typeFunctions.add(new FloatFunction());
        typeFunctions.add(new IntegerFunction());
        typeFunctions.add(new LongFunction());
        typeFunctions.add(new IterableFunction(DefaultTypeFunction.this));
        typeFunctions.add(new StreamFunction(DefaultTypeFunction.this));
        typeFunctions.add(new OptionalFunction(DefaultTypeFunction.this));
        typeFunctions.add(new ObjectFunction(graphQLInputProcessor, graphQLOutputProcessor));
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            service = TypeFunction.class,
            target = "(!(type=default))")
    void addFunction(TypeFunction function) {
        this.otherFunctions.add(function);
    }

    void removeFunction(TypeFunction function) {
        this.otherFunctions.remove(function);
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
    public GraphQLType buildType(boolean input, Class<?> aClass, AnnotatedType annotatedType, ProcessingElementsContainer container) {
        TypeFunction typeFunction = getTypeFunction(aClass, annotatedType);
        if (typeFunction == null) {
            throw new IllegalArgumentException("unsupported type");
        }

        GraphQLType result = typeFunction.buildType(input, aClass, annotatedType, container);
        if (annotatedType != null && annotatedType.isAnnotationPresent(GraphQLNonNull.class)) {
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
