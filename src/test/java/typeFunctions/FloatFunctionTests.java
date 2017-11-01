package typeFunctions;

import graphql.annotations.GraphQLAnnotations;
import graphql.annotations.typeFunctions.DefaultTypeFunction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static graphql.Scalars.GraphQLFloat;
import static org.testng.Assert.assertEquals;
import static typeFunctions.DefaultTypeFunctionTestHelper.testedDefaultTypeFunction;

/**
 * Created by Yael on 25/10/17.
 */
public class FloatFunctionTests {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    @Test
    public void buildType_floatType_returnsGraphQLFloat() {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        assertEquals(instance.buildType(float.class, null,null), GraphQLFloat);
        assertEquals(instance.buildType(Float.class, null,null), GraphQLFloat);
        assertEquals(instance.buildType(Double.class, null,null), GraphQLFloat);
        assertEquals(instance.buildType(double.class, null,null), GraphQLFloat);
    }
}
