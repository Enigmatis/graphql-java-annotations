[![Build Status](https://travis-ci.org/graphql-java/graphql-java-annotations.svg?branch=master)](https://travis-ci.org/graphql-java/graphql-java-annotations)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.graphql-java/graphql-java-annotations.svg?maxAge=2592000)]()
# GraphQL Annotations for Java

[GraphQL-Java](https://github.com/andimarek/graphql-java) is a great library, but its syntax is a little bit verbose. This library offers an annotations-based
syntax for GraphQL schema definition.

## Getting Started


(Gradle syntax)

```groovy
dependencies {
  compile "io.github.graphql-java:graphql-java-annotations:5.2"
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

## Type extensions

Having one single class declaring all fields in a graphQL object type is not always possible, or can lead to huge classes. 
Modularizing the schema by defining fields in different classes allows you to split it in smaller chunks of codes. 
In IDL, this is usually written by using the `extend` keyword on top of a type definition. So you have a type defined like this :

```
type Human {
    id: ID!
    name: String!
}
```

It would be possible to extend it later on by using the following syntax :

```
extend type Human {
    homePlanet: String
}
```

### Defining extensions in annotations

This is possible when using annotations by registering "extensions" classes, corresponding to `extend` clauses, before creating the objects with the GraphQLAnnotationsProcessor.
Extension classes are simple classes, using the same annotations, with an additional `@GraphQLTypeExtension` on the class itself. The annotation value is required and will be the class that it actually extends.

So the previous schema could be defined by the following classes : 
  
```
@GraphQLName("Human")
public class Human {
    @GraphQLField
    public String name() { }
}
```

```
@GraphQLTypeExtension(Human.class)
public class HumanExtension {
    @GraphQLField
    public String homePlanet() { }
}
```

Classes marked as "extensions" will actually not define a new type, but rather set new fields on the class it extends when it will be created. 
All GraphQL annotations can be used on extension classes.

Extensions are registered in GraqhQLAnnotationProcessor by using `registerTypeExtension`. Note that extensions must be registered before the type itself is requested with `getObject()` :

```
GraphQLAnnotationsProcessor processor = GraphQLAnnotations.getInstance(); 

// Register extensions
processor.registerTypeExtension(HumanExtension.class);

// Create type
GraphQLObjectType type = processor.getObject(Human.class);

```

### Data fetching with extensions

As opposed to standard annotated classes mapped to GraphQL types, no instance of the extensions are created by default. 
In DataFetcher, the source object will still be an instance of the extended class.
It is however possible to provide a constructor taking the extended class as parameter. This constructor will be used to create an instance of the extension class when a field with the default DataFetcher (without `@DataFetcher`) will be queried. 
If no such constructor is provided, the field must either be declared as `static` or marked as `@GraphQLInvokeDetached`. Original source object can be found in the `DataFetchingEnvironment`.

```
@GraphQLTypeExtension(Human.class)
public class HumanExtension {
    
    public HumanExtension(Human human) {
        this.human = human;   
    }
    
    @GraphQLField
    public String homePlanet() { 
        // get value somehow from human object
    }
}
```

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

## Relay support

### Mutations

You can use `@GraphQLRelayMutation` annotation to make mutation adhere to
Relay [specification for mutations](https://facebook.github.io/relay/graphql/mutations.htm)

### Connection

You can use `@GraphQLConnection` annotation to make a field iterable in adherence to Relay [Connection specification](https://facebook.github.io/relay/graphql/connections.htm).\
If a field is annotated with the annotation, the associated dataFetcher must return an instance of `PaginatedData`.\
The `PaginatedData` class holds the result of the connection:
1. The data of the page
2. Whether or not there is a next page and a previous page
3. A method that returns for each entity the encoded cursor of the entity (it returns string)

For you convenience, there is `AbstractPaginatedData` that can be extended.

If you want to use you own implementation of connection, that's fine, just give a value to connection().\
Please note that if you do so, you also have to specify your own connection validator that implements `ConnectionValidator`\
(and should throw `@GraphQLConnectionException` if something is wrong) 

NOTE: because `PropertyDataFetcher` and `FieldDataFetcher` can't handle connection, this annotation cant be used on a field that doesn't have a dataFetcher

### Customizing Relay schema

By default, GraphQLAnnotations will use the `graphql.relay.Relay` class to create the Relay specific schema types (Mutations, Connections, Edges, PageInfo, ...).
It is possible to set a custom implementation of the Relay class with `GraphQLAnnotations.setRelay` method. The class should inherit from `graphql.relay.Relay` and 
can redefine methods that create Relay types.
