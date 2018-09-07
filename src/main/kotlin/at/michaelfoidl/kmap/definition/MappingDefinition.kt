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

import at.michaelfoidl.kmap.initializable.Initializable
import at.michaelfoidl.kmap.mapper.Mapper
import at.michaelfoidl.kmap.validation.ValidationResult
import at.michaelfoidl.kmap.validation.Validator
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty0


/**
 * Defines how an object should be mapped. A [MappingDefinition] is created using a builder pattern adding one
 * expression after the other to a collection defining the mapping process.
 *
 * @since 0.1
 * @constructor Creates a new mapping configuration for mapping between the given source class and the given target
 * class.
 * @param sourceClass the class of the source object to be mapped.
 * @param targetClass the class of the target object to be mapped.
 */
class MappingDefinition<SourceT : Any, TargetT : Any>(
        private var sourceClass: KClass<SourceT>,
        private var targetClass: KClass<TargetT>
) {

    @PublishedApi
    internal val mappingExpressions: ArrayList<MappingExpression<SourceT, TargetT>> = ArrayList()

    /**
     * Adds a new expression to the list. It converts a source property to a target property. Optionally, a converter
     * function can convert between source type and target type if they are not the same. If no converter function is
     * provided, a simple cast is tried. If the source value equals `null`, the default function is executed in order to
     * provide a value.
     *
     * @param source a function returning the source property.
     * @param target a function returning the target property.
     * @param converter a function used for converting the source property type to the target property type.
     * @param default a function providing a value in case of the source value being `null`.
     * @return the updated instance.
     */
    fun <SourcePropertyT : Any, TargetPropertyT : Any> convert(
            source: (SourceT) -> KProperty0<SourcePropertyT?>,
            target: (TargetT) -> KMutableProperty0<out TargetPropertyT?>,
            converter: ((SourcePropertyT) -> TargetPropertyT?)? = null,
            default: (() -> TargetPropertyT?) = { null }
    ): MappingDefinition<SourceT, TargetT> {
        this.mappingExpressions.add(ConversionExpression(source, target, converter, default))
        return this
    }

    /**
     * Adds a new expression to the list. It converts a source property to a target property. A [Mapper] is used to
     * convert the source type to the target type. If the source value equals `null`, the target value will be `null` as
     * well.
     *
     * @param source a function returning the source property.
     * @param target a function returning the target property.
     * @param mapper a [Mapper] used for converting the source property type to the target property type.
     * @return the updated instance.
     */
    inline fun <reified SourcePropertyT : Any, reified TargetPropertyT : Any> map(
            noinline source: (SourceT) -> KProperty0<SourcePropertyT?>,
            noinline target: (TargetT) -> KMutableProperty0<out TargetPropertyT?>,
            mapper: Mapper
    ): MappingDefinition<SourceT, TargetT> {
        this.mappingExpressions.add(ConversionExpression(
                source,
                target,
                { sourceProperty: SourcePropertyT? ->
                    if (sourceProperty == null) {
                        Initializable(null)
                    } else {
                        mapper.mapperProvider.provideMapper<SourcePropertyT, TargetPropertyT>(this).convert(sourceProperty)
                    }
                },
                { fetchedValue: Initializable<TargetPropertyT?> ->
                    mapper.mapperProvider.provideMapper<SourcePropertyT, TargetPropertyT>(this).execute(fetchedValue)
                }))
        return this
    }

    /**
     * Adds a new expression to the list. It adds a property to the target object that has no equivalent at the source
     * object.
     *
     * @param target a function returning the target property.
     * @param targetValue a function providing a value the target property should be set to.
     * @return the updated instance.
     */
    fun <TargetPropertyT : Any> add(
            target: (TargetT) -> KMutableProperty0<out TargetPropertyT?>,
            targetValue: (SourceT) -> TargetPropertyT?
    ): MappingDefinition<SourceT, TargetT> {
        this.mappingExpressions.add(AdditionExpression(target, targetValue))
        return this
    }

    /**
     * Adds a new expression to the list. It excludes a property of the source object from mapping that has no
     * equivalent at the target object. Instead, any action that might be necessary to prevent loss of information since
     * the source property value is just dropped, can be defined and executed.
     *
     * @param source a function returning the source property.
     * @param action a function executing any operations that should take place instead of mapping the property.
     * @return the updated instance.
     */
    fun <SourcePropertyT : Any> remove(
            source: (SourceT) -> KProperty0<SourcePropertyT?>,
            action: (SourcePropertyT?) -> Unit
    ): MappingDefinition<SourceT, TargetT> {
        this.mappingExpressions.add(RemovalExpression(source, action))
        return this
    }

    /**
     * Adds a new expression to the list. It excludes a property of the source object from mapping that has no
     * equivalent at the target object. This is a special case of [remove] where no action takes place.
     *
     * @param source a function returning the source property.
     * @return the updated instance.
     */
    fun <SourcePropertyT : Any> ignore(
            source: (SourceT) -> KProperty0<SourcePropertyT?>
    ): MappingDefinition<SourceT, TargetT> {
        this.mappingExpressions.add(RemovalExpression(source) {})
        return this
    }

    /**
     * Checks if this [MappingDefinition] can be used for mapping between the [sourceClass] and the [targetClass].
     */
    internal fun doesApply(sourceClass: KClass<*>, targetClass: KClass<*>): Boolean {
        return sourceClass == this.sourceClass && targetClass == this.targetClass
    }

    /**
     * Checks, if the mapping configuration is valid for mapping between the given source class and the given target
     * class.
     *
     * @param sourceClass class of the source object the mapping configuration should be tested for.
     * @param targetClass class of the target object the mapping configuration should be tested for.
     * @return the result of the validation process.
     */
    fun validate(sourceClass: KClass<SourceT>, targetClass: KClass<TargetT>): ValidationResult {
        return Validator.validate(this, sourceClass, targetClass)
    }
}