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
package graphql.annotations.processor.util;

import graphql.annotations.connection.ConnectionValidator;
import graphql.annotations.connection.GraphQLConnection;
import graphql.annotations.connection.GraphQLSimpleConnection;
import graphql.annotations.dataFetchers.connection.AsyncConnectionDataFetcher;
import graphql.annotations.dataFetchers.connection.AsyncSimpleConnectionDataFetcher;
import graphql.annotations.dataFetchers.connection.ConnectionDataFetcher;
import graphql.annotations.dataFetchers.connection.SimpleConnectionDataFetcher;
import graphql.schema.*;

import java.lang.reflect.AccessibleObject;
import java.util.Arrays;
import java.util.List;

import static graphql.annotations.processor.util.ReflectionKit.newInstance;

@SuppressWarnings("ALL")
public class ConnectionUtil {
    private static final List<Class> TYPES_FOR_CONNECTION = Arrays.asList(GraphQLObjectType.class, GraphQLInterfaceType.class, GraphQLUnionType.class, GraphQLTypeReference.class);

    public static boolean isConnection(AccessibleObject obj, GraphQLType type) {
        final GraphQLType actualType = getActualType(type);

        boolean isValidGraphQLTypeForConnection = obj.isAnnotationPresent(GraphQLConnection.class) &&
                isValidTypeForConnection(actualType);

        if (isValidGraphQLTypeForConnection) {
            ConnectionValidator validator = newInstance(obj.getAnnotation(GraphQLConnection.class).validator());
            validator.validate(obj);
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSimpleConnection(AccessibleObject obj, GraphQLType type) {
        final GraphQLType actualType = getActualType(type);

        boolean isValidGraphQLTypeForConnection = obj.isAnnotationPresent(GraphQLSimpleConnection.class) &&
                isValidTypeForConnection(actualType);

        if (isValidGraphQLTypeForConnection) {
            ConnectionValidator validator = newInstance(obj.getAnnotation(GraphQLSimpleConnection.class).validator());
            validator.validate(obj);
            return true;
        } else {
            return false;
        }
    }

    private static boolean isValidTypeForConnection(GraphQLType actualType) {
        return actualType instanceof GraphQLList && TYPES_FOR_CONNECTION.stream().anyMatch(aClass ->
                aClass.isInstance(((GraphQLList) actualType).getWrappedType()));
    }

    private static GraphQLType getActualType(GraphQLType type) {
        if (type instanceof GraphQLNonNull) {
            return ((GraphQLNonNull) type).getWrappedType();
        }
        return type;
    }

    public static DataFetcher getConnectionDataFetcher(GraphQLConnection connectionAnnotation, DataFetcher actualDataFetcher) {
        actualDataFetcher = new ConnectionDataFetcher(connectionAnnotation.connection(), actualDataFetcher);
        if (connectionAnnotation.async()) {
            actualDataFetcher = new AsyncConnectionDataFetcher((ConnectionDataFetcher) actualDataFetcher);
        }

        return actualDataFetcher;
    }

    public static DataFetcher getSimpleConnectionDataFetcher(GraphQLSimpleConnection connectionAnnotation, DataFetcher actualDataFetcher) {
        actualDataFetcher = new SimpleConnectionDataFetcher(actualDataFetcher, connectionAnnotation.connection());
        if (connectionAnnotation.async()) {
            actualDataFetcher = new AsyncSimpleConnectionDataFetcher((SimpleConnectionDataFetcher) actualDataFetcher);
        }

        return actualDataFetcher;
    }

}
