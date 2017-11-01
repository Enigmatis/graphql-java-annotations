package typeFunctions;

import graphql.annotations.GraphQLAnnotations;
import graphql.annotations.typeFunctions.DefaultTypeFunction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static graphql.Scalars.GraphQLInt;
import static org.testng.Assert.assertEquals;
import static typeFunctions.DefaultTypeFunctionTestHelper.testedDefaultTypeFunction;

/**
 * Created by Yael on 25/10/17.
 */
public class IntegerFunctionTests {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    @Test
    public void buildType_integerType_returnsGraphQLInt() {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        assertEquals(instance.buildType(int.class, null,null), GraphQLInt);
        assertEquals(instance.buildType(Integer.class, null,null), GraphQLInt);
    }
}
