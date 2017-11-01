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

import graphql.annotations.graphQLProcessors.GraphQLInputProcessor;
import graphql.annotations.graphQLProcessors.GraphQLOutputProcessor;
import graphql.annotations.typeFunctions.DefaultTypeFunction;
import graphql.annotations.util.GraphQLObjectInfoRetriever;
import graphql.relay.Relay;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import org.osgi.service.component.annotations.Component;

import java.util.*;

import static graphql.annotations.util.NamingKit.toGraphqlName;

/**
 * A utility class for extracting GraphQL data structures from annotated
 * elements.
 */
@Component
public class GraphQLAnnotations implements GraphQLAnnotationsProcessor {

    private GraphQLObjectHandler graphQLObjectHandler;
    private ProcessingElementsContainer container;

    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;

    public GraphQLAnnotations() {
        this(new DefaultTypeFunction(new GraphQLInputProcessor(), new GraphQLOutputProcessor()), new GraphQLObjectInfoRetriever(), new GraphQLObjectHandler());
    }

    public GraphQLAnnotations(TypeFunction defaultTypeFunction, GraphQLObjectInfoRetriever graphQLObjectInfoRetriever, GraphQLObjectHandler graphQLObjectHandler) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
        this.defaultTypeFunction = defaultTypeFunction;
        this.graphQLObjectHandler = graphQLObjectHandler;
        this.container = initializeContainer(this.defaultTypeFunction);
    }

    private ProcessingElementsContainer initializeContainer(TypeFunction defaultTypeFunction) {
        Map<String, graphql.schema.GraphQLType> typeRegistry = new HashMap<>();
        Map<Class<?>, Set<Class<?>>> extensionsTypeRegistry = new HashMap<>();
        final Stack<String> processing = new Stack<>();
        Relay relay = new Relay();
        ProcessingElementsContainer container = new ProcessingElementsContainer(defaultTypeFunction, relay, typeRegistry, extensionsTypeRegistry, processing);
        return container;
    }

    public static GraphQLAnnotations instance = new GraphQLAnnotations();

    public static GraphQLAnnotations getInstance() {
        return instance;
    }

    public void setRelay(Relay relay) {
        this.container.setRelay(relay);
    }


    public String getTypeName(Class<?> objectClass) {
        GraphQLName name = objectClass.getAnnotation(GraphQLName.class);
        return toGraphqlName(name == null ? objectClass.getSimpleName() : name.value());
    }

    public static GraphQLObjectType object(Class<?> object) throws GraphQLAnnotationsException {
        return new GraphQLObjectHandler().getObject(object, getInstance().getContainer());
    }

    public static class GraphQLFieldDefinitionWrapper extends GraphQLFieldDefinition {

        public GraphQLFieldDefinitionWrapper(GraphQLFieldDefinition fieldDefinition) {
            super(fieldDefinition.getName(), fieldDefinition.getDescription(), fieldDefinition.getType(),
                    fieldDefinition.getDataFetcher(), fieldDefinition.getArguments(), fieldDefinition.getDeprecationReason());
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GraphQLFieldDefinition &&
                    ((GraphQLFieldDefinition) obj).getName().contentEquals(getName());
        }
    }

    protected TypeFunction defaultTypeFunction;

    public void registerTypeExtension(Class<?> objectClass) {
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

    public void unregisterTypeExtension(Class<?> objectClass) {
        GraphQLTypeExtension typeExtension = objectClass.getAnnotation(GraphQLTypeExtension.class);
        if (typeExtension == null) {
            throw new GraphQLAnnotationsException("Class is not annotated with GraphQLTypeExtension", null);
        } else {
            Class<?> aClass = typeExtension.value();
            if (container.getExtensionsTypeRegistry().containsKey(aClass)) {
                container.getExtensionsTypeRegistry().get(aClass).remove(objectClass);
            }
        }
    }

    public void registerType(TypeFunction typeFunction) {
        ((DefaultTypeFunction) defaultTypeFunction).register(typeFunction);
    }

    public static void register(TypeFunction typeFunction) {
        getInstance().registerType(typeFunction);
    }

    public Map<String, graphql.schema.GraphQLType> getTypeRegistry() {
        return container.getTypeRegistry();
    }

    public ProcessingElementsContainer getContainer() {
        return container;
    }

}
