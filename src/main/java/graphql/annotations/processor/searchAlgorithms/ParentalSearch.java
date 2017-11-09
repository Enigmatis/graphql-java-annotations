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
package graphql.annotations.processor.searchAlgorithms;

import graphql.annotations.processor.exceptions.CannotCastMemberException;
import graphql.annotations.processor.retrievers.GraphQLObjectInfoRetriever;

import java.lang.reflect.Field;
import java.lang.reflect.Member;


public class ParentalSearch implements SearchAlgorithm {
    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;

    public ParentalSearch(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
    }

    @Override
    public boolean isFound(Member member) throws CannotCastMemberException {
        Field field=CastToField(member);
        Boolean gqf = graphQLObjectInfoRetriever.isGraphQLField(field);
        if (gqf != null) {
            return gqf;
        }
        Class<?> cls = field.getDeclaringClass();

        do {
            gqf = graphQLObjectInfoRetriever.isGraphQLField(cls);
            if (gqf != null) {
                return gqf;
            }
            cls = cls.getSuperclass();
        } while (cls != null);
        return false;
    }

    private Field CastToField(Member member) throws CannotCastMemberException {
        if(!(member instanceof Field)){
           throw new CannotCastMemberException(member.getName(),"Field");
        }
        else
        {
            return (Field) member;
        }

    }
}
