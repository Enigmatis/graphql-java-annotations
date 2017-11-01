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

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;


public class BreadthFirstSearch implements SearchAlgorithm {

    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;

    public BreadthFirstSearch(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
    }

    @Override
    public boolean isFound(Member member) throws CannotCastMemberException {
        Method method=CastToMethod(member);
        final List<Class<?>> queue = new LinkedList<>();
        final String methodName = method.getName();
        final Class<?>[] parameterTypes = method.getParameterTypes();
        queue.add(method.getDeclaringClass());
        do {
            Class<?> cls = queue.remove(0);

            try {
                method = cls.getDeclaredMethod(methodName, parameterTypes);
                Boolean gqf = graphQLObjectInfoRetriever.isGraphQLField(method);
                if (gqf != null) {
                    return gqf;
                }
            } catch (NoSuchMethodException e) {
            }

            Boolean gqf = graphQLObjectInfoRetriever.isGraphQLField(cls);
            if (gqf != null) {
                return gqf;
            }

            // add interfaces to places to isFound
            for (Class<?> iface : cls.getInterfaces()) {
                queue.add(iface);
            }
            // add parent class to places to isFound
            Class<?> nxt = cls.getSuperclass();
            if (nxt != null) {
                queue.add(nxt);
            }
        } while (!queue.isEmpty());
        return false;
    }

    private Method CastToMethod(Member member) throws CannotCastMemberException {
        if(!(member instanceof Method)){
          throw new CannotCastMemberException(member.getName(),"Method");
        }
        else {
            return (Method)member;
        }
    }
}
