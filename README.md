# kmap

[ ![Download](https://api.bintray.com/packages/michaelfoidl/kmap/kmap/images/download.svg) ](https://bintray.com/michaelfoidl/kmap/kmap/_latestVersion)
[![pipeline status](https://gitlab.com/michaelfoidl/kmap/badges/master/pipeline.svg)](https://gitlab.com/michaelfoidl/kmap/commits/master)


<b>kmap</b> is a simple object-to-object mapping library written in Kotlin. There only is limited logic to connect the source and target properties with each other, so most of these things have to be done by hand at the moment. The usage of submappers as well as constellations with circular references are supported.

## How it works

### Requirements for source and target types

Both, the source and the target type must have a parameterless primary constructor, although it can be marked as private. Therefore, <b>kmap</b> does not work with Kotlin's `data classes`.

### Defining maps

Each mapper has a collection of `MappingDefinitions` that connect source and target properties of the objects to be mapped with each other.

#### Converting Properties

The most common use case is converting a source property to a target property. The conversion might just be a simple cast or more complex, up to the usage of a submapper.

In the first scenario, where `id` and `string` are both properties of `SourceTestObject` and `TargetTestObject`, the `MappingDefintion` can be defined as following:

<code>
    MappingDefinition(SourceTestObject::class, TargetTestObject::class)
        .convert({ it::id }, { it::id })
        .convert({ it::string }, { it::string })
</code>

Since the source `id` and the target `id` are of the same type, a simple cast is enough.

However, when the target `string` should contain the value of the source `id`, you have to use a converter:

<code>

    MappingDefinition(SourceTestObject::class, TargetTestObject::class)
        .convert({ it::id },
                { it::string },
                { it!!.toString() })

</code>

Here, the third lambda expression takes the source value and returns the result as the target type.

For the case that the property to be mapped is of a complex type that has to be mapped by itself, you should use submappers:

<code>

    MappingDefinition(SourceTestObject::class, TargetTestObject::class)
        .convert({ it::complexObject },
                { it::complexObject },
                { complexObjectMapper })

</code>

`complexObjectMapper` is capable of mapping the source type of `complexObject` to the target type. It is itself defined using a `MappingDefinition`. By using this technique, you can also realize circular references.

#### Adding Properties

When your target type has a property that does not exist in the source type or if it is computed out of several source properties, you can add it:

<code>

    MappingDefinition(SourceTestObject::class, TargetTestObject::class)
        .add({ it::addedString },
                { "test" })

</code>

All mapped objects' `addedString` property will have the value "test". The second lambda expression holds the source object and returns any value that is of the target type.

#### Removing or Ignoring Properties

Removing or ignoring properties works pretty much the same as adding properties, however this time the source object is effected.

<code>

    MappingDefinition(SourceTestObject::class, TargetTestObject::class)
        .ignore({ it::ignoredString })

</code>

In the above example, the property `ignoredString` of the source object is ignored. Removing a property does also offer you the possibility to do anything else that might be necessary when doing so:

<code>

    MappingDefinition(SourceTestObject::class, TargetTestObject::class)
        .remove({ it::removedString },
        {
            // do something important
        })

</code>

Therefore, ignoring a property is just a special case of removing it without doing anything else.

#### Validation

In order to make sure that the mapping process will work smoothly, you should validate every `MappingDefinition` before using it (in a test):

<code>

    val result = mappingDefinition.validate(SourceTestObject::class, TargetTestObject::class)

</code>

The resulting `ValidationResult` can be successful, return a warning or an error. A warning suggests that for instance a source property is not converted, but also not ignored or removed or that a target property is not converted or added (but nullable).
If the above target property is not nullable, validation returns an error since mapping would cause a `NullPointerException`. The validation also fails when either the source type or the target type does not fulfil the requirements for being mappable.

### Mapping

When you have successfully defined a `Mapper` with a `MappingDefinition`, mapping is this easy:

<code>

    val result = mapper.map<TargetTestObject>(sourceObject)

</code>

## Bug Reporting

Whenever you find a bug or any other malfunctions, feel free to submit an issue. Please make sure to describe the circumstances as detailed as possible, so that fixing the bug becomes easier.

## Contributing

At the moment, <b>kmap</b> is at an early alpha stage and under heavy development. A lot of things are currently changing (also pretty basic stuff), so Pull Requests do not really make sense yet. Please be patient.