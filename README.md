[![Build Status](https://travis-ci.org/graphql-java/graphql-java-annotations.svg?branch=master)](https://travis-ci.org/graphql-java/graphql-java-annotations)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.graphql-java/graphql-java-annotations.svg?maxAge=3000)]()
# GraphQL Annotations for Java

[GraphQL-Java](https://github.com/andimarek/graphql-java) is a great library, but its syntax is a little bit verbose. This library offers an annotations-based
syntax for GraphQL schema definition.

ATTENTION THAT VER 6.0 WAS NOT RELEASED CORRECTLY AND WILL BE FIXED SOON

## Getting Started


(Gradle syntax)

```groovy
dependencies {
  compile "io.github.graphql-java:graphql-java-annotations:6.1"
}
```

(Maven syntax)

```groovy
<dependency>
    <groupId>io.github.graphql-java</groupId>
    <artifactId>graphql-java-annotations</artifactId>
    <version>6.1</version>
</dependency>
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

This is very similar to defining objects, with the addition of type resolver : 

```java
@GraphQLTypeResolver(MyTypeResolver.class)
public interface SomeInterface {
  @GraphQLField
  String field();
}

public class MyTypeResolver implements TypeResolver {
  GraphQLObjectType getType(TypeResolutionEnvironment env) { ... }
}

// ...
GraphQLInterfaceType object = GraphQLAnnotations.iface(SomeInterface.class);
```

An instance of the type resolver will be created from the specified class. If a `getInstance` method is present on the
class, it will be used instead of the default constructor.

## Defining Unions

To have a union, you must annotate an interface with `@GraphQLUnion`. In the annotation, you must declare all the 
possible types of the union, and a type resolver.
If no type resolver is specified, `UnionTypeResolver` is used. It follows this algorithm:
The resolver assumes the the DB entity's name is the same as  the API entity's name.
 If so, it takes the result from the dataFetcher and decides to which
API entity it should be mapped (according to the name). 
Example: If you have a `Pet` union type, and the dataFetcher returns `Dog`, the typeResolver
will check for each API entity if its name is equal to `Dog`, and returns if it finds something

```java
@GraphQLUnion(possibleTypes={Dog.class, Cat.class})
public interface Pet {}
``` 
and an example with custom `TypeResovler`:
```java
@GraphQLUnion(possibleTypes={DogApi.class, Cat.class}, typeResolver = PetTypeResolver.class)
public interface Pet {}


public class PetTypeResolver implements TypeResolver {
    @Override
    GraphQLObjectType getType(TypeResolutionEnvironment env) {
        Object obj = env.getObject();
        if(obj instanceof DogDB) {
            return (GraphQLObjectType) env.getSchema().getType("DogApi");
        }
        else {
            return (GraphQLObjectType) env.getSchema().getType("Cat");
        }
      
    }
}
```
NOTE: you can have (but not mandatory) a type resolver with constructor that has `Class<?>[]` as the first parameter and
`ProcessingElementsContainer` as the second. the `Class<?>[]` parameter contains the possibleTypes class
and `ProcessingElementsContainer` has all sorts of utils (you can check `UnionTypeResolver` to see how we use it there)

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

The `DefaultValue` class can define a `getInstance` method that will be called instead of the default constructor.

`@GraphQLDeprecate` and Java's `@Deprecated` can be used to specify a deprecated
field.

### Custom data fetcher

You can specify a custom data fetcher for a field with `@GraphQLDataFetcher`. The annotation will reference a class name, 
which will be used as data fetcher. 

An instance of the data fetcher will be created. The `args` attribute on the annotation can be used to specify a list of 
String arguments to pass to the constructor, allowing to reuse the same class on different fields, with different parameter.
The `firstArgIsTargetName` attribute can also be set on `@GraphQLDataFetcher` to pass the field name as a single parameter of the constructor.

Assuming you are using `@GraphQLDataFetcher` this way:

```java
@GraphQLField
@GraphQLDataFetcher(value = HelloWorldDataFetcher.class, args = { "arg1", "arg2" })
public String getHelloWorld(){
    return null;
}
```

Then the class that extends from `DataFetcher.class` will get this args to two supported constructors <br>
Or to a constructor that expecting String array that's way (`String[] args` or `String... args`) or for a constructor that expecting the same number of args that you send with in the annotation.<br>
You get to choose which implementation you want.
```java
public class HelloWorldDataFetcher implements DataFetcher<String> {

    public HelloWorldDataFetcher(String[] args){
        // Do something with your args
    }

    // Note that you need to expect the same number of args as you send with in the annotation args
    public HelloWorldDataFetcher(String arg1, String arg2){
        // Do something with your args
    }

    @Override
    public String get(DataFetchingEnvironment environment) {
        return "something";
    }
}
```



If no argument is needed and a `getInstance` method is present, this method will be called instead of the constructor.

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

Extensions are registered in GraphQLAnnotationProcessor by using `registerTypeExtension`. Note that extensions must be registered before the type itself is requested with `getObject()` :

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

## Directives
You can wire your fields using directives with annotations.
We allow both defining directives using annotations, and wiring fields.

### Creating/Defining a ``GraphQLDirective``
In order to create a directive, you first have to create a class that the directive will be created from.
For example:

```java
    @GraphQLName("upper")
    @GraphQLDescription("upper")
    @DirectiveLocations({Introspection.DirectiveLocation.FIELD_DEFINITION, Introspection.DirectiveLocation.INTERFACE})
    public static class UpperDirective {
        private boolean isActive = true;
    }
```

The name of the directive will be taken from the ``@GraphQLName`` annotation (if not specified, the name will be the class's name).
The description of the directive will be taken from the ``@GraphQLDescription`` annotation's value.
The valid locations of the directive (locations which the directive can be applied on) will be taken from ``@DirectiveLocations``.
The arguments of the directive will be taken from the fields defined in the class - notice that you can only use primitive types as arguments of a directive.
For example, we defined an ``isActive`` field - which is boolean, and its default value is true. That's how the argument of the directive will be defined.
You can also use ``@GraphQLName`` and ``@GraphQLDescription`` annotations on the field.

After you created the class, you will be able to create the ``GraphQLDirective`` object using the following code:
```java
GraphQLAnnotations.directive(UpperDirective.class);
```

### Wiring with directives
Using directives you will be able to wire fields and more, for example, changing the data fetchers of the fields.

In order to define a wiring functionality, you have to create a Wiring class matching one of you directives. For example:
```java
public class UpperWiring implements AnnotationsDirectiveWiring {
        @Override
        public GraphQLFieldDefinition onField(AnnotationsWiringEnvironment environment) {
            GraphQLFieldDefinition field = (GraphQLFieldDefinition) environment.getElement();
            boolean isActive = (boolean) environment.getDirective().getArgument("isActive").getValue();
            DataFetcher dataFetcher = DataFetcherFactories.wrapDataFetcher(field.getDataFetcher(), (((dataFetchingEnvironment, value) -> {
                if (value instanceof String && isActive) {
                    return ((String) value).toUpperCase();
                }
                return value;
            })));
            return field.transform(builder -> builder.dataFetcher(dataFetcher));
        }
    }
```
This class turns your string field to upper case if the directive argument "isActive" is set to true.
Now, you have to wire the field itself:
```java
@GraphQLField
@GraphQLDirectives(@Directive(name = "upperCase", wiringClass = UpperWiring.class, argumentsValues = {"true"}))
public static String name() {
    return "yarin";
}
```
We now wired the field "name" - so it will turn upper case when calling the field.
The ``Directive`` annotations requires the name of the directive, the wiring class (the ``UpperWiring`` class defined earlier), and the values of the arguments. If an argument has a default value, you don't have to supply a value in the arguments values.

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

It is also possible to specify for every connection which relay do you want to use, by giving a value to the annotation: 
`@GraphQLConnection(connectionType = customRelay.class)`. If you do that, please also give values to `connectionFetcher`
and `validator`.

There is also a support for simple paging, without "Nodes" and "Edges". To use it, annotate you connection like that:
`@GraphQLConnection(connectionFetcher = SimplePaginatedDataConnectionFetcher.class, connectionType = SimpleRelay.class, validator = SimplePaginatedDataConnectionTypeValidator.class)`
and the return type must be of type `SimplePaginatedData`.
It has 2 methods:
1. `getTotalCount` - how many elements are there in total
2. `getData` - get the data

For you convenience, there are two classes that you can use: `AbstractSimplePaginatedData` and `SimplePaginatedDataImpl`
For examples, look at the tests
