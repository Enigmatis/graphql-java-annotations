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
package graphql.annotations.dataFetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.util.Map;

import static graphql.annotations.processor.util.ReflectionKit.newInstance;
import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;

public class ExtensionDataFetcherWrapper<T> implements DataFetcher<T> {

    private final Class declaringClass;

    private final DataFetcher<T> dataFetcher;

    public ExtensionDataFetcherWrapper(Class declaringClass, DataFetcher<T> dataFetcher) {
        this.declaringClass = declaringClass;
        this.dataFetcher = dataFetcher;
    }

    @Override
    public T get(DataFetchingEnvironment environment) throws Exception {
        Object source = environment.getSource();
        if (source != null && (!declaringClass.isInstance(source)) && !(source instanceof Map)) {
            environment = newDataFetchingEnvironment(environment).source(newInstance(declaringClass, source)).build();
        }
        return dataFetcher.get(environment);
    }

    public DataFetcher<T> getUnwrappedDataFetcher() {
        return dataFetcher;
    }

}
