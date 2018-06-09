package graphql.annotations.directives;

import graphql.introspection.Introspection;
import graphql.schema.GraphQLDirective;

public abstract class BasicDirectiveInfo implements DirectiveInfo {
    @Override
    public GraphQLDirective toDirective() {
        return GraphQLDirective.newDirective().name(getName()).description(getDescription())
                .validLocations(getValidLocations().toArray(new Introspection.DirectiveLocation[getValidLocations().size()])).build();
    }
}
