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
package graphql.annotations.processor.retrievers.fieldBuilders.field;

import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import graphql.annotations.connection.GraphQLConnection;
import graphql.annotations.connection.GraphQLSimpleConnection;
import graphql.annotations.connection.TypesConnectionChecker;
import graphql.annotations.dataFetchers.ExtensionDataFetcherWrapper;
import graphql.annotations.dataFetchers.MethodDataFetcher;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.retrievers.fieldBuilders.Builder;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.annotations.processor.util.DataFetcherConstructor;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;
import graphql.schema.PropertyDataFetcher;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.annotations.processor.util.ConnectionUtil.getConnectionDataFetcher;
import static graphql.annotations.processor.util.ConnectionUtil.getSimpleConnectionDataFetcher;
import static java.util.Objects.nonNull;

@SuppressWarnings("ALL")
public class FieldDataFetcherBuilder implements Builder<DataFetcher> {
    private Field field;
    private DataFetcherConstructor dataFetcherConstructor;
    private GraphQLType outputType;
    private TypeFunction typeFunction;
    private ProcessingElementsContainer container;
    private TypesConnectionChecker typesConnectionChecker;

    public FieldDataFetcherBuilder(Field field, DataFetcherConstructor dataFetcherConstructor, GraphQLType outputType, TypeFunction typeFunction, ProcessingElementsContainer container, TypesConnectionChecker typesConnectionChecker) {
        this.field = field;
        this.dataFetcherConstructor = dataFetcherConstructor;
        this.outputType = outputType;
        this.typeFunction = typeFunction;
        this.container = container;
        this.typesConnectionChecker = typesConnectionChecker;
    }

    @Override
    public DataFetcher build() {
        GraphQLDataFetcher dataFetcher = field.getAnnotation(GraphQLDataFetcher.class);
        DataFetcher actualDataFetcher = null;
        if (nonNull(dataFetcher)) {
            actualDataFetcher = dataFetcherConstructor.constructDataFetcher(field.getName(), dataFetcher);
        }

        if (actualDataFetcher == null) {
            actualDataFetcher = handleNullCase(actualDataFetcher);
        }

        if (typesConnectionChecker.isConnection()) {
            actualDataFetcher = getConnectionDataFetcher(field.getAnnotation(GraphQLConnection.class), actualDataFetcher);
        }
        if (typesConnectionChecker.isSimpleConnection()) {
            actualDataFetcher = getSimpleConnectionDataFetcher(field.getAnnotation(GraphQLSimpleConnection.class), actualDataFetcher);
        }

        return actualDataFetcher;
    }

    private DataFetcher handleNullCase(DataFetcher actualDataFetcher) {

        // if there is getter for fields type, use propertyDataFetcher, otherwise use method directly
        if (isaBoolean()) {
            actualDataFetcher = getBooleanDataFetcher(actualDataFetcher);
        } else if (checkIfPrefixGetterExists(field.getDeclaringClass(), "get", field.getName())) {
            actualDataFetcher = wrapExtension(new PropertyDataFetcher(field.getName()), field);
        } else {
            actualDataFetcher = getDataFetcherWithFluentGetter(actualDataFetcher);
        }

        if (actualDataFetcher == null) {
            actualDataFetcher = wrapExtension(new PropertyDataFetcher(field.getName()), field);
        }

        return actualDataFetcher;
    }

    private DataFetcher getDataFetcherWithFluentGetter(DataFetcher actualDataFetcher) {
        StringBuilder fluentBuffer = new StringBuilder(field.getName());
        fluentBuffer.setCharAt(0, Character.toLowerCase(fluentBuffer.charAt(0)));
        String fluentGetter = fluentBuffer.toString();

        boolean hasFluentGetter = false;
        Method fluentMethod = null;
        try {
            fluentMethod = field.getDeclaringClass().getMethod(fluentGetter);
            hasFluentGetter = true;
        } catch (NoSuchMethodException ignored) {
        }

        if (hasFluentGetter) {
            actualDataFetcher = new MethodDataFetcher(fluentMethod, typeFunction, container);
        }
        return actualDataFetcher;
    }

    private DataFetcher wrapExtension(DataFetcher dataFetcher, Field field) {
        if (field.getDeclaringClass().isAnnotationPresent(GraphQLTypeExtension.class)) {
            return new ExtensionDataFetcherWrapper(field.getDeclaringClass(), dataFetcher);
        }

        return dataFetcher;
    }

    private DataFetcher getBooleanDataFetcher(DataFetcher actualDataFetcher) {
        if (checkIfPrefixGetterExists(field.getDeclaringClass(), "is", field.getName()) ||
                checkIfPrefixGetterExists(field.getDeclaringClass(), "get", field.getName())) {
            actualDataFetcher = wrapExtension(new PropertyDataFetcher(field.getName()), field);
        }
        return actualDataFetcher;
    }

    private boolean isaBoolean() {
        return outputType == GraphQLBoolean || (outputType instanceof GraphQLNonNull && ((GraphQLNonNull) outputType).getWrappedType() == GraphQLBoolean);
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

}
