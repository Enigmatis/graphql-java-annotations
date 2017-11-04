package graphql.annotations.processor.retrievers.fieldBuilders.method;

import graphql.annotations.annotationTypes.GraphQLName;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.testng.AssertJUnit.assertEquals;

public class MethodNameBuilderTest {

    @GraphQLName("testName")
    public void testMethod1() {
    }

    public void getTest() {
    }

    public void isTest(){
    }

    public void setTest(){

    }

    public void test(){

    }

    @Test
    public void build_graphQLNameAnnotationExists_returnAnnotatedName() throws NoSuchMethodException {
        // arrange
        Method method = getClass().getMethod("testMethod1");
        MethodNameBuilder methodNameBuilder = new MethodNameBuilder(method);

        // act
        String name = methodNameBuilder.build();

        // assert
        assertEquals(name, "testName");
    }

    @Test
    public void build_graphQLNameAnnotationNotExistsWithGetPrefix_returnCorrectName() throws NoSuchMethodException {
        // arrange
        Method method = getClass().getMethod("getTest");
        MethodNameBuilder methodNameBuilder = new MethodNameBuilder(method);

        // act
        String name = methodNameBuilder.build();

        // assert
        assertEquals(name, "test");
    }

    @Test
    public void build_graphQLNameAnnotationNotExistsWithIsPrefix_returnCorrectName() throws NoSuchMethodException{
        // arrange
        Method method = getClass().getMethod("isTest");
        MethodNameBuilder methodNameBuilder = new MethodNameBuilder(method);

        // act
        String name = methodNameBuilder.build();

        // assert
        assertEquals(name, "test");
    }

    @Test
    public void build_graphQLNameAnnotationNotExistsWithSetPrefix_returnCorrectName() throws NoSuchMethodException{
        // arrange
        Method method = getClass().getMethod("setTest");
        MethodNameBuilder methodNameBuilder = new MethodNameBuilder(method);

        // act
        String name = methodNameBuilder.build();

        // assert
        assertEquals(name, "test");
    }


    @Test
    public void build_graphQLNameAnnotationNotExistsNoPrefix_returnCorrectName() throws NoSuchMethodException{
        // arrange
        Method method = getClass().getMethod("test");
        MethodNameBuilder methodNameBuilder = new MethodNameBuilder(method);

        // act
        String name = methodNameBuilder.build();

        // assert
        assertEquals(name, "test");
    }

}
