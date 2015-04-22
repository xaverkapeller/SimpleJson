# SimpleJson

A simple and lightning fast JSON Parser for Android without any runtime overhead!

* **No slow reflection, full performance**: SimpleJson uses compile time annotation processing to generate performant and efficient implementations of models and parsers. No performance hit due to reflection at runtime.
* **Most errors caught at compile time**: SimpleJson will check for most common errors at compile time and give you useful and detailed error messages.
* **Generates real debuggable code**: You can view the model and parser implementations at any time and debug any error and behaviour. No more guessing what went wrong.
* **Easy to use and quick to setup**: Getting SimpleJson to work requires no setup and after adding only a few annotations on your models you are good to go.

# Table of Contents

* [Basic Usage](#basic-usage)
* [Installation](#installation)
* [Mapping Enums](#mapping-enums)
* [Collections and Child Entities](#collections-and-child-entities)
* [Performance Considerations](#performance-considerations)
* [Planned Features](#planned-features)

# Basic Usage

SimpleJson works exclusively with interfaces. It generates an implementation of this interface for you as well as the parser which translates the JSON. To get started just annotate your interface with `@JsonEntity` and use `@Key` to tell SimpleJson how to map elements in the JSON to the getters.

```java
@JsonEntity
public interface ExampleModel {
    
  @Key("id")
  public long getId();

  @Key("text")
  public String getText();
}
```

A JSON that corrosponds to the above interface would look something like this:

```json
{
  "text": "some example text",
  "id": 27
}
```

You can parse a JSON into an `ExampleModel` or the other way around like this:

```java
ExampleModel model = SimpleJson.fromJson(ExampleModel.class, json);
...
String json = SimpleJson.toJson(ExampleModel.class, model);
```

If you have an array of JSON objects like this:

```json
[
  {
    "text": "qwerty",
    "id": 27
  },
  {
    "text": "asdfdsa",
    "id": 37
  },
  {
    "text": "hello world",
    "id": 47
  }
]
```

Then you can parse that JSON by calling `fromJsonArray()`:

```java
List<ExampleModel> models = SimpleJson.fromJsonArray(ExampleModel.class, json);
```

You can of course also translate a `List` of `ExampleModel`s into a JSON like this:

```java
List<ExampleModel> models = ...;
String json = SimpleJson.toJson(ExampleModel.class, models);
```

If there is an optional element in a JSON you want to parse just annotate the corrosponding getter with `@Optional`. If the element is missing from the JSON then it will be parsed as `null`. If an element is not annotated with `@Optional` and it is missing from the JSON than a `JSONException` will be thrown! 

**Note:** Methods annotated with `@Optional` cannot return primitive values! Use boxed values instead.

# Installation

1) Just download this library and add the two modules SimpleJson and SimpleJsonCompiler to your Android project.

2) The top of the build.gradle file of your app needs to look like this:

```
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.+'
    }
}

apply plugin: 'com.android.application'
apply plugin: 'android-apt'
...
```

3) In the dependencies add these two lines at the bottom:

```
apt project(':SimpleJsonCompiler')
compile project(':SimpleJson')
```

And that is it! Now just sync your gradle files and begin annotating your models.

# Mapping Enums

SimpleJson can map Enums from and to JSON directly for you! To enable mapping on an enum just to add the `@JsonEnum` annotation. Currently you can map enum constants to strings or to integers. SimpleJson provides two annotations called `@MapString` and `@MapInt` to define those mappings. You can also use `@MapDefault` to define default mapping values if no other mapping applies. If no default value is defined with `@MapDefault` then a `JSONException` will be thrown if no other mapping can be applied.

```java
@JsonEnum
public enum ExampleEnum {

  @MapString("a")
  VALUE_A,
    
  @MapString("b")
  VALUE_B,
    
  @MapString("c")
  VALUE_C
}
```

By annotating the enum like above `VALUE_A` will be mapped to the String `"a"` in the JSON, `VALUE_B` will be mapped to `"b"` and so on. If you parse a JSON and the String `"c"` is encountered in an element which should be parsed as `ExampleEnum` then it will be mapped to `VALUE_C`, if `"b"` is encountered it will be mapped to `VALUE_B` and so on. If a string is encountered which does not match any mapping then a `JSONException` will be thrown. 

**Note:** You cannot mix `@MapString` and `@MapInt` with each other! You need to either exclusively use `@MapString` or `@MapInt` for the whole enum! Also you currently have to annotate every enum constant. A non strict mode will be added in future versions.

# Collections and Child Entities

You can also work with complex and models! Consider some like this:

```java
@JsonEntity
public interface Parent {

  @Key("types")
  public List<Type> getTypes();
    
  @Key("children")
  public Set<Child> getChildren();
}

@JsonEntity
public interface Child {

  @Key("text")
  public String getText();
  
  @Key("enabled")
  public boolean isEnabled();
  
  @Key("value")
  public double getValue();
}

@JsonEnum
public enum Type {
  @MapInt(0) A,
  @MapInt(1) B,
  @MapInt(2) C
  @MapDefault D
}
```

Collections like `List` or `Set` are represented as array in the JSON. Child entities will be parsed recursively and if a `Parent` would be translated to JSON result would look something like this:

```json
{
  "types": [0, 1],
  "children": [
    {
      "text": "some text",
      "enabled": 1,
      "value": 23.7
    },
    {
      "text": "some other text",
      "enabled": 0,
      "value": 0.0
    },
    {
      "text": "example",
      "enabled": 1,
      "value": 1234.5
    }
  ]
}
```

# Performance Considerations

SimpleJson is optimized to be as fast and efficient as possible but there are still some optimizations which can be implemented by you to get the most out of it!

SimpleJson internally uses a generated implementation of the `Parser` interface. For various reasons the implementations are **not** cached by SimpleJson. So each time you call a static method like `toJson` or `fromJson` internally a new instance of the appropriate `Parser` interface is created. If you perform many such operations in a sequence the overhead of constantly creating new `Parser` instances can be felt (compiler optimizations not withstanding).

For that reason SimpleJson provides another static method called `getParser()`. With it you can hold on to the `Parser` instance for as long as you want and use it to translate your JSONs just like you would with `SimpleJson`. For example:

```java
final Parser<ExampleModel> parser = SimpleJson.getParser(ExampleModel.class);
...
ExampleModel model = parser.fromJson(json);
...
String json = parser.toJson(model);
...
List<ExampleModel> models = parser.fromJsonArray(json);
```

Note that when using the `Parser` instance directly you do not need to specify the class of the model anymore!

# Planned Features

 - [x] Child Entities
 - [x] Support for `@Optional` annotation to define optional elements.
 - [ ] Better Code Generation
 - [ ] Option to enable/disable strict mode when parsing or mapping
