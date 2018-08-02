package graphql.annotations.processor.directives;

import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputType;

import java.lang.reflect.Field;

import static graphql.schema.GraphQLArgument.newArgument;

public class DirectiveArgumentCreator {
    private CommonPropertiesCreator commonPropertiesCreator;
    private TypeFunction typeFunction;
    private ProcessingElementsContainer container;

    public DirectiveArgumentCreator(CommonPropertiesCreator commonPropertiesCreator, TypeFunction typeFunction, ProcessingElementsContainer container) {
        this.commonPropertiesCreator = commonPropertiesCreator;
        this.typeFunction = typeFunction;
        this.container = container;
    }


    public GraphQLArgument getArgument(Field field, Class<?> containingClass) {
        GraphQLArgument.Builder builder = newArgument();
        builder.name(commonPropertiesCreator.getName(field));
        builder.description(commonPropertiesCreator.getDescription(field));
        builder.type(getType(field));
        try {
            builder.defaultValue(getDefaultValue(field, containingClass));
        } catch (IllegalAccessException | InstantiationException e) {
            throw new GraphQLAnnotationsException(e);
        }

        return builder.build();
    }

    private Object getDefaultValue(Field field, Class<?> containingClass) throws IllegalAccessException, InstantiationException {
        field.setAccessible(true);
        Object object = containingClass.newInstance();
        return field.get(object);
    }

    private GraphQLInputType getType(Field field) {
        //todo check if primitive type, if not - throw an exception
        return (GraphQLInputType) typeFunction.buildType(true, field.getType(),
                field.getAnnotatedType(), container);
    }


}
