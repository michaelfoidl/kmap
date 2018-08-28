/*
 * kmap
 * version 0.1
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
 * A subtype of [MappingExpression] that supports converting a property of the source object to a property of the target object.
 * For converting between different types, you can use a simple lambda expression or a mapper to do so.
 *
 * @since 0.1
 * @constructor Creates a new [ConversionExpression] defined by a [sourcePropertyFunction] returning the source property,
 * a [targetPropertyFunction] returning the target property, a [conversionFunction] providing the conversion logic and a
 * [executionFunction] writing the results of the conversion process to the target object.
 */
class ConversionExpression<SourceT : Any, TargetT : Any, SourcePropertyT : Any?, TargetPropertyT : Any?>(
        private val sourcePropertyFunction: (SourceT) -> KProperty0<SourcePropertyT?>,
        private val targetPropertyFunction: (TargetT) -> KMutableProperty0<out TargetPropertyT?>,
        private val conversionFunction: (SourcePropertyT?) -> Initializable<TargetPropertyT?>,
        private val executionFunction: (Initializable<TargetPropertyT?>) -> Unit
) : MappingExpression<SourceT, TargetT>() {

    /**
     * @constructor Creates a new [ConversionExpression] defined by a [sourcePropertyFunction] returning the source property
     * and a [targetPropertyFunction] returning the target property. If the types of source property and target property
     * are not the same, the values are simply casted.
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
     * @constructor Creates a new [ConversionExpression] defined by a [sourcePropertyFunction] returning the source property,
     * a [targetPropertyFunction] returning the target property, a [mappingFunction] converting the type of the source
     * property to the type of the target property and a [defaultValueFunction] providing a value in case the source value
     * is `null`. If no [mappingFunction] is provided and the types of source property and target property are not the same,
     * the values are simply casted.
     */
    constructor(
            sourcePropertyFunction: (SourceT) -> KProperty0<SourcePropertyT?>,
            targetPropertyFunction: (TargetT) -> KMutableProperty0<out TargetPropertyT?>,
            mappingFunction: ((SourcePropertyT) -> TargetPropertyT?)? = null,
            defaultValueFunction: () -> TargetPropertyT? = { null }
    ) : this(
            sourcePropertyFunction,
            targetPropertyFunction,
            { sourceProperty: SourcePropertyT? ->
                if (mappingFunction == null) {
                    @Suppress("UNCHECKED_CAST")
                    Initializable(sourceProperty as TargetPropertyT)
                } else {
                    if (sourceProperty == null) {
                        Initializable<TargetPropertyT?>(defaultValueFunction())
                    } else {
                        Initializable<TargetPropertyT?>(mappingFunction(sourceProperty))
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