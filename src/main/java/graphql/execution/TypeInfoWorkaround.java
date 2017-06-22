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

package graphql.execution;

import graphql.schema.GraphQLType;

/**
 * Graphql 3.0.0 exposed {@link TypeInfo} but the builder is not public
 * and hence it cant be used outside its package.  Until this gets fixed
 * this is the work around
 */
public class TypeInfoWorkaround {

    public static TypeInfo newTypeInfo(GraphQLType childType, TypeInfo parentTypeInfo) {
        return TypeInfo.newTypeInfo().parentInfo(parentTypeInfo).type(childType).build();
    }
}
