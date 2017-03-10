[![Build Status](https://travis-ci.org/graphql-java/graphql-java-annotations.svg?branch=master)](https://travis-ci.org/yrashk/graphql-java-annotations)
[![Maven Central](https://img.shields.io/maven-central/v/com.graphql-java/graphql-java-annotations.svg?maxAge=2592000)]()
# GraphQL Annotations for Java

[GraphQL-Java](https://github.com/andimarek/graphql-java) is a great library, but its syntax is a little bit verbose. This library offers an annotations-based
syntax for GraphQL schema definition.

## Getting Started


(Gradle syntax)

```groovy
dependencies {
  compile "com.graphql-java:graphql-java-annotations:0.13.1"
}
```


## Defining Objects

Any regular Java class can be converted to a GraphQL object type. Fields can
be defined with a `@GraphQLField` (see more on fields below) annotation:

```java
public class SomeObject {
  @GraphQLField
  public String field;
}

// ...
GraphQLObjectType object = GraphQLAnnotations.object(SomeObject.class);
```

## Defining Interfaces

This is very similar to defining objects:

```java
public interface SomeInterface {
  @GraphQLField
  String field();
}

// ...
GraphQLInterfaceType object = GraphQLAnnotations.iface(SomeInterface.class);
```

## Fields

In addition to specifying a field over a Java class field, a field can be defined over a method:

```java
public class SomeObject {
  @GraphQLField
  public String field() {
    return "field";
  }
}
```

Or a method with arguments:

```java
public class SomeObject {
  @GraphQLField
  public String field(String value) {
    return value;
  }
}
```
> Note: You need to use `-parameters` javac option to compile, which makes argument name as the default GraphQL name. Otherwise, you will need to add the `@GraphQLName("value")` annotation to specify one.

You can also inject `DataFetchingEnvironment` as an argument, at any position:

```java
public class SomeObject {
  @GraphQLField
  public String field(DataFetchingEnvironment env, String value) {
    return value;
  }
}
```

Additionally, `@GraphQLName` can be used to override field name. You can use `@GraphQLDescription` to set a description.

These can also be used for field parameters:

```java
public String field(@GraphQLName("val") String value) {
  return value;
}
```

In addition, `@GraphQLDefaultValue` can be used to set a default value to a parameter. Due to limitations of annotations, the default value has to be provided by a class that implements `Supplier<Object>`:

```java
public static class DefaultValue implements Supplier<Object> {
  @Override
  public Object get() {
    return "default";
  }
}

@GraphQLField
public String field(@GraphQLDefaultValue(DefaultValue.class) String value) {
  return value;
}
```

`@GraphQLDeprecate` and Java's `@Deprecated` can be used to specify a deprecated
field.

You can specify a custom data fetcher for a field with `@GraphQLDataFetcher`

## Type Inference

By default, standard GraphQL types (String, Integer, Long, Float, Boolean, Enum, List) will be inferred from Java types. Also, it will respect `@javax.validation.constraints.NotNull` annotation with respect to value's nullability, as well as `@GraphQLNonNull`

Stream type is also supported and treated as a list.

If you want to register an additional type (for example, UUID), you have to create a new class implementing `TypeFunction` for it:

```java
public class UUIDTypeFunction implements TypeFunction {
    ...
}
```

And register it with `GraphQLAnnotations`:

```java
GraphQLAnnotations.register(new UUIDTypeFunction())

// or if not using a static version of GraphQLAnnotations:
// new GraphQLAnnotations().registerType(new UUIDTypeFunction())
```

You can also specify custom type function for any field with `@GraphQLType` annotation.

## Relay Mutations

You can use `@GraphQLRelayMutation` annotation to make mutation adhere to
Relay [specification for mutations](https://facebook.github.io/relay/graphql/mutations.htm)

## Relay Connection

You can use `@GraphQLConnection` annotation to make a field iterable in adherence to Relay [Connection specification](https://facebook.github.io/relay/graphql/connections.htm).
