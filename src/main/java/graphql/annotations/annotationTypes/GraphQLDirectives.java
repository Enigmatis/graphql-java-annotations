package graphql.annotations.annotationTypes;

import graphql.annotations.directives.BasicDirectiveInfo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphQLDirectives {
    Class<? extends BasicDirectiveInfo>[] value();
}
