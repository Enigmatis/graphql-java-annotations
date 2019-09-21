package graphql.annotations.annotationTypes.directives.definition;

import graphql.annotations.directives.AnnotationsDirectiveWiring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * You should put this annotation on top of a directive method you created, or a directive java annotation you created
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphQLDirectiveDefinition {
    Class<? extends AnnotationsDirectiveWiring> wiring();
}
