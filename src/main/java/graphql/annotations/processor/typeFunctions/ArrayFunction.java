package graphql.annotations.processor.typeFunctions;

import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLType;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;

/**
 * Support for arrays
 */
public class ArrayFunction implements TypeFunction {

    private DefaultTypeFunction defaultTypeFunction;

    public ArrayFunction(DefaultTypeFunction defaultTypeFunction) {
        this.defaultTypeFunction = defaultTypeFunction;
    }

    @Override
    public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
        return aClass.isArray();
    }

    @Override
    public GraphQLType buildType(boolean input, Class<?> aClass, AnnotatedType annotatedType, ProcessingElementsContainer container) {
        if (!(annotatedType instanceof AnnotatedArrayType)) {
            throw new IllegalArgumentException("Array type parameter should be specified");
        }
        AnnotatedArrayType parameterizedType = (AnnotatedArrayType) annotatedType;
        AnnotatedType arg = parameterizedType.getAnnotatedGenericComponentType();
        Class<?> klass;
        if (arg.getType() instanceof ParameterizedType) {
            klass = (Class<?>) ((ParameterizedType) (arg.getType())).getRawType();
        } else {
            klass = (Class<?>) arg.getType();
        }
        return new GraphQLList(defaultTypeFunction.buildType(input, klass, arg, container));
    }
}
