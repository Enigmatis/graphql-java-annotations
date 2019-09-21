package graphql.annotations.processor.util;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.directives.definition.GraphQLDirectiveDefinition;
import graphql.annotations.directives.AnnotationsDirectiveWiring;
import org.testng.annotations.Test;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DirectiveJavaAnnotationUtilTest {

    @Test(expectedExceptions = NullPointerException.class)
    public void getDirectiveAnnotations_nullElement_throwException() {
        // Act
        DirectiveJavaAnnotationUtil.getDirectiveAnnotations(null);
    }

    static class Wiring implements AnnotationsDirectiveWiring {

    }

    @GraphQLDirectiveDefinition(wiring = Wiring.class)
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @GraphQLName("sample")
    @interface DirectiveAnnotationSample {

    }

    @GraphQLDirectiveDefinition(wiring = Wiring.class)
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface DirectiveAnnotationSample2 {

    }


    static class Container {

        @DirectiveAnnotationSample
        @DirectiveAnnotationSample2
        @GraphQLName("blabla")
        public void fieldAnnotatedWithMultipleAnnotationsIncludingDirective() {
        }

        @GraphQLName("bbb")
        @GraphQLDescription("asdfasdf")
        public void fieldAnnotatedWithMultipleAnnotationsWithoutDirective() {
        }

        public void fieldNotAnnotated() {
        }

    }

    @Test
    public void getDirectiveAnnotations_elementAnnotatedWithMultipleAnnotationsIncludingDirective_returnOnlyDirectivesAnnotations() throws NoSuchMethodException {
        // Arrange
        Method method = Container.class.getMethod("fieldAnnotatedWithMultipleAnnotationsIncludingDirective");
        // Act
        Stream<Annotation> directiveAnnotations = DirectiveJavaAnnotationUtil.getDirectiveAnnotations(method);
        Object[] objects = directiveAnnotations.toArray();

        // Assert
        assertThat(objects.length, is(2));
        assertThat((objects[0] instanceof DirectiveAnnotationSample), is(true));
        assertThat((objects[1] instanceof DirectiveAnnotationSample2), is(true));
    }

    @Test
    public void getDirectiveAnnotations_elementAnnotatedWithMultipleAnnotationsWithoutDirective_returnEmptyList() throws NoSuchMethodException {
        // Arrange
        Method method = Container.class.getMethod("fieldAnnotatedWithMultipleAnnotationsWithoutDirective");
        // Act
        Stream<Annotation> directiveAnnotations = DirectiveJavaAnnotationUtil.getDirectiveAnnotations(method);
        Object[] objects = directiveAnnotations.toArray();

        // Assert
        assertThat(objects.length, is(0));
    }

    @Test
    public void getDirectiveAnnotations_elementNotAnnotated_returnEmptyList() throws NoSuchMethodException {
        // Arrange
        Method method = Container.class.getMethod("fieldNotAnnotated");
        // Act
        Stream<Annotation> directiveAnnotations = DirectiveJavaAnnotationUtil.getDirectiveAnnotations(method);
        Object[] objects = directiveAnnotations.toArray();

        // Assert
        assertThat(objects.length, is(0));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void getName_nullElement_throwNullPointerException() {
        String name = DirectiveJavaAnnotationUtil.getName(null);
    }

    @Test
    public void getName_elementWithGraphQLName_returnGraphQLName() throws NoSuchMethodException {
        // Arrange
        DirectiveAnnotationSample annotation = Container.class.getMethod("fieldAnnotatedWithMultipleAnnotationsIncludingDirective").getAnnotation(DirectiveAnnotationSample.class);
        // Act
        String name = DirectiveJavaAnnotationUtil.getName(annotation);
        // Assert
        assertThat(name, is("sample"));
    }

    @Test
    public void getName_elementWithoutGraphQLName_returnAnnotationName() throws NoSuchMethodException {
        // Arrange
        DirectiveAnnotationSample2 annotation = Container.class.getMethod("fieldAnnotatedWithMultipleAnnotationsIncludingDirective").getAnnotation(DirectiveAnnotationSample2.class);
        // Act
        String name = DirectiveJavaAnnotationUtil.getName(annotation);
        // Assert
        assertThat(name, is("DirectiveAnnotationSample2"));
    }
}
