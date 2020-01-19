package graphql.annotations.directives;

import graphql.introspection.Introspection;
import graphql.schema.*;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.assertEquals;

public class DirectiveSchemaVisitorTest {
    private DirectiveSchemaVisitor directiveSchemaVisitor;
    private GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private HashMap<String, AnnotationsDirectiveWiring> directiveWiringMap = new HashMap<>();
    private AnnotationsDirectiveWiring wiringMock;
    private TreeTransformerUtilWrapper transformerUtilWrapper;

    @BeforeMethod
    public void setUp() {
        codeRegistryBuilder = mock(GraphQLCodeRegistry.Builder.class);
        wiringMock = mock(AnnotationsDirectiveWiring.class);
        transformerUtilWrapper = mock(TreeTransformerUtilWrapper.class);
        directiveWiringMap.put("upper", wiringMock);
        directiveWiringMap.put("suffix", wiringMock);
        directiveWiringMap.put("noWiringMock", null);
        directiveSchemaVisitor = new DirectiveSchemaVisitor(directiveWiringMap, codeRegistryBuilder, transformerUtilWrapper);
    }


    @Test
    public void visitGraphQLArgument_hasDirectives_wiringFunctionIsCalledAndNodeChanged() {
        // Arrange
        GraphQLArgument argument = mock(GraphQLArgument.class);
        List<GraphQLDirective> directivesOnType = new ArrayList<>();
        GraphQLDirective directiveMock = mock(GraphQLDirective.class);
        when(directiveMock.validLocations()).thenReturn(EnumSet.of(Introspection.DirectiveLocation.ARGUMENT_DEFINITION));
        when(directiveMock.getName()).thenReturn("upper");
        directivesOnType.add(directiveMock);
        TraverserContext<GraphQLSchemaElement> context = mock(TraverserContext.class);
        when(argument.getDirectives()).thenReturn(directivesOnType);
        // Act
        directiveSchemaVisitor.visitGraphQLArgument(argument, context);

        // Assert
        verify(wiringMock).onArgument(any());
        verify(transformerUtilWrapper).changeNode(eq(context), any());
    }

    @Test
    public void visitGraphQLArgument_noDirectives_returnsContinue() {
        // Arrange
        GraphQLArgument argument = mock(GraphQLArgument.class);
        TraverserContext<GraphQLSchemaElement> context = mock(TraverserContext.class);
        when(argument.getDirectives()).thenReturn(new ArrayList<>());
        // Act
        TraversalControl traversalControl = directiveSchemaVisitor.visitGraphQLArgument(argument, context);

        // Assert
        verifyZeroInteractions(wiringMock);
        verifyZeroInteractions(transformerUtilWrapper);
        assertEquals(traversalControl, TraversalControl.CONTINUE);
    }

    @Test
    public void visitGraphQLArgument_noWiringFunction_changedToSameNode() {
        // Arrange
        GraphQLArgument argument = mock(GraphQLArgument.class);
        List<GraphQLDirective> directivesOnType = new ArrayList<>();
        GraphQLDirective directiveMock = mock(GraphQLDirective.class);
        when(directiveMock.validLocations()).thenReturn(EnumSet.of(Introspection.DirectiveLocation.ARGUMENT_DEFINITION));
        when(directiveMock.getName()).thenReturn("noWiringMock");
        directivesOnType.add(directiveMock);
        TraverserContext<GraphQLSchemaElement> context = mock(TraverserContext.class);
        when(argument.getDirectives()).thenReturn(directivesOnType);

        // Act
        directiveSchemaVisitor.visitGraphQLArgument(argument, context);

        // Assert
        verifyZeroInteractions(wiringMock);
        verify(transformerUtilWrapper).changeNode(eq(context), eq(argument));
    }

    ////

    @Test
    public void visitGraphQLFieldDefinition_hasDirectives_wiringFunctionIsCalledAndNodeChanged() {
        // Arrange
        GraphQLFieldDefinition type = mock(GraphQLFieldDefinition.class);
        List<GraphQLDirective> directivesOnType = new ArrayList<>();
        GraphQLDirective directiveMock = mock(GraphQLDirective.class);
        when(directiveMock.validLocations()).thenReturn(EnumSet.of(Introspection.DirectiveLocation.FIELD_DEFINITION));
        when(directiveMock.getName()).thenReturn("upper");
        directivesOnType.add(directiveMock);
        TraverserContext<GraphQLSchemaElement> context = mock(TraverserContext.class);
        when(type.getDirectives()).thenReturn(directivesOnType);
        // Act
        directiveSchemaVisitor.visitGraphQLFieldDefinition(type, context);

        // Assert
        verify(wiringMock).onField(any());
        verify(transformerUtilWrapper).changeNode(eq(context), any());
    }

    @Test
    public void visitGraphQLFieldDefinition_noDirectives_returnsContinue() {
        // Arrange
        GraphQLFieldDefinition type = mock(GraphQLFieldDefinition.class);
        TraverserContext<GraphQLSchemaElement> context = mock(TraverserContext.class);
        when(type.getDirectives()).thenReturn(new ArrayList<>());
        // Act
        TraversalControl traversalControl = directiveSchemaVisitor.visitGraphQLFieldDefinition(type, context);

        // Assert
        verifyZeroInteractions(wiringMock);
        verifyZeroInteractions(transformerUtilWrapper);
        assertEquals(traversalControl, TraversalControl.CONTINUE);
    }

    @Test
    public void visitGraphQLFieldDefinition_noWiringFunction_changedToSameNode() {
        // Arrange
        GraphQLFieldDefinition type = mock(GraphQLFieldDefinition.class);
        List<GraphQLDirective> directivesOnType = new ArrayList<>();
        GraphQLDirective directiveMock = mock(GraphQLDirective.class);
        when(directiveMock.validLocations()).thenReturn(EnumSet.of(Introspection.DirectiveLocation.FIELD_DEFINITION));
        when(directiveMock.getName()).thenReturn("noWiringMock");
        directivesOnType.add(directiveMock);
        TraverserContext<GraphQLSchemaElement> context = mock(TraverserContext.class);
        when(type.getDirectives()).thenReturn(directivesOnType);

        // Act
        directiveSchemaVisitor.visitGraphQLFieldDefinition(type, context);

        // Assert
        verifyZeroInteractions(wiringMock);
        verify(transformerUtilWrapper).changeNode(eq(context), eq(type));
    }

}
