package graphql.annotations.processor.util;

import graphql.annotations.directives.AnnotationsWiringEnvironment;
import graphql.schema.*;

import java.util.function.BiFunction;

import static graphql.schema.GraphQLObjectType.newObject;

public class CodeRegistryUtil {
    /**
     * This util method helps you to wrap your datafetcher with some lambda code
     *
     * @param fieldDefinition The field you want to wrap its datafetcher
     * @param environment     the environment object of the Wiring process
     * @param mapFunction     the lambda expression to wrap with
     */
    public static void wrapDataFetcher(GraphQLFieldDefinition fieldDefinition, AnnotationsWiringEnvironment environment,
                                       BiFunction<DataFetchingEnvironment, Object, Object> mapFunction) {
        DataFetcher originalDataFetcher = getDataFetcher(environment.getCodeRegistryBuilder(), environment.getParentName(), fieldDefinition);
        DataFetcher wrappedDataFetcher = DataFetcherFactories.wrapDataFetcher(originalDataFetcher, mapFunction);
        environment.getCodeRegistryBuilder()
                .dataFetcher(FieldCoordinates.coordinates(environment.getParentName(), fieldDefinition.getName()), wrappedDataFetcher);
    }

    /**
     * this util method helps you to retrieve the data fetcher from the code registry if you do not have the whole parent object (only parent name)
     *
     * @param codeRegistryBuilder the code registry builder
     * @param parentName          the parent name
     * @param fieldDefinition     the field definition which the data fetcher is linked to
     * @return the data fetcher
     */
    public static DataFetcher getDataFetcher(GraphQLCodeRegistry.Builder codeRegistryBuilder, String parentName, GraphQLFieldDefinition fieldDefinition) {
        return codeRegistryBuilder.getDataFetcher(newObject().name(parentName).build(), fieldDefinition);
    }
}