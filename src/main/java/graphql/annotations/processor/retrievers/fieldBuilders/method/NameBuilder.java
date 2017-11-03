package graphql.annotations.processor.retrievers.fieldBuilders.method;

import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.retrievers.fieldBuilders.Builder;

import java.lang.reflect.Method;

import static graphql.annotations.processor.util.NamingKit.toGraphqlName;

public class NameBuilder implements Builder<String> {
    private Method method;

    public NameBuilder(Method method) {
        this.method = method;
    }

    @Override
    public String build() {
        String name = method.getName().replaceFirst("^(is|get|set)(.+)", "$2");
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        GraphQLName nameAnn = method.getAnnotation(GraphQLName.class);
        return toGraphqlName(nameAnn == null ? name : nameAnn.value());
    }
}
