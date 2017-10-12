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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A package level helper in calling reflective methods and turning them into
 * GraphQLAnnotationsException runtime exceptions
 */
class ReflectionKit {
    static <T> T newInstance(Class<T> clazz) throws GraphQLAnnotationsException {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new GraphQLAnnotationsException("Unable to instantiate class : " + clazz, e);
        }
    }

    static <T> T constructNewInstance(Constructor<T> constructor, Object... args) throws GraphQLAnnotationsException {
        try {
            return constructor.newInstance(args);

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new GraphQLAnnotationsException("Unable to instantiate via constructor : " + constructor, e);
        }
    }

    static <T> Constructor<T> constructor(Class<T> type, Class<?>... parameterTypes) {
        try {
            return type.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new GraphQLAnnotationsException("Unable to find constructor", e);
        }
    }

    static <T> T newInstance(Class<T> clazz, Object parameter) {
        if (parameter != null) {
            for (Constructor<T> constructor : (Constructor<T>[]) clazz.getConstructors()) {
                if (constructor.getParameterCount() == 1 && constructor.getParameters()[0].getType().isAssignableFrom(parameter.getClass())) {
                    return constructNewInstance(constructor, parameter);
                }
            }
        }
        return null;
    }


}
