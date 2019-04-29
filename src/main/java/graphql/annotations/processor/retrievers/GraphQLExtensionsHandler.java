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

import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.CannotCastMemberException;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.searchAlgorithms.SearchAlgorithm;
import graphql.schema.GraphQLFieldDefinition;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static graphql.annotations.processor.util.ObjectUtil.getAllFields;

@Component(service = GraphQLExtensionsHandler.class, immediate = true)
public class GraphQLExtensionsHandler {

    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;
    private SearchAlgorithm fieldSearchAlgorithm;
    private SearchAlgorithm methodSearchAlgorithm;
    private GraphQLFieldRetriever fieldRetriever;

    public List<GraphQLFieldDefinition> getExtensionFields(Class<?> object, List<String> definedFields, ProcessingElementsContainer container) throws CannotCastMemberException {
        List<GraphQLFieldDefinition> fields = new ArrayList<>();
        if (container.getExtensionsTypeRegistry().containsKey(object)) {
            for (Class<?> aClass : container.getExtensionsTypeRegistry().get(object)) {
                for (Method method : graphQLObjectInfoRetriever.getOrderedMethods(aClass)) {
                    if (method.isBridge() || method.isSynthetic()) {
                        continue;
                    }
                    if (methodSearchAlgorithm.isFound(method)) {
                        addExtensionField(fieldRetriever.getField(object.getTypeName(), method, container), fields, definedFields);
                    }
                }
                for (Field field : getAllFields(aClass).values()) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    if (fieldSearchAlgorithm.isFound(field)) {
                        addExtensionField(fieldRetriever.getField(object.getTypeName(), field, container), fields, definedFields);
                    }
                }
            }
        }
        return fields;
    }

    private void addExtensionField(GraphQLFieldDefinition gqlField, List<GraphQLFieldDefinition> fields, List<String> definedFields) {
        if (!definedFields.contains(gqlField.getName())) {
            definedFields.add(gqlField.getName());
            fields.add(gqlField);
        } else {
            throw new GraphQLAnnotationsException("Duplicate field found in extension : " + gqlField.getName(), null);
        }
    }


    public void registerTypeExtension(Class<?> objectClass, ProcessingElementsContainer container) {
        GraphQLTypeExtension typeExtension = objectClass.getAnnotation(GraphQLTypeExtension.class);
        if (typeExtension == null) {
            throw new GraphQLAnnotationsException("Class is not annotated with GraphQLTypeExtension", null);
        } else {
            Class<?> aClass = typeExtension.value();
            if (!container.getExtensionsTypeRegistry().containsKey(aClass)) {
                container.getExtensionsTypeRegistry().put(aClass, new HashSet<>());
            }
            container.getExtensionsTypeRegistry().get(aClass).add(objectClass);
        }
    }

    @Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setGraphQLObjectInfoRetriever(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
    }

    public void unsetGraphQLObjectInfoRetriever(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever) {
        this.graphQLObjectInfoRetriever = null;
    }


    @Reference(target = "(type=field)", policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setFieldSearchAlgorithm(SearchAlgorithm fieldSearchAlgorithm) {
        this.fieldSearchAlgorithm = fieldSearchAlgorithm;
    }

    public void unsetFieldSearchAlgorithm(SearchAlgorithm fieldSearchAlgorithm) {
        this.fieldSearchAlgorithm = null;
    }

    @Reference(target = "(type=method)", policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setMethodSearchAlgorithm(SearchAlgorithm methodSearchAlgorithm) {
        this.methodSearchAlgorithm = methodSearchAlgorithm;
    }

    public void unsetMethodSearchAlgorithm(SearchAlgorithm methodSearchAlgorithm) {
        this.methodSearchAlgorithm = null;
    }

    @Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setFieldRetriever(GraphQLFieldRetriever fieldRetriever) {
        this.fieldRetriever = fieldRetriever;
    }

    public void unsetFieldRetriever(GraphQLFieldRetriever fieldRetriever) {
        this.fieldRetriever = null;
    }
}
