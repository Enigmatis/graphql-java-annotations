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

import graphql.annotations.annotationTypes.GraphQLTypeResolver;
import graphql.annotations.annotationTypes.GraphQLUnion;
import graphql.annotations.directives.DirectiveWirer;
import graphql.annotations.directives.DirectiveWiringMapRetriever;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.CannotCastMemberException;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.searchAlgorithms.SearchAlgorithm;
import graphql.annotations.processor.typeBuilders.*;
import graphql.schema.*;
import org.osgi.service.component.annotations.*;

@Component(service = GraphQLTypeRetriever.class, immediate = true)
public class GraphQLTypeRetriever {

    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;
    private GraphQLInterfaceRetriever graphQLInterfaceRetriever;
    private GraphQLFieldRetriever graphQLFieldRetriever;
    private SearchAlgorithm fieldSearchAlgorithm;
    private SearchAlgorithm methodSearchAlgorithm;
    private GraphQLExtensionsHandler extensionsHandler;


    /**
     * This will examine the object and will return a {@link GraphQLType} based on the class type and annotationTypes.
     * - If its annotated with {@link graphql.annotations.annotationTypes.GraphQLUnion} it will return a {@link GraphQLUnionType}
     * - If its annotated with {@link graphql.annotations.annotationTypes.GraphQLTypeResolver} it will return a {@link GraphQLInterfaceType}
     * - It it's an Enum it will return a {@link GraphQLEnumType},
     * otherwise it will return a {@link GraphQLObjectType}.
     *
     * @param object    the object class to examine*
     * @param container a class that hold several members that are required in order to build schema
     * @param isInput   true if the type is an input type, false otherwise
     * @return a {@link GraphQLType} that represents that object class
     * @throws graphql.annotations.processor.exceptions.GraphQLAnnotationsException if the object class cannot be examined
     * @throws graphql.annotations.processor.exceptions.CannotCastMemberException   if the object class cannot be examined
     */
    public GraphQLType getGraphQLType(Class<?> object, ProcessingElementsContainer container, boolean isInput) throws GraphQLAnnotationsException, CannotCastMemberException {
        // because the TypeFunction can call back to this processor and
        // Java classes can be circular, we need to protect against
        // building the same type twice because graphql-java 3.x requires
        // all type instances to be unique singletons
        String typeName = graphQLObjectInfoRetriever.getTypeName(object);
        GraphQLType type;

        if (isInput) {
            typeName = container.getInputPrefix() + typeName + container.getInputSuffix();
        }

        if (container.getProcessing().contains(typeName)) {
            return new GraphQLTypeReference(typeName);
        }

        type = container.getTypeRegistry().get(typeName);
        if (type != null) return type;

        container.getProcessing().push(typeName);
        if (object.getAnnotation(GraphQLUnion.class) != null) {
            type = new UnionBuilder(graphQLObjectInfoRetriever).getUnionBuilder(object, container).build();
        } else if (object.isAnnotationPresent(GraphQLTypeResolver.class)) {
            type = new InterfaceBuilder(graphQLObjectInfoRetriever, graphQLFieldRetriever, extensionsHandler).getInterfaceBuilder(object, container).build();
        } else if (Enum.class.isAssignableFrom(object)) {
            type = new EnumBuilder(graphQLObjectInfoRetriever).getEnumBuilder(object).build();
        } else {
            if (isInput) {
                type = new InputObjectBuilder(graphQLObjectInfoRetriever, fieldSearchAlgorithm, methodSearchAlgorithm,
                        graphQLFieldRetriever).getInputObjectBuilder(object, container).build();
            } else {
                type = new OutputObjectBuilder(graphQLObjectInfoRetriever, fieldSearchAlgorithm, methodSearchAlgorithm,
                        graphQLFieldRetriever, graphQLInterfaceRetriever, extensionsHandler).getOutputObjectBuilder(object, container).build();
            }
        }

        DirectiveWirer directiveWirer = new DirectiveWirer();

        // wire the type with the directives and change the original type
        type = directiveWirer.wire((GraphQLDirectiveContainer) type,
                new DirectiveWiringMapRetriever().getDirectiveWiringMap(object, container),
                container.getCodeRegistryBuilder(), null);

        container.getTypeRegistry().put(type.getName(), type);
        container.getProcessing().pop();

        return type;
    }

    public GraphQLObjectInfoRetriever getGraphQLObjectInfoRetriever() {
        return graphQLObjectInfoRetriever;
    }

    public GraphQLInterfaceRetriever getGraphQLInterfaceRetriever() {
        return graphQLInterfaceRetriever;
    }

    public GraphQLFieldRetriever getGraphQLFieldRetriever() {
        return graphQLFieldRetriever;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setGraphQLObjectInfoRetriever(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
    }

    public void unsetGraphQLObjectInfoRetriever(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever) {
        this.graphQLObjectInfoRetriever = null;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setGraphQLInterfaceRetriever(GraphQLInterfaceRetriever graphQLInterfaceRetriever) {
        this.graphQLInterfaceRetriever = graphQLInterfaceRetriever;
    }

    public void unsetGraphQLInterfaceRetriever(GraphQLInterfaceRetriever graphQLInterfaceRetriever) {
        this.graphQLInterfaceRetriever = null;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setGraphQLFieldRetriever(GraphQLFieldRetriever graphQLFieldRetriever) {
        this.graphQLFieldRetriever = graphQLFieldRetriever;
    }

    public void unsetGraphQLFieldRetriever(GraphQLFieldRetriever graphQLFieldRetriever) {
        this.graphQLFieldRetriever = null;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, target = "(type=field)", policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setFieldSearchAlgorithm(SearchAlgorithm fieldSearchAlgorithm) {
        this.fieldSearchAlgorithm = fieldSearchAlgorithm;
    }

    public void unsetFieldSearchAlgorithm(SearchAlgorithm fieldSearchAlgorithm) {
        this.fieldSearchAlgorithm = null;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, target = "(type=method)", policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setMethodSearchAlgorithm(SearchAlgorithm methodSearchAlgorithm) {
        this.methodSearchAlgorithm = methodSearchAlgorithm;
    }

    public void unsetMethodSearchAlgorithm(SearchAlgorithm methodSearchAlgorithm) {
        this.methodSearchAlgorithm = null;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setExtensionsHandler(GraphQLExtensionsHandler extensionsHandler) {
        this.extensionsHandler = extensionsHandler;
    }

    public void unsetExtensionsHandler(GraphQLExtensionsHandler extensionsHandler) {
        this.extensionsHandler = null;
    }

}
