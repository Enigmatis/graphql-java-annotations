package graphql.annotations.processor.retrievers.fieldBuilders.method;

import graphql.annotations.annotationTypes.GraphQLBatched;
import graphql.annotations.annotationTypes.GraphQLConnection;
import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.annotationTypes.GraphQLRelayMutation;
import graphql.annotations.dataFetchers.BatchedMethodDataFetcher;
import graphql.annotations.dataFetchers.MethodDataFetcher;
import graphql.annotations.dataFetchers.RelayMutationMethodDataFetcher;
import graphql.annotations.dataFetchers.connection.ConnectionDataFetcher;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.retrievers.fieldBuilders.Builder;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.annotations.processor.util.DataFetcherConstructor;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;

import java.lang.reflect.Method;
import java.util.List;

public class DataFetcherBuilder implements Builder<DataFetcher> {
    private Method method;
    private GraphQLOutputType outputType;
    private TypeFunction typeFunction;
    private ProcessingElementsContainer container;
    private GraphQLFieldDefinition relayFieldDefinition;
    private List<GraphQLArgument> args;
    private DataFetcherConstructor dataFetcherConstructor;
    private boolean isConnection;

    public DataFetcherBuilder(Method method, GraphQLOutputType outputType, TypeFunction typeFunction,
                              ProcessingElementsContainer container, GraphQLFieldDefinition relayFieldDefinition,
                              List<GraphQLArgument> args, DataFetcherConstructor dataFetcherConstructor, boolean isConnection) {
        this.method = method;
        this.outputType = outputType;
        this.typeFunction = typeFunction;
        this.container = container;
        this.relayFieldDefinition = relayFieldDefinition;
        this.args = args;
        this.dataFetcherConstructor = dataFetcherConstructor;
        this.isConnection = isConnection;
    }

    @Override
    public DataFetcher build() {
        GraphQLDataFetcher dataFetcher = method.getAnnotation(GraphQLDataFetcher.class);
        DataFetcher actualDataFetcher;
        if (dataFetcher == null && method.getAnnotation(GraphQLBatched.class) != null) {
            actualDataFetcher = new BatchedMethodDataFetcher(method, typeFunction, container);
        } else if (dataFetcher == null) {
            actualDataFetcher = new MethodDataFetcher(method, typeFunction, container);
        } else {
            actualDataFetcher = dataFetcherConstructor.constructDataFetcher(method.getName(), dataFetcher);
        }

        if (method.isAnnotationPresent(GraphQLRelayMutation.class) && relayFieldDefinition != null) {
            actualDataFetcher = new RelayMutationMethodDataFetcher(method, args, relayFieldDefinition.getArgument("input").getType(), relayFieldDefinition.getType());
        }

        if (isConnection){
            actualDataFetcher = new ConnectionDataFetcher(method.getAnnotation(GraphQLConnection.class).connection(), actualDataFetcher);
        }
        return actualDataFetcher;
    }
}
