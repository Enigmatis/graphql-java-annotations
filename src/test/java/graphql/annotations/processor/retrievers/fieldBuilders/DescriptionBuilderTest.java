package graphql.annotations.processor.retrievers.fieldBuilders;

import graphql.annotations.annotationTypes.GraphQLDescription;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

public class DescriptionBuilderTest {

    @GraphQLDescription("test description")
    public int testField;

    @GraphQLDescription("test description")
    public int testMethod(){return 1;}

    public int testField2;

    public int testsMethod2(){return 1;}

    @Test
    public void build_descriptionAnnotationExistsOnField_returnCorrectDescription() throws NoSuchFieldException {
        // arrange
        Field field = getClass().getField("testField");
        DescriptionBuilder descriptionBuilder = new DescriptionBuilder(field);

        // act
        String description = descriptionBuilder.build();

        // assert
        assertEquals(description, "test description");
    }

    @Test
    public void build_descriptionAnnotationExistsOnMethod_returnCorrectDescription() throws NoSuchMethodException {
        // arrange
        Method method = getClass().getMethod("testMethod");
        DescriptionBuilder descriptionBuilder = new DescriptionBuilder(method);

        // act
        String description = descriptionBuilder.build();

        // assert
        assertEquals(description, "test description");
    }

    @Test
    public void build_descriptionAnnotationNotExistsOnField_returnNull() throws NoSuchFieldException {
        // arrange
        Field field = getClass().getField("testField2");
        DescriptionBuilder descriptionBuilder = new DescriptionBuilder(field);

        // act
        String description = descriptionBuilder.build();

        // assert
        assertNull(description);
    }

    @Test
    public void build_descriptionAnnotationNotExistsOnMethod_returnNull() throws NoSuchMethodException {
        // arrange
        Method method = getClass().getMethod("testsMethod2");
        DescriptionBuilder descriptionBuilder = new DescriptionBuilder(method);

        // act
        String description = descriptionBuilder.build();

        // assert
        assertNull(description);
    }
}
