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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class NamingKitTest {

    @Test
    public void testNameReplacement() throws Exception {

        String result;

        result = NamingKit.toGraphqlName("valid_Name_123");
        assertEquals(result, "valid_Name_123");

        result = NamingKit.toGraphqlName("Valid_Name_123");
        assertEquals(result, "Valid_Name_123");

        result = NamingKit.toGraphqlName("replaced$Name_123");
        assertEquals(result, "replaced_Name_123");

        result = NamingKit.toGraphqlName("com.name.replaced$Name_123");
        assertEquals(result, "com_name_replaced_Name_123");

        // start chara must be a-zA-Z
        result = NamingKit.toGraphqlName("1_invalidStart");
        assertEquals(result, "_49__invalidStart");

        result = NamingKit.toGraphqlName("$_invalidStart");
        assertEquals(result, "__invalidStart");

    }
}