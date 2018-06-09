package graphql.annotations.directives;

import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;

public class InvalidDirectiveLocationException extends GraphQLAnnotationsException {
    public InvalidDirectiveLocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
