package graphql.annotations.processor.retrievers.fieldBuilders;

import graphql.annotations.annotationTypes.GraphQLDirectives;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLScalarType;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static graphql.schema.GraphQLDirective.newDirective;

public class DirectivesBuilder implements Builder<GraphQLDirective[]> {
    private AnnotatedElement object;
    private TypeFunction typeFunction;
    private ProcessingElementsContainer container;

    public DirectivesBuilder(AnnotatedElement object, TypeFunction typeFunction, ProcessingElementsContainer container) {
        this.object = object;
        this.typeFunction = typeFunction;
        this.container = container;
    }

    private GraphQLDirective transformArgs(GraphQLDirective graphQLDirective, String[] argumentValues) {
        GraphQLDirective.Builder directiveBuilder = newDirective(graphQLDirective);
        directiveBuilder.clearArguments();

        List<GraphQLArgument> arguments = graphQLDirective.getArguments();

        if (argumentValues.length > arguments.size()) {
            throw new GraphQLAnnotationsException(String.format("Directive '%s' is supplied with more argument values than it supports", graphQLDirective.getName()), null);
        }

        for (int i = 0; i < argumentValues.length; i++) {
            int finalI = i;
            GraphQLArgument graphQLArgument = arguments.get(i);

            directiveBuilder.argument(graphQLArgument.transform(builder -> {
                String argumentValue = argumentValues[finalI];
                try {
                    if (graphQLArgument.getType() instanceof GraphQLScalarType) {
                        Object value = ((GraphQLScalarType) graphQLArgument.getType()).getCoercing().parseValue(argumentValue);
                        builder.value(value);
                    } else {
                        throw new GraphQLAnnotationsException("Directive argument type must be a scalar!", null);
                    }
                } catch (Exception e) {
                    throw new GraphQLAnnotationsException("Could not parse argument value to argument type", e);
                }
            }));
        }

        for (int i = argumentValues.length; i < arguments.size(); i++) {
            int finalI = i;
            directiveBuilder.argument(arguments.get(i).transform(builder -> builder.value(arguments.get(finalI).getDefaultValue())));
        }
        return directiveBuilder.build();
    }

    @Override
    public GraphQLDirective[] build() {
        GraphQLDirectives directives = object.getAnnotation(GraphQLDirectives.class);
        if (directives == null) return new GraphQLDirective[]{};
        List<GraphQLDirective> graphQLDirectives = Arrays.stream(directives.value())
                .map(x -> transformArgs(container.getDirectiveRegistry().get(x.name()), x.argumentsValues())).collect(Collectors.toList());

        return graphQLDirectives.toArray(new GraphQLDirective[graphQLDirectives.size()]);
    }
}
