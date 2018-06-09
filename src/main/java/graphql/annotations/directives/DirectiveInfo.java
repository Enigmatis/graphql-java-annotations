package graphql.annotations.directives;

import graphql.introspection.Introspection;
import graphql.schema.GraphQLDirective;

import java.util.List;

public interface DirectiveInfo {
    String getName();

    String getDescription();

    List<Introspection.DirectiveLocation> getValidLocations();

    GraphQLDirective toDirective();

    AnnotationsDirectiveWiring getSchemaDirectiveWiring();
}
