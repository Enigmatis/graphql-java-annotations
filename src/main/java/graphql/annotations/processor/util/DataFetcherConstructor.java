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

import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.schema.DataFetcher;

import java.lang.reflect.Constructor;
import java.util.stream.Stream;

import static graphql.annotations.processor.util.ReflectionKit.constructNewInstance;
import static graphql.annotations.processor.util.ReflectionKit.newInstance;
import static java.util.Arrays.stream;

public class DataFetcherConstructor {
    public DataFetcher constructDataFetcher(String fieldName, GraphQLDataFetcher annotatedDataFetcher) {
        final String[] args;
        if (annotatedDataFetcher.firstArgIsTargetName()) {
            args = Stream.concat(Stream.of(fieldName), stream(annotatedDataFetcher.args())).toArray(String[]::new);
        } else {
            args = annotatedDataFetcher.args();
        }
        if (args.length == 0) {
            return newInstance(annotatedDataFetcher.value());
        } else {
            try {
                final Constructor<? extends DataFetcher> ctr = annotatedDataFetcher.value().getDeclaredConstructor(
                        stream(args).map(v -> String.class).toArray(Class[]::new));
                return constructNewInstance(ctr, (Object[]) args);
            } catch (final NoSuchMethodException e) {
                throw new GraphQLAnnotationsException("Unable to instantiate DataFetcher via constructor for: " + fieldName, e);
            }
        }
    }

}
