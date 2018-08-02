package graphql.annotations.processor.directives;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLName;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

public class CommonPropertiesCreator {
    public String getDescription(AnnotatedElement annotatedElement) {
        GraphQLDescription graphQLDescriptionAnnotation = annotatedElement.getAnnotation(GraphQLDescription.class);
        if (graphQLDescriptionAnnotation != null) {
            return graphQLDescriptionAnnotation.value();
        }
        return null;
    }

    public String getName(AnnotatedElement annotatedElement) {
        GraphQLName graphQLNameAnnotation = annotatedElement.getAnnotation(GraphQLName.class);
        if (graphQLNameAnnotation != null) {
            return graphQLNameAnnotation.value();
        }
        if (annotatedElement instanceof Class<?>) {
            return ((Class<?>) annotatedElement).getSimpleName();
        }
        else if (annotatedElement instanceof Field){
            return ((Field) annotatedElement).getName();
        }
        return null;
    }
}
