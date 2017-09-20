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

import java.util.Map;

import static graphql.annotations.ReflectionKit.newInstance;

public class ExtensionDataFetcherWrapper implements DataFetcher {

    private final Class declaringClass;
    private final DataFetcher dataFetcher;

    public ExtensionDataFetcherWrapper(Class declaringClass, DataFetcher dataFetcher) {
        this.declaringClass = declaringClass;
        this.dataFetcher = dataFetcher;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object get(DataFetchingEnvironment environment) {
        Object source = environment.getSource();
        if (source != null && (!declaringClass.isInstance(source)) && !(source instanceof Map)) {
            environment = new DataFetchingEnvironmentImpl(newInstance(declaringClass, source), environment.getArguments(),
                    environment.getContext(), environment.getFields(), environment.getFieldType(), environment.getParentType(),
                    environment.getGraphQLSchema(), environment.getFragmentsByName(), environment.getExecutionId(), environment.getSelectionSet());
        }

        return dataFetcher.get(environment);
    }

}
