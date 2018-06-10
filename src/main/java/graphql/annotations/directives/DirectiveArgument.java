package graphql.annotations.directives;

public class DirectiveArgument {
    private String name;
    private String defaultValue;
    private String description;
    private Class<?> type;

    public DirectiveArgument(String name, Class<?> type, String defaultValue, String description) {
        assert (name != null);
        assert (type != null);
        this.name = name;
        this.defaultValue = defaultValue;
        this.description = description;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public Class<?> getType() {
        return type;
    }
}
