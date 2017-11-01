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
package graphql.annotations.connection;

import graphql.annotations.GraphQLField;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the annotated field or method (given it is also
 * annotated with {@link GraphQLField}) is a collection that will
 * be adhering <a href="https://facebook.github.io/relay/graphql/connections.htm">Relay Connection specification</a>
 *
 * At the moment, the only allowed type for such field is <code>List&lt;?&gt;</code>
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphQLConnection {
    /**
     * By default, a simple List connection is specified, but can be overridden using
     * this property to allow for more efficient fetching procedures (limiting database queries, etc.)
     * @return a connection class
     */
    Class<? extends ConnectionFetcher> connection() default EnhancedConnectionFetcher.class;

    /**
     * By default, wrapped type's name is used for naming TypeConnection, but can be overridden
     * using this property
     * @return the wrapped type's name
     */
    String name() default "";
}
