package graphql.annotations;

import graphql.schema.GraphQLObjectType;
import lombok.Getter;

public class GraphQLObjectTypeWrapper extends GraphQLObjectType {

    @Getter
    private final Class<?> objectClass;

    public GraphQLObjectTypeWrapper(Class<?> objectClass, GraphQLObjectType objectType) {
        super(objectType.getName(), objectType.getDescription(), objectType.getFieldDefinitions(),
                objectType.getInterfaces());
        this.objectClass = objectClass;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GraphQLObjectType &&
               ((GraphQLObjectType) obj).getName().contentEquals(getName()) &&
               ((GraphQLObjectType) obj).getFieldDefinitions().equals(getFieldDefinitions());
    }
}
