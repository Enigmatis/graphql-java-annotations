package graphql.annotations.directives;

import graphql.annotations.annotationTypes.GraphQLDirectives;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.schema.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DirectiveWirer {
    public GraphQLDirectiveContainer wire(GraphQLDirectiveContainer element, ProcessingElementsContainer container, DirectiveInfo... directiveInfos) {
        for (DirectiveInfo x : directiveInfos) {
            if (element instanceof GraphQLFieldDefinition) {
                element = x.getSchemaDirectiveWiring()
                        .onField(new AnnotationsWiringEnvironmentImpl<>((GraphQLFieldDefinition) element, x.toDirective()));
            } else if (element instanceof GraphQLObjectType) {
                element = x.getSchemaDirectiveWiring()
                        .onObject(new AnnotationsWiringEnvironmentImpl<>((GraphQLObjectType) element, x.toDirective()));
            } else if (element instanceof GraphQLArgument) {
                element = x.getSchemaDirectiveWiring()
                        .onArgument(new AnnotationsWiringEnvironmentImpl<>((GraphQLArgument) element, x.toDirective()));
            } else if (element instanceof GraphQLInterfaceType) {
                element = x.getSchemaDirectiveWiring()
                        .onInterface(new AnnotationsWiringEnvironmentImpl<>((GraphQLInterfaceType) element, x.toDirective()));
            } else if (element instanceof GraphQLUnionType) {
                element = x.getSchemaDirectiveWiring()
                        .onUnion(new AnnotationsWiringEnvironmentImpl<>((GraphQLUnionType) element, x.toDirective()));
            } else if (element instanceof GraphQLEnumType) {
                element = x.getSchemaDirectiveWiring()
                        .onEnum(new AnnotationsWiringEnvironmentImpl<>((GraphQLEnumType) element, x.toDirective()));
            } else if (element instanceof GraphQLEnumValueDefinition) {
                element = x.getSchemaDirectiveWiring()
                        .onEnumValue(new AnnotationsWiringEnvironmentImpl<>((GraphQLEnumValueDefinition) element, x.toDirective()));
            } else if (element instanceof GraphQLScalarType) {
                element = x.getSchemaDirectiveWiring()
                        .onScalar(new AnnotationsWiringEnvironmentImpl<>((GraphQLScalarType) element, x.toDirective()));
            } else if (element instanceof GraphQLInputObjectType) {
                element = x.getSchemaDirectiveWiring()
                        .onInputObjectType(new AnnotationsWiringEnvironmentImpl<>((GraphQLInputObjectType) element, x.toDirective()));
            } else if (element instanceof GraphQLInputObjectField) {
                element = x.getSchemaDirectiveWiring()
                        .onInputObjectField(new AnnotationsWiringEnvironmentImpl<>((GraphQLInputObjectField) element, x.toDirective()));
            }

        }
        return element;

    }


}
