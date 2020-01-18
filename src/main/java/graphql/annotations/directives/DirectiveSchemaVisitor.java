package graphql.annotations.directives;

import graphql.introspection.Introspection;
import graphql.schema.*;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static graphql.util.TreeTransformerUtil.changeNode;

public class DirectiveSchemaVisitor implements GraphQLTypeVisitor {
    private HashMap<String, AnnotationsDirectiveWiring> directiveWiringMap;
    private GraphQLCodeRegistry.Builder codeRegistryBuilder;

    @FunctionalInterface
    interface WiringFunction {
        GraphQLDirectiveContainer apply(GraphQLDirective a, GraphQLDirectiveContainer b,
                                        AnnotationsDirectiveWiring wiring, GraphQLSchemaElement parentElement)
                throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;
    }

    private Map<Class, WiringFunction> functionMap;


    public DirectiveSchemaVisitor(HashMap<String, AnnotationsDirectiveWiring> directiveWiringMap, GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        this.directiveWiringMap = directiveWiringMap;
        this.functionMap = createFunctionsMap();
        this.codeRegistryBuilder = codeRegistryBuilder;
    }

    @Override
    public TraversalControl visitGraphQLArgument(GraphQLArgument node, TraverserContext<GraphQLSchemaElement> context) {
        return this.visitGraphQLType(GraphQLArgument.class, node, context);
    }


    @Override
    public TraversalControl visitGraphQLInterfaceType(GraphQLInterfaceType node, TraverserContext<GraphQLSchemaElement> context) {
        return this.visitGraphQLType(GraphQLInterfaceType.class, node, context);
    }

    @Override
    public TraversalControl visitGraphQLEnumType(GraphQLEnumType node, TraverserContext<GraphQLSchemaElement> context) {
        return this.visitGraphQLType(GraphQLEnumType.class, node, context);
    }

    @Override
    public TraversalControl visitGraphQLEnumValueDefinition(GraphQLEnumValueDefinition node, TraverserContext<GraphQLSchemaElement> context) {
        return this.visitGraphQLType(GraphQLEnumValueDefinition.class, node, context);
    }

    @Override
    public TraversalControl visitGraphQLFieldDefinition(GraphQLFieldDefinition node, TraverserContext<GraphQLSchemaElement> context) {
        return this.visitGraphQLType(GraphQLFieldDefinition.class, node, context);
    }

    @Override
    public TraversalControl visitGraphQLDirective(GraphQLDirective node, TraverserContext<GraphQLSchemaElement> context) {
        return TraversalControl.CONTINUE;
    }

    @Override
    public TraversalControl visitGraphQLInputObjectField(GraphQLInputObjectField node, TraverserContext<GraphQLSchemaElement> context) {
        return this.visitGraphQLType(GraphQLInputObjectField.class, node, context);
    }

    @Override
    public TraversalControl visitGraphQLInputObjectType(GraphQLInputObjectType node, TraverserContext<GraphQLSchemaElement> context) {
        return this.visitGraphQLType(GraphQLInputObjectType.class, node, context);
    }

    @Override
    public TraversalControl visitGraphQLList(GraphQLList node, TraverserContext<GraphQLSchemaElement> context) {
        return TraversalControl.CONTINUE;
    }

    @Override
    public TraversalControl visitGraphQLNonNull(GraphQLNonNull node, TraverserContext<GraphQLSchemaElement> context) {
        return TraversalControl.CONTINUE;
    }

    @Override
    public TraversalControl visitGraphQLObjectType(GraphQLObjectType node, TraverserContext<GraphQLSchemaElement> context) {
        return this.visitGraphQLType(GraphQLObjectType.class, node, context);
    }

    @Override
    public TraversalControl visitGraphQLScalarType(GraphQLScalarType node, TraverserContext<GraphQLSchemaElement> context) {
        return this.visitGraphQLType(GraphQLScalarType.class, node, context);
    }

    @Override
    public TraversalControl visitGraphQLTypeReference(GraphQLTypeReference node, TraverserContext<GraphQLSchemaElement> context) {
        return TraversalControl.CONTINUE;
    }

    @Override
    public TraversalControl visitGraphQLUnionType(GraphQLUnionType node, TraverserContext<GraphQLSchemaElement> context) {
        return this.visitGraphQLType(GraphQLUnionType.class, node, context);
    }

    private TraversalControl visitGraphQLType(Class<? extends GraphQLDirectiveContainer> typeOfContainer,
                                              GraphQLDirectiveContainer node, TraverserContext<GraphQLSchemaElement> context) {
        List<GraphQLDirective> directives = node.getDirectives();
        if (directives.size() == 0) {
            return TraversalControl.CONTINUE;
        }
        GraphQLDirectiveContainer newNode = node;
        for (GraphQLDirective directive : directives) {
            AnnotationsDirectiveWiring wiring = this.directiveWiringMap.get(directive.getName());
            if (wiring != null) {
                try {
                    GraphQLSchemaElement parentElement = context.getParentNode();
                    newNode = functionMap.get(typeOfContainer).apply(directive, newNode,
                            wiring, parentElement);
                } catch (Exception e) {
                    e.printStackTrace();
                    return TraversalControl.CONTINUE;
                }
            }
        }
        return changeNode(context, newNode);
    }

    private void putInMap(Map<Class, WiringFunction> map, Class clazz, String functionName,
                          Introspection.DirectiveLocation... locations) {
        map.put(clazz, (d, e, wiring, parentElement) -> {
            assertLocation(d, e, locations);
            AnnotationsWiringEnvironmentImpl environment =
                    new AnnotationsWiringEnvironmentImpl(e, e.getDirective(d.getName()), parentElement, codeRegistryBuilder);
            return (GraphQLDirectiveContainer) wiring.getClass().getMethod(functionName, AnnotationsWiringEnvironment.class)
                    .invoke(wiring, environment);
        });
    }

    private Map<Class, WiringFunction> createFunctionsMap() {
        Map<Class, WiringFunction> functionMap = new HashMap<>();
        putInMap(functionMap, GraphQLFieldDefinition.class, "onField", Introspection.DirectiveLocation.FIELD, Introspection.DirectiveLocation.FIELD_DEFINITION);
        putInMap(functionMap, GraphQLObjectType.class, "onObject", Introspection.DirectiveLocation.OBJECT);
        putInMap(functionMap, GraphQLArgument.class, "onArgument", Introspection.DirectiveLocation.ARGUMENT_DEFINITION);
        putInMap(functionMap, GraphQLInterfaceType.class, "onInterface", Introspection.DirectiveLocation.INTERFACE);
        putInMap(functionMap, GraphQLUnionType.class, "onUnion", Introspection.DirectiveLocation.UNION);
        putInMap(functionMap, GraphQLEnumType.class, "onEnum", Introspection.DirectiveLocation.ENUM);
        putInMap(functionMap, GraphQLEnumValueDefinition.class, "onEnumValue", Introspection.DirectiveLocation.ENUM_VALUE);
        putInMap(functionMap, GraphQLScalarType.class, "onScalar", Introspection.DirectiveLocation.SCALAR);
        putInMap(functionMap, GraphQLInputObjectType.class, "onInputObjectType", Introspection.DirectiveLocation.INPUT_OBJECT);
        putInMap(functionMap, GraphQLInputObjectField.class, "onInputObjectField", Introspection.DirectiveLocation.INPUT_FIELD_DEFINITION);

        return functionMap;
    }

    private void assertLocation(GraphQLDirective graphQLDirective, GraphQLDirectiveContainer element, Introspection.DirectiveLocation... validLocations) {
        boolean isSupported = false;
        for (Introspection.DirectiveLocation validLocation : validLocations) {
            if (graphQLDirective.validLocations().contains(validLocation)) {
                isSupported = true;
            }
        }
        if (!isSupported) {
            throw getInvalidDirectiveLocationException(element, graphQLDirective, validLocations);
        }
    }

    private InvalidDirectiveLocationException getInvalidDirectiveLocationException(GraphQLDirectiveContainer element, GraphQLDirective graphQLDirective, Introspection.DirectiveLocation... validLocations) {
        return new InvalidDirectiveLocationException("The element: '" + element.getName() + "' is annotated with the directive: '"
                + graphQLDirective.getName() + "' which is not valid on the element location: '" + Arrays.toString(Arrays.stream(validLocations).map(Enum::name).toArray()) + "'", null);
    }
}
