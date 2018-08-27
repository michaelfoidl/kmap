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
import at.michaelfoidl.kmap.initializable.Initializable
import at.michaelfoidl.kmap.mapper.Mapper
import at.michaelfoidl.kmap.validation.ValidationResult
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty0
import kotlin.reflect.full.memberProperties


class MappingDefinition<SourceT : Any, TargetT : Any>(
        private var sourceClass: KClass<SourceT>,
        private var targetClass: KClass<TargetT>
) {

    @PublishedApi
    internal val mappingExpressions: ArrayList<MappingExpression<SourceT, TargetT>> = ArrayList()

    fun <SourcePropertyT : Any, TargetPropertyT : Any> convert(
            source: (SourceT) -> KProperty0<SourcePropertyT?>,
            target: (TargetT) -> KMutableProperty0<out TargetPropertyT?>,
            converter: ((SourcePropertyT) -> TargetPropertyT?)? = null,
            default: (() -> TargetPropertyT?) = { null }
    ): MappingDefinition<SourceT, TargetT> {
        this.mappingExpressions.add(ConversionExpression(source, target, converter, default))
        return this
    }

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

    fun <TargetPropertyT : Any> add(
            target: (TargetT) -> KMutableProperty0<out TargetPropertyT?>,
            targetValue: (SourceT) -> TargetPropertyT?
    ): MappingDefinition<SourceT, TargetT> {
        this.mappingExpressions.add(AdditionExpression(target, targetValue))
        return this
    }

    fun <SourcePropertyT : Any> remove(
            source: (SourceT) -> KProperty0<SourcePropertyT?>,
            action: (SourcePropertyT?) -> Unit
    ): MappingDefinition<SourceT, TargetT> {
        this.mappingExpressions.add(RemovalExpression(source, action))
        return this
    }

    fun <SourcePropertyT : Any> ignore(
            source: (SourceT) -> KProperty0<SourcePropertyT?>
    ): MappingDefinition<SourceT, TargetT> {
        this.mappingExpressions.add(RemovalExpression(source) {})
        return this
    }


    // TODO autoMap (just maps the property to the target property with the same name

    fun doesApply(sourceClass: KClass<*>, targetClass: KClass<*>): Boolean {
        return sourceClass == this.sourceClass && targetClass == this.targetClass
    }

    // TODO move validation to extra class

    fun validate(sourceClass: KClass<SourceT>, targetClass: KClass<TargetT>): ValidationResult {

        val result = ValidationResult()

        val isSourceClassValid = ReflectionUtilities.validateThatEmptyPrimaryConstructorExists(sourceClass)
        if (!isSourceClassValid) {
            result.addError("No empty or optional-parameter-only primary constructor was found for class " + sourceClass.qualifiedName + "")
        }

        val isTargetClassValid = ReflectionUtilities.validateThatEmptyPrimaryConstructorExists(targetClass)
        if (!isTargetClassValid) {
            result.addError("No empty or optional-parameter-only primary constructor was found for class " + targetClass.qualifiedName + "")
        }

        if (isTargetClassValid) {
            val areAllRequiredTargetPropertiesMapped = targetClass.memberProperties
                    .filter { it.isLateinit }
                    .all { property ->
                        this.mappingExpressions.any { expression ->
                            expression.mapsToProperty(targetClass, property)
                        }
                    }
            if (!areAllRequiredTargetPropertiesMapped) {
                result.addError("Not all required properties of target class " + targetClass.qualifiedName + " are mapped.")
            }


            val areAllTargetPropertiesMapped = targetClass.memberProperties
                    .filter { it is KMutableProperty<*> }
                    .all { property ->
                        this.mappingExpressions.any { expression ->
                            expression.mapsToProperty(targetClass, property)
                        }
                    }
            if (!areAllTargetPropertiesMapped) {
                result.addWarning("Not all properties of target class " + targetClass.qualifiedName + " are mapped.")
            }
        }

        if (isSourceClassValid) {
            val areAllSourcePropertiesMapped = sourceClass.memberProperties.all { property ->
                this.mappingExpressions.any { expression ->
                    expression.mapsFromProperty(sourceClass, property)
                }
            }
            if (!areAllSourcePropertiesMapped) {
                result.addWarning("Not all properties of source class " + sourceClass.qualifiedName + " are mapped.")
            }
        }

        return result
    }
}