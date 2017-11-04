package graphql.annotations.processor.retrievers.fieldBuilders;

import graphql.annotations.annotationTypes.GraphQLDeprecate;

import java.lang.reflect.AccessibleObject;

public class DeprecateBuilder implements Builder<String> {
    private AccessibleObject object;
    private final String DEFAULT_DEPRECATION_DESCRIPTION = "Deprecated";

    public DeprecateBuilder(AccessibleObject object) {
        this.object = object;
    }

    @Override
    public String build() {
        GraphQLDeprecate deprecate = object.getAnnotation(GraphQLDeprecate.class);
        if (deprecate != null) {
            return deprecate.value();
        }
        if (object.getAnnotation(Deprecated.class) != null) {
            return DEFAULT_DEPRECATION_DESCRIPTION;
        }
        return null;
    }
}
