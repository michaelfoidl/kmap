/*
 * kmap
 * version 0.2
 *
 * Copyright (c) 2018, Michael Foidl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.michaelfoidl.kmap.definition

import at.michaelfoidl.kmap.ReflectionUtilities
import at.michaelfoidl.kmap.caching.MappingCache
import at.michaelfoidl.kmap.exceptions.MappingException
import at.michaelfoidl.kmap.initializable.Initializable
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0


/**
 * A subtype of [MappingExpression] that supports converting a property of the source object to the euqivalent property
 * of the target object. For converting between different types, you can use a simple lambda expression or a specific
 * mapper.
 *
 * @since 0.1
 * @constructor Creates a new [ConversionExpression] that can be added to a [MappingDefinition].
 * @property sourcePropertyFunction A function returning the source property to be converted.
 * @property targetPropertyFunction A function returning the target property to be converted to.
 * @property conversionFunction A function converting the value of the source property to the target property type and
 * returning a reference to where the result will be stored.
 * @property executionFunction A function writing the results of the conversion process to the target object.
 */
@PublishedApi
internal class ConversionExpression<SourceT : Any, TargetT : Any, SourcePropertyT : Any?, TargetPropertyT : Any?>(
        private val sourcePropertyFunction: (SourceT) -> KProperty0<SourcePropertyT?>,
        private val targetPropertyFunction: (TargetT) -> KMutableProperty0<out TargetPropertyT?>,
        private val conversionFunction: (SourcePropertyT?) -> Initializable<TargetPropertyT?>,
        private val executionFunction: (Initializable<TargetPropertyT?>) -> Unit
) : MappingExpression<SourceT, TargetT>() {

    /**
     * @constructor Creates a new [ConversionExpression] that can be added to a [MappingDefinition].
     * @param sourcePropertyFunction A function returning the source property to be converted.
     * @param targetPropertyFunction A function returning the target property to be converted to.
     */
    constructor(
            sourcePropertyFunction: (SourceT) -> KProperty0<SourcePropertyT?>,
            targetPropertyFunction: (TargetT) -> KMutableProperty0<out TargetPropertyT?>
    ) : this(
            sourcePropertyFunction,
            targetPropertyFunction,
            null
    )

    /**
     * @constructor Creates a new [ConversionExpression] that can be added to a [MappingDefinition].
     * @param sourcePropertyFunction A function returning the source property to be converted.
     * @param targetPropertyFunction A function returning the target property to be converted to.
     * @param converterFunction A function converting a value of the source property type to the target property type.
     * @param defaultValueFunction A function returning a value to be used in case of the source value being equal to
     * `null`.
     */
    constructor(
            sourcePropertyFunction: (SourceT) -> KProperty0<SourcePropertyT?>,
            targetPropertyFunction: (TargetT) -> KMutableProperty0<out TargetPropertyT?>,
            converterFunction: ((SourcePropertyT) -> TargetPropertyT?)? = null,
            defaultValueFunction: () -> TargetPropertyT? = { null }
    ) : this(
            sourcePropertyFunction,
            targetPropertyFunction,
            { sourceProperty: SourcePropertyT? ->
                if (converterFunction == null) {
                    @Suppress("UNCHECKED_CAST")
                    Initializable(sourceProperty as TargetPropertyT)
                } else {
                    if (sourceProperty == null) {
                        Initializable<TargetPropertyT?>(defaultValueFunction())
                    } else {
                        Initializable<TargetPropertyT?>(converterFunction(sourceProperty))
                    }
                }
            },
            { _: Initializable<TargetPropertyT?> -> }
    )

    private lateinit var result: Initializable<TargetPropertyT?>
    private lateinit var sourcePropertyName: String
    private lateinit var sourceClassName: String

    override fun doConvert(source: SourceT, cache: MappingCache) {
        val sourceProperty = this.sourcePropertyFunction.invoke(source)

        ReflectionUtilities.ensureThatPropertyExists(source::class, sourceProperty)

        this.sourcePropertyName = sourceProperty.name
        this.sourceClassName = source::class.qualifiedName!!

        val value: SourcePropertyT? = sourceProperty.getter.call()
        this.result = this.conversionFunction(value)
    }

    override fun doExecute(target: TargetT) {
        val targetProperty = this.targetPropertyFunction.invoke(target)

        ReflectionUtilities.validateThatPropertyExists(target::class, targetProperty)

        this.executionFunction(this.result)
        try {
            targetProperty.setter.call(this.result.value)
        } catch (exception: Exception) {
            throw MappingException("Property '" + this.sourcePropertyName + "' of " + this.sourceClassName + " could not be mapped to property '" + targetProperty.name + "' of " + target::class.qualifiedName + " due to conversion issues.", exception)
        }
    }

    override fun mapsToProperty(elementClass: KClass<TargetT>, property: KProperty<*>): Boolean {
        val targetProperty = this.targetPropertyFunction.invoke(ReflectionUtilities.createNewInstance(elementClass))
        return property.name == targetProperty.name
    }

    override fun mapsFromProperty(elementClass: KClass<SourceT>, property: KProperty<*>): Boolean {
        val sourceProperty = this.sourcePropertyFunction.invoke(ReflectionUtilities.createNewInstance(elementClass))
        return property.name == sourceProperty.name
    }
}