package graphql.annotations.directives;

import graphql.introspection.Introspection;
import graphql.schema.GraphQLDirective;

public interface DirectiveInfo {
    String getName();

    String getDescription();

    Introspection.DirectiveLocation[] getValidLocations();

    GraphQLDirective toDirective();

    AnnotationsDirectiveWiring getSchemaDirectiveWiring();
}
