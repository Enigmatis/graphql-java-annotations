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


import graphql.relay.Relay;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the annotated field or method (given it is also
 * annotated with {@link graphql.annotations.annotationTypes.GraphQLField}) is a collection that will
 * be adhering <a href="https://facebook.github.io/relay/graphql/connections.htm">Relay Connection specification</a>
 *
 * At the moment, the only allowed type for such field is <code>List&lt;?&gt;</code>
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphQLConnection {
    /**
     * By default, a paginated data connection is specified.
     * this property allows for more efficient fetching procedures (limiting database queries, etc.)
     * NOTE: if you override this, you should also override the validator field, and specify
     * your own connection validator
     * @return a connection class
     */
    Class<? extends ConnectionFetcher> connectionFetcher() default PaginatedDataConnectionFetcher.class;

    /**
     * By default, wrapped type's name is used for naming TypeConnection, but can be overridden
     * using this property
     * @return the wrapped type's name
     */
    String name() default "";

    /**
     * By default, the the validator validates a paginated data connection.
     * Can be overridden (and should be) if you are using a custom connection
     * @return a connection validator
     */
    Class <? extends ConnectionValidator> validator() default PaginatedDataConnectionTypeValidator.class;

    /**
     * By default, the paginated data is fetched synchronously. If explicitly specified, asynchronous data fetching
     * will be used.
     * @return if async fetching to be used.
     */
    boolean async() default false;

    /**
     * By default, the relay connection that the container has is used. If you want to change the way connection works
     * (For example, you don't want edges and nodes), override the {@link Relay} class and specify it.
     *
     * @return a class that represents the connection type
     */
    Class<? extends Relay> connectionType() default FakeRelay.class;
}
