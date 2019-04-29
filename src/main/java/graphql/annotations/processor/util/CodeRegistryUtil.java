package graphql.annotations.processor.util;

import graphql.annotations.directives.AnnotationsWiringEnvironment;
import graphql.schema.*;

import java.util.function.BiFunction;

import static graphql.schema.GraphQLObjectType.newObject;

public class CodeRegistryUtil {
    /**
     * This util method helps you to wrap your datafetcher with some lambda code
     * @param fieldDefinition The field you want to wrap its datafetcher
     * @param environment the environment object of the Wiring process
     * @param mapFunction the lambda expression to wrap with
     */
    public static void wrapDataFetcher(GraphQLFieldDefinition fieldDefinition, AnnotationsWiringEnvironment environment,
                                       BiFunction<DataFetchingEnvironment, Object, Object> mapFunction){
        DataFetcher originalDataFetcher = environment.getCodeRegistryBuilder()
                .getDataFetcher(newObject().name(environment.getParentName()).build(), fieldDefinition);
        DataFetcher wrappedDataFetcher = DataFetcherFactories.wrapDataFetcher(originalDataFetcher, mapFunction);
        environment.getCodeRegistryBuilder()
                .dataFetcher(FieldCoordinates.coordinates(environment.getParentName(), fieldDefinition.getName()), wrappedDataFetcher);
    }
}
