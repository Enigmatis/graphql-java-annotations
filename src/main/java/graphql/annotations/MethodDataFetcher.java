package graphql.annotations;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class MethodDataFetcher implements DataFetcher {
    private final Method method;
    private final int envIndex;

    public MethodDataFetcher(Method method) {
        this.method = method;
        List<Class<?>> parameterTypes = Arrays.asList(method.getParameters()).stream().
                map(Parameter::getType).
                collect(Collectors.toList());
        envIndex = parameterTypes.indexOf(DataFetchingEnvironment.class);
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        if (environment.getSource() == null) return null;
        try {
            ArrayList args = new ArrayList<>(environment.getArguments().values());
            if (envIndex >= 0) {
                args.add(envIndex, environment);
            }
            return method.invoke(environment.getSource(), args.toArray());
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }
}
