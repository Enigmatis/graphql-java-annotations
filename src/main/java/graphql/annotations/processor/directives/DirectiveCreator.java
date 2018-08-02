package graphql.annotations.processor.directives;

import graphql.annotations.directives.creation.DirectiveLocations;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.introspection.Introspection;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;

import java.lang.reflect.Field;
import java.util.Arrays;

import static graphql.schema.GraphQLDirective.newDirective;

public class DirectiveCreator {

    private DirectiveArgumentCreator directiveArgumentCreator;
    private CommonPropertiesCreator commonPropertiesCreator;


    public DirectiveCreator(DirectiveArgumentCreator directiveArgumentCreator, CommonPropertiesCreator commonPropertiesCreator) {
        this.directiveArgumentCreator = directiveArgumentCreator;
        this.commonPropertiesCreator = commonPropertiesCreator;
    }

    public GraphQLDirective getDirective(Class<?> annotatedClass) {
        GraphQLDirective.Builder builder = newDirective();
        builder.name(commonPropertiesCreator.getName(annotatedClass));
        builder.description(commonPropertiesCreator.getDescription(annotatedClass));
        Introspection.DirectiveLocation[] validLocations = getValidLocations(annotatedClass);
        if (validLocations == null || validLocations.length == 0) {
            throw new GraphQLAnnotationsException("No valid locations defined on directive", null);
        }
        builder.validLocations(validLocations);
        buildArguments(builder, annotatedClass);

        return builder.build();
    }

    private void buildArguments(GraphQLDirective.Builder builder, Class<?> annotatedClass) {
        Arrays.stream(annotatedClass.getDeclaredFields()).forEach(x ->
                builder.argument(directiveArgumentCreator.getArgument(x, annotatedClass)));
    }


    private Introspection.DirectiveLocation[] getValidLocations(Class<?> annotatedClass) {
        DirectiveLocations directiveLocationsAnnotation = annotatedClass.getAnnotation(DirectiveLocations.class);
        if (directiveLocationsAnnotation != null) {
            return directiveLocationsAnnotation.value();
        }
        return null;
    }

}
