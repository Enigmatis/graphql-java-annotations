package typeFunctions;

import graphql.annotations.GraphQLAnnotations;
import graphql.annotations.graphQLProcessors.GraphQLInputProcessor;
import graphql.annotations.graphQLProcessors.GraphQLOutputProcessor;
import graphql.annotations.typeFunctions.DefaultTypeFunction;

public class DefaultTypeFunctionTestHelper {
    public static DefaultTypeFunction testedDefaultTypeFunction() {
        // wire up the ability
        GraphQLAnnotations graphQLAnnotations = new GraphQLAnnotations();
        DefaultTypeFunction defaultTypeFunction = new DefaultTypeFunction(new GraphQLInputProcessor(),new GraphQLOutputProcessor());
        defaultTypeFunction.setAnnotationsProcessor(graphQLAnnotations);
        return defaultTypeFunction;
    }
}
