package graphql.annotations.directives.creation;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.directives.CommonPropertiesCreator;
import graphql.annotations.processor.directives.DirectiveArgumentCreator;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.schema.GraphQLArgument;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import static graphql.Scalars.GraphQLBoolean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

public class DirectiveArgumentCreatorTest {
    @GraphQLDescription("isActive")
    private boolean isActive = true;
    private DirectiveArgumentCreator directiveArgumentCreator;

    private TypeFunction typeFunction;
    private ProcessingElementsContainer container;

    @BeforeMethod
    public void setUp() throws NoSuchFieldException {
        CommonPropertiesCreator commonPropertiesCreator = Mockito.mock(CommonPropertiesCreator.class);
        typeFunction = Mockito.mock(TypeFunction.class);
        Field field = this.getClass().getDeclaredField("isActive");
        container = Mockito.mock(ProcessingElementsContainer.class);
        when(typeFunction.buildType(same(true), same(boolean.class), any(), same(container))).thenReturn(GraphQLBoolean);
        when(commonPropertiesCreator.getName(any())).thenCallRealMethod();
        when(commonPropertiesCreator.getDescription(any())).thenCallRealMethod();
        directiveArgumentCreator = new DirectiveArgumentCreator(commonPropertiesCreator, typeFunction, container);
    }

    @Test
    public void getArgument_goodFieldSupplied_correctArgumentCreated() throws NoSuchFieldException {
        GraphQLArgument isActive = directiveArgumentCreator.getArgument(this.getClass().getDeclaredField("isActive"), DirectiveArgumentCreatorTest.class);
        // Assert
        assertEquals(isActive.getName(), "isActive");
        assertEquals(isActive.getDefaultValue(), true);
        assertEquals(isActive.getDescription(), "isActive");
        assertEquals(isActive.getType(), GraphQLBoolean);
    }
}
