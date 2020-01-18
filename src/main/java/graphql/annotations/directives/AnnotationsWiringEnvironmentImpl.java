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
package graphql.annotations.directives;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLSchemaElement;

public class AnnotationsWiringEnvironmentImpl implements AnnotationsWiringEnvironment {
    private final GraphQLDirectiveContainer element;
    private final GraphQLDirective directive;
    private final GraphQLSchemaElement parentElement;
    private GraphQLCodeRegistry.Builder codeRegistryBuilder;

    public AnnotationsWiringEnvironmentImpl(GraphQLDirectiveContainer element, GraphQLDirective directive,
                                            GraphQLSchemaElement parentELement, GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        this.element = element;
        this.directive = directive;
        this.parentElement = parentELement;
        this.codeRegistryBuilder = codeRegistryBuilder;
    }

    @Override
    public GraphQLDirectiveContainer getElement() {
        return element;
    }

    @Override
    public GraphQLDirective getDirective() {
        return directive;
    }

    @Override
    public GraphQLSchemaElement getParentElement() {
        return parentElement;
    }

    @Override
    public GraphQLCodeRegistry.Builder getCodeRegistryBuilder() {
        return codeRegistryBuilder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnnotationsWiringEnvironmentImpl that = (AnnotationsWiringEnvironmentImpl) o;

        if (element != null ? !element.equals(that.element) : that.element != null) return false;
        if (parentElement != null ? !parentElement.equals(that.parentElement) : that.parentElement != null) return false;
        if (codeRegistryBuilder != null ? !codeRegistryBuilder.equals(that.codeRegistryBuilder) : that.codeRegistryBuilder != null)
            return false;
        return directive != null ? directive.equals(that.directive) : that.directive == null;
    }

    @Override
    public int hashCode() {
        int result = element != null ? element.hashCode() : 0;
        result = 31 * result + (directive != null ? directive.hashCode() : 0);
        return result;
    }
}
