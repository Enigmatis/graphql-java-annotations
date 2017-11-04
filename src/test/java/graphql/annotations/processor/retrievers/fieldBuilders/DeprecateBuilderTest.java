package graphql.annotations.processor.retrievers.fieldBuilders;

import graphql.annotations.annotationTypes.GraphQLDeprecate;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

public class DeprecateBuilderTest {

    @GraphQLDeprecate("test deprecated")
    public int testMethod() {
        return 1;
    }

    @GraphQLDeprecate
    public int testMethod2() {
        return 1;
    }

    @Deprecated
    public int testMethod3() {
        return 1;
    }

    public int testMethod4() {
        return 1;
    }

    @Test
    public void build_graphQLDeprecateAnnotationExistsWithValue_returnAnnotationValue() throws NoSuchMethodException {
        // arrange
        Method method = getClass().getMethod("testMethod");
        DeprecateBuilder deprecateBuilder = new DeprecateBuilder(method);

        // act
        String deprecate = deprecateBuilder.build();

        // assert
        assertEquals(deprecate, "test deprecated");
    }


    @Test
    public void build_graphQLDeprecateAnnotationExistsWithNoValue_returnEmptyString() throws NoSuchMethodException {
        // arrange
        Method method = getClass().getMethod("testMethod2");
        DeprecateBuilder deprecateBuilder = new DeprecateBuilder(method);

        // act
        String deprecate = deprecateBuilder.build();

        // assert
        assertEquals(deprecate, "");
    }

    @Test
    public void build_deprecatedAnnotationExists_returnDefaultDeprecationComment() throws NoSuchMethodException {
        // arrange
        Method method = getClass().getMethod("testMethod3");
        DeprecateBuilder deprecatedBuilder = new DeprecateBuilder(method);

        // act
        String deprecate = deprecatedBuilder.build();

        // assert
        assertEquals(deprecate, "Deprecated");
    }

    @Test
    public void build_noDeprecatedAnnotation_returnNull() throws NoSuchMethodException {
        // arrange
        Method method = getClass().getMethod("testMethod4");
        DeprecateBuilder deprecateBuilder = new DeprecateBuilder(method);

        // act
        String deprecate = deprecateBuilder.build();

        // assert
        assertNull(deprecate);
    }
}
