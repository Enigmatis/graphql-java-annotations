package graphql.annotations.directives;

import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.introspection.Introspection;
import graphql.schema.GraphQLDirective;

public abstract class BasicDirectiveInfo implements DirectiveInfo {
    @Override
    public GraphQLDirective toDirective(TypeFunction typeFunction, ProcessingElementsContainer container, String... argumentsValues) {
        GraphQLDirective.Builder builder = GraphQLDirective.newDirective().name(getName()).description(getDescription())
                .validLocations(getValidLocations().toArray(new Introspection.DirectiveLocation[getValidLocations().size()]));


        DirectiveArgument[] arguments = getArguments();

        if (argumentsValues.length > arguments.length) {
            throw new GraphQLAnnotationsException(String.format("Directive '%s' is supplied with more argument values than it supports", getName()), null);
        }

        // run over the arguments with supplied values
        for (int i = 0; i < argumentsValues.length; i++) {
            try {
                builder.argument(new DirectiveGraphQLArgumentBuilder(arguments[i], argumentsValues[i], typeFunction, container).build());
            } catch (GraphQLAnnotationsException e) {
                throw new GraphQLAnnotationsException(String.format("Exception while creating argument for directive '%s': %s", getName(), e.getMessage()), e);
            }
        }

        // run over the rest of the arguments with no supplied value
        for (int i = arguments.length - argumentsValues.length; i < arguments.length; i++) {
            try {
                builder.argument(new DirectiveGraphQLArgumentBuilder(arguments[i], null, typeFunction, container).build());
            } catch (GraphQLAnnotationsException e) {
                throw new GraphQLAnnotationsException(String.format("Exception while creating argument for directive '%s': %s", getName(), e.getMessage()), e);
            }
        }

        return builder.build();
    }


}
