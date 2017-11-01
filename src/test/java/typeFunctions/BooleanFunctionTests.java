package typeFunctions;

import graphql.annotations.GraphQLAnnotations;
import graphql.annotations.typeFunctions.DefaultTypeFunction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static graphql.Scalars.GraphQLBoolean;
import static org.testng.Assert.assertEquals;
import static typeFunctions.DefaultTypeFunctionTestHelper.testedDefaultTypeFunction;

/**
 * Created by Yael on 25/10/17.
 */
public class BooleanFunctionTests {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    @Test
    public void buildType_booleanType_returnsGraphQLBoolean() {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        assertEquals(instance.buildType(boolean.class, null,null), GraphQLBoolean);
        assertEquals(instance.buildType(Boolean.class, null,null), GraphQLBoolean);
    }
}
