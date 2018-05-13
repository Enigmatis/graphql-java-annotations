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
package graphql.annotations.processor.util;

import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.schema.DataFetcher;
import org.testng.annotations.Test;

import java.lang.annotation.Annotation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author danieltaub on 07/05/2018.
 */
public class DataFetcherConstructorTest {
    private DataFetcherConstructor constructor = new DataFetcherConstructor();

    @Test
    public void graphQLDataFetcherWithArrayCtorTest() {
        GraphQLDataFetcher graphQLDataFetcher = getGraphQLDataFetcher(DataFetcherMock.class, false, "Arg1", "Arg2");
        DataFetcherMock dataFetcher = (DataFetcherMock) constructor.constructDataFetcher(null, graphQLDataFetcher);

        assertEquals(dataFetcher.getArgs().length, 2);
        assertEquals(dataFetcher.getArgs()[0], "Arg1");
        assertEquals(dataFetcher.getArgs()[1], "Arg2");
    }

    @Test
    public void graphQLDataFetcherWithParamsCtorTest() {
        GraphQLDataFetcher graphQLDataFetcher = getGraphQLDataFetcher(DataFetcherMock.class, false, "Arg1", "Arg2", "Arg3");
        DataFetcherMock dataFetcher = (DataFetcherMock) constructor.constructDataFetcher(null, graphQLDataFetcher);

        assertEquals(dataFetcher.getArgs().length, 3);
        assertEquals(dataFetcher.getArgs()[0], "Arg1");
        assertEquals(dataFetcher.getArgs()[1], "Arg2");
        assertEquals(dataFetcher.getArgs()[2], "Arg3");
    }

    @Test
    public void graphQLDataFetcherDefaultCtorTest() {
        GraphQLDataFetcher graphQLDataFetcher = getGraphQLDataFetcher(DataFetcherMock.class, false);
        DataFetcherMock dataFetcher = (DataFetcherMock) constructor.constructDataFetcher(null, graphQLDataFetcher);

        assertNull(dataFetcher.getArgs());
    }

    private GraphQLDataFetcher getGraphQLDataFetcher(final Class<? extends DataFetcher> value,
                                                     boolean argsInTarget, String... args) {
        GraphQLDataFetcher annotation = new GraphQLDataFetcher() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return GraphQLDataFetcher.class;
            }

            @Override
            public Class<? extends DataFetcher> value() {
                return value;
            }

            @Override
            public String[] args() {
                return args;
            }

            @Override
            public boolean firstArgIsTargetName() {
                return argsInTarget;
            }
        };

        return annotation;
    }
}