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
package graphql.annotations.processor.typeFunctions;

import graphql.annotations.processor.GraphQLAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static graphql.Scalars.GraphQLBoolean;
import static org.testng.Assert.assertEquals;
import static graphql.annotations.processor.typeFunctions.DefaultTypeFunctionTestHelper.testedDefaultTypeFunction;

public class BooleanFunctionTests {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    @Test
    public void buildType_booleanType_returnsGraphQLBoolean() {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        assertEquals(instance.buildType(boolean.class, null,null), GraphQLBoolean);
        assertEquals(instance.buildType(Boolean.class, null,null), GraphQLBoolean);
    }
}
