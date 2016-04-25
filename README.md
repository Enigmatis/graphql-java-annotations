[![Build Status](https://travis-ci.org/yrashk/graphql-java-annotations.svg?branch=master)](https://travis-ci.org/yrashk/graphql-java-annotations)
[ ![Download](https://api.bintray.com/packages/yrashk/maven/graphql-java-annotations/images/download.svg) ](https://bintray.com/yrashk/maven/graphql-java-annotations/_latestVersion)

# GraphQL Annotations for Java

[GraphQL-Java](https://github.com/andimarek/graphql-java) is a great library, but its syntax is a little bit verbose. This library offers an annotations-based
syntax for GraphQL schema definition.

## Getting Started


You can get packages from Bintray:

(Gradle syntax)

```groovy
repositories {
    jcenter()
}

dependencies {
  compile "graphql-java-annotations:graphql-java-annotations:${graphQLJavaAnnotationsVersion}"
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

In addition, `@GraphQLDefaultValue` can be used to set a default value to a parameter. Due to limitations of annotations, the default valueu has to be provided by a class that implements `Supplier<Object>`:

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

If you want to register an additional type (for example, UUID), you have to implement `TypeFunction` for it and register it with `DefaultTypeFunction`:

```java
DefaultTypeFunction.register(UUID.class, new UUIDTypeFunction());
```

You can also specify custom type function for any field with `@GraphQLType` annotation.

## Relay Mutations

You can use `@GraphQLRelayMutation` annotation to make mutation adhere to
Relay [specification for mutations](https://facebook.github.io/relay/graphql/mutations.htm)

## Relay Connection

You can use `@GraphQLCursor` annotation to make a field iterable in adherence to Relay Connection specification.
