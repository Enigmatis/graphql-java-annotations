package typeFunctions;


import graphql.annotations.GraphQLAnnotations;
import graphql.annotations.typeFunctions.DefaultTypeFunction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static graphql.Scalars.GraphQLLong;
import static org.testng.Assert.assertEquals;
import static typeFunctions.DefaultTypeFunctionTestHelper.testedDefaultTypeFunction;

public class LongFunctionTests {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    @Test
    public void buildType_longType_returnsGraphQLLong() {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        assertEquals(instance.buildType(long.class, null,null), GraphQLLong);
        assertEquals(instance.buildType(Long.class, null,null), GraphQLLong);
    }
}
