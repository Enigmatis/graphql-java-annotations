package graphql.annotations;

import graphql.schema.GraphQLList;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.util.List;

public class BatchedTypeFunction implements TypeFunction {
    private TypeFunction defaultTypeFunction;

    public BatchedTypeFunction(TypeFunction defaultTypeFunction) {
        this.defaultTypeFunction = defaultTypeFunction;
    }

    @Override
    public graphql.schema.GraphQLType apply(Class<?> aClass, AnnotatedType annotatedType) {
        if (!aClass.isAssignableFrom(List.class)) {
            throw new IllegalArgumentException("Batched method should return a List");
        }
        if (!(annotatedType instanceof AnnotatedParameterizedType)) {
            throw new IllegalArgumentException("Batched should return parameterized type");
        }
        AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType) annotatedType;
        AnnotatedType arg = parameterizedType.getAnnotatedActualTypeArguments()[0];
        Class<?> klass;
        if (arg.getType() instanceof ParameterizedType) {
            klass = (Class<?>)((ParameterizedType)(arg.getType())).getRawType();
        } else {
            klass = (Class<?>) arg.getType();
        }
        return defaultTypeFunction.apply(klass, arg);
    }
}
