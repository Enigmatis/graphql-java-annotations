/**
 * Copyright 2016 Yurii Rashkovskii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations;

public interface GraphQLAnnotationsProcessor {
    /**
     * Register a new type extension class. This extension will be used when the extended object will be created.
     * The class must have a {@link GraphQLTypeExtension} annotation.
     *
     * @param objectClass The extension class to register
     */
    void registerTypeExtension(Class<?> objectClass);

    /**
     * Unregister a type extension class.
     *
     * @param objectClass The extension class to unregister
     */
    void unregisterTypeExtension(Class<?> objectClass);

}
