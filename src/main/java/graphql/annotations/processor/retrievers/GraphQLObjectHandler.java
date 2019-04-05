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
package graphql.annotations.processor.retrievers;

import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.CannotCastMemberException;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

@Component(service = GraphQLObjectHandler.class, immediate = true)
public class GraphQLObjectHandler {

    private GraphQLTypeRetriever typeRetriever;

    public GraphQLObjectType getObject(Class<?> object, ProcessingElementsContainer container) throws GraphQLAnnotationsException, CannotCastMemberException {
        return (GraphQLObjectType) getObject(object, container, false);
    }

    public GraphQLInputObjectType getInputObject(Class<?> object, ProcessingElementsContainer container) throws GraphQLAnnotationsException, CannotCastMemberException {
        return (GraphQLInputObjectType) getObject(object, container, true);
    }

    public GraphQLTypeRetriever getTypeRetriever() {
        return typeRetriever;
    }

    @Reference(policy= ReferencePolicy.DYNAMIC, policyOption= ReferencePolicyOption.GREEDY)
    public void setTypeRetriever(GraphQLTypeRetriever typeRetriever) {
        this.typeRetriever = typeRetriever;
    }

    public void unsetTypeRetriever(GraphQLTypeRetriever typeRetriever) {
        this.typeRetriever = null;
    }

    private GraphQLType getObject(Class<?> object, ProcessingElementsContainer container, boolean isInput) {
        GraphQLType type = typeRetriever.getGraphQLType(object, container, isInput);
        if (isObjectType(type)) {
            return type;
        } else {
            throw new IllegalArgumentException("Object resolve to a " + type.getClass().getSimpleName());
        }

    }

    private boolean isObjectType(GraphQLType type) {
        return type instanceof GraphQLInputObjectType ||
               type instanceof GraphQLObjectType;
    }
}
