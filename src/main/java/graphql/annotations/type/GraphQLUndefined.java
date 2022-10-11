/**
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
package graphql.annotations.type;

import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeVisitor;
import graphql.schema.SchemaElementChildrenContainer;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;

import java.util.List;

public class GraphQLUndefined implements GraphQLType {
    @Override
    public List<GraphQLSchemaElement> getChildren() {
        return GraphQLType.super.getChildren();
    }

    @Override
    public SchemaElementChildrenContainer getChildrenWithTypeReferences() {
        return GraphQLType.super.getChildrenWithTypeReferences();
    }

    @Override
    public GraphQLSchemaElement withNewChildren(SchemaElementChildrenContainer newChildren) {
        return GraphQLType.super.withNewChildren(newChildren);
    }

    @Override
    public TraversalControl accept(TraverserContext<GraphQLSchemaElement> context, GraphQLTypeVisitor visitor) {
        return null;
    }

    @Override
    public GraphQLSchemaElement copy() {
        return new GraphQLUndefined();
    }
}
