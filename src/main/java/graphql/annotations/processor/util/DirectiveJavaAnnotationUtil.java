package graphql.annotations.processor.util;

import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.directives.creation.DirectiveAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.stream.Stream;

public class DirectiveJavaAnnotationUtil {

    public static Stream<Annotation> getDirectiveAnnotations(AnnotatedElement annotatedElement) {
        if (annotatedElement==null){
            throw new NullPointerException("supplied element is null");
        }
        return Arrays.stream(annotatedElement.getDeclaredAnnotations()).filter(annotation -> annotation.annotationType().isAnnotationPresent(DirectiveAnnotation.class));
    }

    public static String getName(Annotation annotation){
        if (annotation==null){
            throw new NullPointerException("supplied annotation is null");
        }
        if (annotation.annotationType().isAnnotationPresent(GraphQLName.class)) {
            return annotation.annotationType().getAnnotation(GraphQLName.class).value();
        } else {
            return annotation.annotationType().getSimpleName();
        }
    }
}
