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

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironmentImpl;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import static graphql.annotations.ReflectionKit.constructNewInstance;

 class ConnectionDataFetcher implements DataFetcher {
    private final Class<? extends Connection> connection;
    private final DataFetcher actualDataFetcher;
    private final Constructor<Connection> constructor;

    public ConnectionDataFetcher(Class<? extends Connection> connection, DataFetcher actualDataFetcher) {
        this.connection = connection;
        Optional<Constructor<Connection>> constructor =
                Arrays.asList(connection.getConstructors()).stream().
                        filter(c -> c.getParameterCount() == 1).
                        map(c -> (Constructor<Connection>) c).
                        findFirst();
        if (constructor.isPresent()) {
            this.constructor = constructor.get();
        } else {
            throw new IllegalArgumentException(connection + " doesn't have a single argument constructor");
        }
        this.actualDataFetcher = actualDataFetcher;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        // Create a list of arguments with connection specific arguments excluded
        HashMap<String, Object> arguments = new HashMap<>(environment.getArguments());
        arguments.keySet().removeAll(Arrays.asList("first", "last", "before", "after"));
        DataFetchingEnvironment env = new DataFetchingEnvironmentImpl(environment.getSource(), arguments, environment.getContext(),
                environment.getRoot(), environment.getFieldDefinition(), environment.getFields(), environment.getFieldType(), environment.getParentType(), environment.getGraphQLSchema(),
                environment.getFragmentsByName(), environment.getExecutionId(), environment.getSelectionSet(), environment.getFieldTypeInfo());
        Object data = actualDataFetcher.get(env);
        if (data != null) {
            Connection conn = constructNewInstance(constructor, data);
            return conn.get(environment);
        }
        return null;
    }
}
