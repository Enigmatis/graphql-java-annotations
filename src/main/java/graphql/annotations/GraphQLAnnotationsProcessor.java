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

import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLUnionType;

public interface GraphQLAnnotationsProcessor {
    /**
     * @param iface interface
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException if <code>iface</code> is not an interface or doesn't have <code>@GraphTypeResolver</code> annotation
     */
    graphql.schema.GraphQLType getInterface(Class<?> iface)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException;

    GraphQLUnionType.Builder getUnionBuilder(Class<?> iface) throws InstantiationException,
                                                                    IllegalAccessException;

    GraphQLInterfaceType.Builder getIfaceBuilder(Class<?> iface) throws InstantiationException,
                                                                        IllegalAccessException;

    /**
     * @param object
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     */
    GraphQLObjectType getObject(Class<?> object) throws IllegalAccessException, InstantiationException,
                                                        NoSuchMethodException;

    GraphQLObjectType.Builder getObjectBuilder(Class<?> object) throws NoSuchMethodException,
                                                                       InstantiationException, IllegalAccessException;

    GraphQLInputObjectType getInputObject(GraphQLObjectType graphQLType);
}
