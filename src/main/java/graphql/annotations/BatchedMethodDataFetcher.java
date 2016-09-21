package graphql.annotations;

import graphql.execution.batched.Batched;
import graphql.schema.DataFetchingEnvironment;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class BatchedMethodDataFetcher extends MethodDataFetcher {
    public BatchedMethodDataFetcher(Method method) {
        super(method);
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Batched method should be static");
        }
    }

    @SneakyThrows
    @Batched
    @Override
    public Object get(DataFetchingEnvironment environment) {
        return super.get(environment);
    }
}
