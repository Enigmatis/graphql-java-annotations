package graphql.annotations.directives.creation;

import graphql.annotations.directives.AnnotationsDirectiveWiring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DirectiveAnnotation{
    Class<? extends AnnotationsDirectiveWiring> value();
}

