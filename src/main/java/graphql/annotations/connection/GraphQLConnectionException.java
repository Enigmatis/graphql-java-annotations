package graphql.annotations.connection;

class GraphQLConnectionException extends RuntimeException {

    GraphQLConnectionException(String error) {
        super(error);
    }
}
