package graphql.annotations.annotationTypes.directives.definition;

import graphql.annotations.directives.AnnotationsDirectiveWiring;
import graphql.introspection.Introspection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)

public @interface DirectiveWiring {
    Class<? extends AnnotationsDirectiveWiring> value();
}
