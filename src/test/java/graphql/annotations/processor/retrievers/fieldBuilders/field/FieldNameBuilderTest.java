package graphql.annotations.processor.retrievers.fieldBuilders.field;

import graphql.annotations.annotationTypes.GraphQLName;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import static org.testng.AssertJUnit.assertEquals;

public class FieldNameBuilderTest {

    public int test1;

    @GraphQLName("test1")
    public int test2;

    @Test
    public void build_fieldWithGraphQLNameAnnotation_returnAnnotationValue() throws NoSuchFieldException{
        // arrange
        Field field = getClass().getField("test2");
        FieldNameBuilder fieldNameBuilder = new FieldNameBuilder(field);

        // act
        String name = fieldNameBuilder.build();

        // assert
        assertEquals(name, "test1");
    }

    @Test
    public void build_fieldWithNoGraphQLNameAnnotation_returnFieldName() throws NoSuchFieldException{
        // arrange
        Field field = getClass().getField("test1");
        FieldNameBuilder fieldNameBuilder = new FieldNameBuilder(field);

        // act
        String name = fieldNameBuilder.build();

        // assert
        assertEquals(name, "test1");
    }
}
