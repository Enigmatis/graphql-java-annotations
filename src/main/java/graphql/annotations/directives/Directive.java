package graphql.annotations.directives;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Directive {
    //    Class<? extends BasicDirectiveInfo> info();
    String name();

    Class<? extends AnnotationsDirectiveWiring> wiringClass();

    String[] argumentsValues() default {};
}
