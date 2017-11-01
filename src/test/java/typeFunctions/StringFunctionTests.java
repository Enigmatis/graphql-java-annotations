package typeFunctions;

import graphql.annotations.GraphQLAnnotations;
import graphql.annotations.typeFunctions.DefaultTypeFunction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static graphql.Scalars.GraphQLString;
import static org.testng.Assert.assertEquals;
import static typeFunctions.DefaultTypeFunctionTestHelper.testedDefaultTypeFunction;

/**
 * Created by Yael on 25/10/17.
 */
public class StringFunctionTests {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    @Test
    public void buildType_stringType_returnsGraphQLString() {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        assertEquals(instance.buildType(String.class, null,null), GraphQLString);
    }


}
