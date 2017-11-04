package graphql.annotations.processor.retrievers.fieldBuilders;

import graphql.annotations.annotationTypes.GraphQLDeprecate;
import graphql.annotations.processor.retrievers.fieldBuilders.Builder;

import java.lang.reflect.AccessibleObject;

public class DeprecateBuilder implements Builder<String> {
    private AccessibleObject object;

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
            return "Deprecated";
        }
        return null;
    }
}
