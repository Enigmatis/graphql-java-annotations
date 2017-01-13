package graphql.annotations;

import graphql.schema.TypeResolver;

import java.util.function.Supplier;

/**
 * @author Francois Delalleau
 */
public interface TypeResolverSupplier extends Supplier<TypeResolver> {

  Class<?> resolvedInterface();

}
