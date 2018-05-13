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

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

/**
 * @author danieltaub on 07/05/2018.
 */
public class DataFetcherMock implements DataFetcher {
    private String[] args;

    public DataFetcherMock(String... args) {
        this.args = args;
    }

    public DataFetcherMock(String arg1, String arg2, String arg3) {
        this.args = new String[]{
                arg1, arg2, arg3
        };
    }

    public DataFetcherMock() {
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        return null;
    }

    public String[] getArgs() {
        return args;
    }
}
