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

import graphql.execution.batched.Batched;
import graphql.schema.DataFetchingEnvironment;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class BatchedMethodDataFetcher extends MethodDataFetcher {
    public BatchedMethodDataFetcher(Method method) {
        super(method);
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Batched method should be static");
        }
    }

    public BatchedMethodDataFetcher(Method method, TypeFunction typeFunction) {
        super(method, typeFunction);
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Batched method should be static");
        }
    }

    @Batched
    @Override
    public Object get(DataFetchingEnvironment environment) {
        return super.get(environment);
    }
}
