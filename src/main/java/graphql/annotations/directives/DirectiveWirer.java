package graphql.annotations.directives;

import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.introspection.Introspection;
import graphql.schema.*;

import java.util.Arrays;

public class DirectiveWirer {
    public GraphQLDirectiveContainer wire(GraphQLDirectiveContainer element, ProcessingElementsContainer container, DirectiveInfo... directiveInfos) {
        for (DirectiveInfo directiveInfo : directiveInfos) {
            if (element instanceof GraphQLFieldDefinition) {
                assertLocation(directiveInfo, element, Introspection.DirectiveLocation.FIELD, Introspection.DirectiveLocation.FIELD_DEFINITION);
                element = directiveInfo.getSchemaDirectiveWiring()
                        .onField(new AnnotationsWiringEnvironmentImpl<>((GraphQLFieldDefinition) element, element.getDirective(directiveInfo.getName())));
            } else if (element instanceof GraphQLObjectType) {
                assertLocation(directiveInfo, element, Introspection.DirectiveLocation.OBJECT);
                element = directiveInfo.getSchemaDirectiveWiring()
                        .onObject(new AnnotationsWiringEnvironmentImpl<>((GraphQLObjectType) element, element.getDirective(directiveInfo.getName())));
            } else if (element instanceof GraphQLArgument) {
                assertLocation(directiveInfo, element, Introspection.DirectiveLocation.ARGUMENT_DEFINITION);
                element = directiveInfo.getSchemaDirectiveWiring()
                        .onArgument(new AnnotationsWiringEnvironmentImpl<>((GraphQLArgument) element, element.getDirective(directiveInfo.getName())));
            } else if (element instanceof GraphQLInterfaceType) {
                assertLocation(directiveInfo, element, Introspection.DirectiveLocation.INTERFACE);
                element = directiveInfo.getSchemaDirectiveWiring()
                        .onInterface(new AnnotationsWiringEnvironmentImpl<>((GraphQLInterfaceType) element, element.getDirective(directiveInfo.getName())));
            } else if (element instanceof GraphQLUnionType) {
                assertLocation(directiveInfo, element, Introspection.DirectiveLocation.UNION);
                element = directiveInfo.getSchemaDirectiveWiring()
                        .onUnion(new AnnotationsWiringEnvironmentImpl<>((GraphQLUnionType) element, element.getDirective(directiveInfo.getName())));
            } else if (element instanceof GraphQLEnumType) { // todo support enum
                assertLocation(directiveInfo, element, Introspection.DirectiveLocation.ENUM);
                element = directiveInfo.getSchemaDirectiveWiring()
                        .onEnum(new AnnotationsWiringEnvironmentImpl<>((GraphQLEnumType) element, element.getDirective(directiveInfo.getName())));
            } else if (element instanceof GraphQLEnumValueDefinition) { // todo support enum value
                assertLocation(directiveInfo, element, Introspection.DirectiveLocation.ENUM_VALUE);
                element = directiveInfo.getSchemaDirectiveWiring()
                        .onEnumValue(new AnnotationsWiringEnvironmentImpl<>((GraphQLEnumValueDefinition) element, element.getDirective(directiveInfo.getName())));
            } else if (element instanceof GraphQLScalarType) { // todo: support scalars
                assertLocation(directiveInfo, element, Introspection.DirectiveLocation.SCALAR);
                element = directiveInfo.getSchemaDirectiveWiring()
                        .onScalar(new AnnotationsWiringEnvironmentImpl<>((GraphQLScalarType) element, element.getDirective(directiveInfo.getName())));
            } else if (element instanceof GraphQLInputObjectType) {
                assertLocation(directiveInfo, element, Introspection.DirectiveLocation.INPUT_OBJECT);
                element = directiveInfo.getSchemaDirectiveWiring()
                        .onInputObjectType(new AnnotationsWiringEnvironmentImpl<>((GraphQLInputObjectType) element, element.getDirective(directiveInfo.getName())));
            } else if (element instanceof GraphQLInputObjectField) {
                assertLocation(directiveInfo, element, Introspection.DirectiveLocation.INPUT_FIELD_DEFINITION);
                element = directiveInfo.getSchemaDirectiveWiring()
                        .onInputObjectField(new AnnotationsWiringEnvironmentImpl<>((GraphQLInputObjectField) element, element.getDirective(directiveInfo.getName())));
            }

        }
        return element;

    }

    private void assertLocation(DirectiveInfo directiveInfo, GraphQLDirectiveContainer element, Introspection.DirectiveLocation... validLocations) {
        boolean isSupported = false;
        for (Introspection.DirectiveLocation validLocation : validLocations) {
            if (directiveInfo.getValidLocations().contains(validLocation)) {
                isSupported = true;
            }
        }
        if (!isSupported) {
            throw getInvalidDirectiveLocationException(element, directiveInfo, validLocations);
        }
    }

    private InvalidDirectiveLocationException getInvalidDirectiveLocationException(GraphQLDirectiveContainer element, DirectiveInfo directiveInfo, Introspection.DirectiveLocation... validLocations) {
        return new InvalidDirectiveLocationException("The element: '" + element.getName() + "' is annotated with the directive: '"
                + directiveInfo.getName() + "' which is not valid on the element location: '" + Arrays.toString(Arrays.stream(validLocations).map(Enum::name).toArray()) + "'", null);
    }

}
