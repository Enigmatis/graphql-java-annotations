/**
 * Copyright 2016 Yurii Rashkovskii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations.directives.creation;

import graphql.AssertException;
import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.directives.CommonPropertiesCreator;
import graphql.annotations.processor.directives.DirectiveArgumentCreator;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.schema.GraphQLArgument;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

public class DirectiveArgumentCreatorTest {
    @GraphQLDescription("isActive")
    private boolean isActive = true;
    private DirectiveArgumentCreator directiveArgumentCreator;

    private TypeFunction typeFunction;
    private ProcessingElementsContainer container;

    @Retention(RetentionPolicy.RUNTIME)
    @interface SampleAnnotation {
        String suffix() default "bla";
    }

    @interface SampleAnnotationWithGraphQLData {
        @GraphQLName("suffix1")
        @GraphQLDescription("bla")
        String suffix();
    }

    static class SampleAnnotationVoidMethod{
        public void suffix(){}
    }

    static class Container{
        public void methodWithParamsNoGraphQLData(String param1){}
        public void methodWithParamsGraphQLData(@GraphQLName("suffix") @GraphQLDescription("the suffix") String param1){}
    }


    @BeforeMethod
    public void setUp() throws NoSuchFieldException {
        CommonPropertiesCreator commonPropertiesCreator = Mockito.mock(CommonPropertiesCreator.class);
        typeFunction = Mockito.mock(TypeFunction.class);
        Field field = this.getClass().getDeclaredField("isActive");
        container = Mockito.mock(ProcessingElementsContainer.class);
        when(typeFunction.buildType(same(true), same(boolean.class), any(), same(container))).thenReturn(GraphQLBoolean);
        when(typeFunction.buildType(same(true), same(String.class), any(), same(container))).thenReturn(GraphQLString);
        when(commonPropertiesCreator.getName(any())).thenCallRealMethod();
        when(commonPropertiesCreator.getDescription(any())).thenCallRealMethod();
        directiveArgumentCreator = new DirectiveArgumentCreator(commonPropertiesCreator, typeFunction, container);
    }

    @Test
    public void getArgument_goodFieldSupplied_correctArgumentCreated() throws NoSuchFieldException {
        GraphQLArgument isActive = directiveArgumentCreator.getArgument(this.getClass().getDeclaredField("isActive"), DirectiveArgumentCreatorTest.class);
        // Assert
        assertEquals(isActive.getName(), "isActive");
        assertEquals(isActive.getDefaultValue(), true);
        assertEquals(isActive.getDescription(), "isActive");
        assertEquals(isActive.getType(), GraphQLBoolean);
    }

    // method

    @Test
    public void getArgument_goodMethodSuppliedWithoutGraphQLName_correctArgumentCreated() throws NoSuchMethodException {
        // Act
        GraphQLArgument argument = directiveArgumentCreator.getArgument(SampleAnnotation.class.getMethod("suffix"));

        // Assert
        assertEquals(argument.getName(), "suffix");
        assertEquals(argument.getDefaultValue(), "bla");
        assertEquals(argument.getDescription(), null);
        assertEquals(argument.getType(), GraphQLString);
    }

    @Test
    public void getArgument_goodMethodSuppliedWithGraphQLData_correctArgumentCreated() throws NoSuchMethodException {
        // Act
        GraphQLArgument argument = directiveArgumentCreator.getArgument(SampleAnnotationWithGraphQLData.class.getMethod("suffix"));

        // Assert
        assertEquals(argument.getName(), "suffix1");
        assertEquals(argument.getDefaultValue(), null);
        assertEquals(argument.getDescription(), "bla");
        assertEquals(argument.getType(), GraphQLString);
    }

    @Test(expectedExceptions = AssertException.class)
    public void getArgument_voidMethod_throwException() throws NoSuchMethodException {
        // Act
        GraphQLArgument argument = directiveArgumentCreator.getArgument(SampleAnnotationVoidMethod.class.getMethod("suffix"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void getArgument_nullMethod_throwException(){
        // Act
        GraphQLArgument argument = directiveArgumentCreator.getArgument((Method) null);
    }


    // parameter

    @Test
    public void getArgument_goodParameterSuppliedWithoutGraphQLName_correctArgumentCreated() throws NoSuchMethodException {
        // Act
        GraphQLArgument argument = directiveArgumentCreator.getArgument(Container.class.getMethod("methodWithParamsNoGraphQLData", String.class).getParameters()[0]);

        // Assert
        assertEquals(argument.getName(), "arg0");
        assertEquals(argument.getDefaultValue(), null);
        assertEquals(argument.getDescription(), null);
        assertEquals(argument.getType(), GraphQLString);
    }

    @Test
    public void getArgument_goodParameterSuppliedWithGraphQLData_correctArgumentCreated() throws NoSuchMethodException {
        // Act
        GraphQLArgument argument = directiveArgumentCreator.getArgument(Container.class.getMethod("methodWithParamsGraphQLData", String.class).getParameters()[0]);

        // Assert
        assertEquals(argument.getName(), "suffix");
        assertEquals(argument.getDefaultValue(), null);
        assertEquals(argument.getDescription(), "the suffix");
        assertEquals(argument.getType(), GraphQLString);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void getArgument_nullParam_throwException(){
        // Act
        GraphQLArgument argument = directiveArgumentCreator.getArgument((Parameter) null);
    }
}
