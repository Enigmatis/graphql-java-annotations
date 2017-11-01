package typeFunctions;

import graphql.annotations.GraphQLAnnotations;
import graphql.annotations.GraphQLID;
import graphql.annotations.graphQLProcessors.GraphQLInputProcessor;
import graphql.annotations.graphQLProcessors.GraphQLOutputProcessor;
import graphql.annotations.typeFunctions.DefaultTypeFunction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static graphql.Scalars.GraphQLID;
import static org.testng.Assert.assertEquals;
import static typeFunctions.DefaultTypeFunctionTestHelper.testedDefaultTypeFunction;


public class IDFunctionTests {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    public @graphql.annotations.GraphQLID String idStringMethod() {
        return "asd";
    }

    public @GraphQLID Integer idIntegerMethod() {
        return 5;
    }

    public @GraphQLID int idIntMethod() {
        return 5;
    }

    public @GraphQLID String idStringField;
    public @GraphQLID Integer idIntegerField;
    public @GraphQLID int idIntField;

    @Test
    public void buildType_stringMethodAnnotatedWithGraphQLID_returnsGraphQLID() throws NoSuchMethodException, NoSuchFieldException {
        // Arrange
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        Method idStringMethod = IDFunctionTests.class.getMethod("idStringMethod");

        // Act+Assert
        assertEquals(instance.buildType(idStringMethod.getReturnType(), idStringMethod.getAnnotatedReturnType(),null), GraphQLID);
    }

    @Test
    public void buildType_integerMethodAnnotatedWithGraphQLID_returnsGraphQLID() throws NoSuchMethodException {
        // Arrange
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        Method idIntegerMethod = IDFunctionTests.class.getMethod("idIntegerMethod");

        // Act+Assert
        assertEquals(instance.buildType(idIntegerMethod.getReturnType(), idIntegerMethod.getAnnotatedReturnType(),null), GraphQLID);
    }

    @Test
    public void buildType_intMethodAnnotatedWithGraphQLID_returnsGraphQLID() throws NoSuchMethodException {
        // Arrange
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        Method idIntMethod = IDFunctionTests.class.getMethod("idIntMethod");

        // Act+Assert
        assertEquals(instance.buildType(idIntMethod.getReturnType(), idIntMethod.getAnnotatedReturnType(),null), GraphQLID);
    }

    @Test
    public void buildType_stringFieldAnnotatedWithGraphQLID_returnsGraphQLID() throws NoSuchFieldException {
        // Arrange
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        Field idStringField = IDFunctionTests.class.getField("idStringField");

        // Act+Assert
        assertEquals(instance.buildType(idStringField.getType(), idStringField.getAnnotatedType(),null), GraphQLID);
    }

    @Test
    public void buildType_integerFieldAnnotatedWithGraphQLID_returnsGraphQLID() throws NoSuchFieldException {
        // Arrange
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        Field idIntegerField = IDFunctionTests.class.getField("idIntegerField");

        // Act+Assert
        assertEquals(instance.buildType(idIntegerField.getType(), idIntegerField.getAnnotatedType(),null), GraphQLID);
    }

    @Test
    public void buildType_intFieldAnnotatedWithGraphQLID_returnsGraphQLID() throws NoSuchFieldException {
        // Arrange
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        Field idIntField = IDFunctionTests.class.getField("idIntField");

        // Act+Assert
        assertEquals(instance.buildType(idIntField.getType(), idIntField.getAnnotatedType(),null), GraphQLID);
    }


}
