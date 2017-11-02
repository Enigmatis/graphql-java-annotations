package graphql.annotations.connection;

import java.lang.reflect.AccessibleObject;

public interface ConnectionValidator {

    void validate(AccessibleObject field);
}
