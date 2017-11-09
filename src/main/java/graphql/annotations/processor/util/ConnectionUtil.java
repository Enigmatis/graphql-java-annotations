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
package graphql.annotations.processor.util;

import graphql.annotations.connection.ConnectionValidator;
import graphql.annotations.connection.GraphQLConnection;
import graphql.schema.*;

import java.lang.reflect.AccessibleObject;
import java.util.Arrays;
import java.util.List;

import static graphql.annotations.processor.util.ReflectionKit.newInstance;

public class ConnectionUtil {
    private static final List<Class> TYPES_FOR_CONNECTION = Arrays.asList(GraphQLObjectType.class, GraphQLInterfaceType.class, GraphQLUnionType.class, GraphQLTypeReference.class);

    public static boolean isConnection(AccessibleObject obj, GraphQLOutputType type) {
        if (type instanceof graphql.schema.GraphQLNonNull) {
            type = (GraphQLOutputType) ((GraphQLNonNull) type).getWrappedType();
        }
        final GraphQLOutputType actualType = type;
        boolean isValidGraphQLTypeForConnection = obj.isAnnotationPresent(GraphQLConnection.class) &&
                actualType instanceof GraphQLList && TYPES_FOR_CONNECTION.stream().anyMatch(aClass ->
                aClass.isInstance(((GraphQLList) actualType).getWrappedType()));

        if (isValidGraphQLTypeForConnection) {
            ConnectionValidator validator = newInstance(obj.getAnnotation(GraphQLConnection.class).validator());
            validator.validate(obj);
            return true;
        } else {
            return false;
        }
    }

}
