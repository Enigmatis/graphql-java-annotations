package graphql.annotations.processor.retrievers.fieldBuilders.method;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.processor.retrievers.fieldBuilders.Builder;

import java.lang.reflect.AccessibleObject;

public class DescriptionBuilder implements Builder<String> {
    private AccessibleObject object;

    public DescriptionBuilder(AccessibleObject method) {
        this.object = method;
    }

    @Override
    public String build() {
        GraphQLDescription description = object.getAnnotation(GraphQLDescription.class);
        return description==null? null : description.value();
    }
}