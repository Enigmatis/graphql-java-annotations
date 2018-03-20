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
package graphql.annotations.processor.typeBuilders;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLTypeResolver;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.CannotCastMemberException;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.retrievers.GraphQLExtensionsHandler;
import graphql.annotations.processor.retrievers.GraphQLFieldRetriever;
import graphql.annotations.processor.retrievers.GraphQLObjectInfoRetriever;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static graphql.annotations.processor.util.ReflectionKit.newInstance;
import static graphql.schema.GraphQLInterfaceType.newInterface;
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
public class InterfaceBuilder {

    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;
    private GraphQLFieldRetriever graphQLFieldRetriever;
    private GraphQLExtensionsHandler extensionsHandler;

    public InterfaceBuilder(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever, GraphQLFieldRetriever graphQLFieldRetriever, GraphQLExtensionsHandler extensionsHandler) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
        this.graphQLFieldRetriever=graphQLFieldRetriever;
        this.extensionsHandler = extensionsHandler;
    }

    public GraphQLInterfaceType.Builder getInterfaceBuilder(Class<?> iface, ProcessingElementsContainer container) throws GraphQLAnnotationsException,
            IllegalArgumentException, CannotCastMemberException {
        if (!iface.isInterface()) {
            throw new IllegalArgumentException(iface + " is not an interface");
        }
        GraphQLInterfaceType.Builder builder = newInterface();

        builder.name(graphQLObjectInfoRetriever.getTypeName(iface));
        GraphQLDescription description = iface.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            builder.description(description.value());
        }
        List<String> definedFields = new ArrayList<>();
        for (Method method : graphQLObjectInfoRetriever.getOrderedMethods(iface)) {
            boolean valid = !Modifier.isStatic(method.getModifiers()) &&
                    method.getAnnotation(GraphQLField.class) != null;
            if (valid) {
                GraphQLFieldDefinition gqlField = graphQLFieldRetriever.getField(method,container);
                definedFields.add(gqlField.getName());
                builder.field(gqlField);
            }
        }
        builder.fields(extensionsHandler.getExtensionFields(iface, definedFields,container));

        GraphQLTypeResolver typeResolver = iface.getAnnotation(GraphQLTypeResolver.class);
        builder.typeResolver(newInstance(typeResolver.value()));
        return builder;
    }
}
