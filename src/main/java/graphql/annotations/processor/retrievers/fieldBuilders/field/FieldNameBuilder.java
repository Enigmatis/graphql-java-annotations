package graphql.annotations.processor.retrievers.fieldBuilders.field;

import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.retrievers.fieldBuilders.Builder;

import java.lang.reflect.Field;

import static graphql.annotations.processor.util.NamingKit.toGraphqlName;

public class FieldNameBuilder implements Builder<String> {
    private Field field;

    public FieldNameBuilder(Field field) {
        this.field = field;
    }

    @Override
    public String build() {
        GraphQLName name = field.getAnnotation(GraphQLName.class);
        return toGraphqlName(name == null ? field.getName() : name.value());
    }
}
