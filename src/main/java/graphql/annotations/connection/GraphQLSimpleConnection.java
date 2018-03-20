package graphql.annotations.connection;

import graphql.annotations.dataFetchers.connection.SimpleConnectionFetcher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphQLSimpleConnection {

    /**
     * By default, wrapped type's name is used for naming TypeConnection, but can be overridden
     * using this property
     * @return the wrapped type's name
     */
    String name() default "";

    /**
     * By default, a simple paginated data connection is specified.
     * this property allows for more efficient fetching procedures (limiting database queries, etc.)
     * NOTE: if you override this, you should also override the validator field, and specify
     * your own connection validator
     * @return a connection class
     */
    Class<? extends SimpleConnectionFetcher> connection() default SimplePaginatedDataConnectionFetcher.class;

    /**
     * By default, the the validator validates a simple paginated data connection.
     * Can be overridden (and should be) if you are using a custom connection
     * @return a connection validator
     */
    Class <? extends ConnectionValidator> validator() default SimplePaginatedDataConnectionTypeValidator.class;

    /**
     * By default, the simple paginated data is fetched synchronously. If explicitly specified, asynchronous data fetching
     * will be used.
     * @return if async fetching to be used.
     */
    boolean async() default false;
}
