package graphql.annotations.directives;

import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.introspection.Introspection;
import graphql.schema.GraphQLDirective;

public abstract class BasicDirectiveInfo implements DirectiveInfo {
    @Override
    public GraphQLDirective toDirective(TypeFunction typeFunction, ProcessingElementsContainer container, String... argumentsValues) {
        GraphQLDirective.Builder builder = GraphQLDirective.newDirective().name(getName()).description(getDescription())
                .validLocations(getValidLocations().toArray(new Introspection.DirectiveLocation[getValidLocations().size()]));

        DirectiveArgument[] arguments = getArguments();
        for (int i = 0; i < arguments.length; i++) {
            builder.argument(new DirectiveGraphQLArgumentBuilder(arguments[i], argumentsValues[i], typeFunction, container).build());
        }

        return builder.build();
    }


}
