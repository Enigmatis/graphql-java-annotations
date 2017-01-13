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
package graphql.annotations;

import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * @author Francois Delalleau
 */
public abstract class AbstractLoader<T, U extends Provider<T>> {

  private final Class<U> providerClass;

  protected AbstractLoader(Class<U> providerClass){
    this.providerClass = providerClass;
  }

  public synchronized T get(Class<?> classToResolve) {
    Objects.requireNonNull(classToResolve);
    load();

    for (U next : types) {
      if (classToResolve.equals(next.forClass())) {
        return next.get();
      }
    }
    return null;
  }

  private synchronized void load() {
    if (types == null || !types.iterator().hasNext()) {
      types = ServiceLoader.load(providerClass);
    }
  }

  public synchronized Iterator<U> all() {
    load();
    return types.iterator();
  }

  private ServiceLoader<U> types;

}
