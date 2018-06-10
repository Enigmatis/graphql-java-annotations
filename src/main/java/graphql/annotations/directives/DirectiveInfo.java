package graphql.annotations.directives;

import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.introspection.Introspection;
import graphql.schema.GraphQLDirective;

import java.util.List;

public interface DirectiveInfo {
    String getName();

    String getDescription();

    DirectiveArgument[] getArguments();

    List<Introspection.DirectiveLocation> getValidLocations();

    GraphQLDirective toDirective(TypeFunction typeFunction, ProcessingElementsContainer container, String... argumentsValues);

    AnnotationsDirectiveWiring getSchemaDirectiveWiring();
}
