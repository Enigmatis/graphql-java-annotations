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
package graphql.annotations.processor.util;

import graphql.relay.DefaultConnection;
import graphql.relay.DefaultPageInfo;
import graphql.relay.PageInfo;

import java.util.Collections;

public class RelayKit {
    /**
     * An empty page info
     */
    public static final PageInfo EMPTY_PAGE_INFO = new DefaultPageInfo(null, null, false, false);

    /**
     * An empty connection
     */
    public static final graphql.relay.Connection<Object> EMPTY_CONNECTION = new DefaultConnection<>(Collections.emptyList(), EMPTY_PAGE_INFO);
}
