package graphql.annotations.directives;

import graphql.schema.GraphQLDirective;

public abstract class BasicDirectiveInfo implements DirectiveInfo {
    @Override
    public GraphQLDirective toDirective() {
        return GraphQLDirective.newDirective().name(getName()).description(getDescription())
                .validLocations(getValidLocations()).build();
    }
}
