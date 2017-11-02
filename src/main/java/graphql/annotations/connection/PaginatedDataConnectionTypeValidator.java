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
package graphql.annotations.connection;

import graphql.annotations.GraphQLDataFetcher;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PaginatedDataConnectionTypeValidator implements ConnectionValidator {

    public void validate(AccessibleObject field) {
        if (field instanceof Field) {
            if (field.isAnnotationPresent(GraphQLConnection.class) && !field.isAnnotationPresent(GraphQLDataFetcher.class)) {
                throw new GraphQLConnectionException("Please don't use @GraphQLConnection on" + ((Field) field).getName() +
                        " without @GraphQLDataFetcher, because " +
                        "neither PropertyDataFetcher nor FieldDataFetcher know how to handle connection");
            }

            if (!PaginatedData.class.isAssignableFrom(((Field) field).getType())) {
                throw new GraphQLConnectionException(((Field) field).getName() + " type must be PaginatedData");
            }
        } else {
            if (!PaginatedData.class.isAssignableFrom(((Method) field).getReturnType())) {
                throw new GraphQLConnectionException(((Method) field).getName() + " return type must be PaginatedData");
            }
        }
    }
}
