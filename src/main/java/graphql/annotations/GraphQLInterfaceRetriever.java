package graphql.annotations;

import graphql.annotations.util.GraphQLOutputObjectRetriever;

public class GraphQLInterfaceRetriever {

    private  GraphQLOutputObjectRetriever graphQLOutputObjectRetriever;

    public GraphQLInterfaceRetriever(GraphQLOutputObjectRetriever graphQLOutputObjectRetriever){
        this.graphQLOutputObjectRetriever=graphQLOutputObjectRetriever;
    }

    public GraphQLInterfaceRetriever(){
        this(new GraphQLOutputObjectRetriever());
    }
    public graphql.schema.GraphQLOutputType getInterface(Class<?> iface, ProcessingElementsContainer container) throws GraphQLAnnotationsException {
        return graphQLOutputObjectRetriever.getOutputType(iface,container);
    }
}
