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
package graphql.annotations;

import graphql.schema.DataFetchingEnvironmentImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class MethodDataFetcherTest {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }


    public class TestException extends Exception {
    }

    public String method() throws TestException {
        throw new TestException();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void exceptionRethrowing() {
        try {
            MethodDataFetcher methodDataFetcher = new MethodDataFetcher(getClass().getMethod("method"));
            methodDataFetcher.get(new DataFetchingEnvironmentImpl(this, new HashMap<String,Object>(), null, null, null, new ArrayList<>(), null, null, null, null, null, null, null));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}