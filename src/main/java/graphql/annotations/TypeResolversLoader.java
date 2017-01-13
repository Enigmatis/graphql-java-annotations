package graphql.annotations;

import graphql.schema.TypeResolver;

import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * @author Francois Delalleau
 */
public class TypeResolversLoader {

  public synchronized static TypeResolver get(Class<?> classToResolve) {
    Objects.requireNonNull(classToResolve);
    load();

    for (TypeResolverSupplier next : types) {
      if (classToResolve.equals(next.resolvedInterface())) {
        return next.get();
      }
    }
    return null;
  }

  private synchronized static void load() {
    if (types == null || !types.iterator().hasNext()) {
      types = ServiceLoader.load(TypeResolverSupplier.class);
    }
  }

  public synchronized static Iterator<TypeResolverSupplier> all() {
    load();
    return types.iterator();
  }

  private static ServiceLoader<TypeResolverSupplier> types;

}
