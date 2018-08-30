/*
 * kmap
 * version 0.1.1
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
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty


/**
 * A subtype of [MappingExpression] that supports adding a new property to the target object that has no equivalent at
 * the source object.
 *
 * @since 0.1
 * @constructor Creates a new [AdditionExpression] that can then be added to a [MappingDefinition].
 * @property targetPropertyFunction A function that returns the property to be added.
 * @property targetValueFunction A function that returns the value the target property should be set to.
 */
internal class AdditionExpression<SourceT : Any, TargetT : Any, TargetPropertyT : Any?>(
        private val targetPropertyFunction: (TargetT) -> KMutableProperty0<out TargetPropertyT?>,
        private val targetValueFunction: (SourceT) -> TargetPropertyT?
) : MappingExpression<SourceT, TargetT>() {

    private var result: TargetPropertyT? = null

    override fun doConvert(source: SourceT, cache: MappingCache) {
        this.result = targetValueFunction.invoke(source)
    }

    override fun doExecute(target: TargetT) {
        val targetProperty = this.targetPropertyFunction.invoke(target)

        ReflectionUtilities.ensureThatPropertyExists(target::class, targetProperty)

        targetProperty.setter.call(this.result)
    }

    override fun mapsToProperty(elementClass: KClass<TargetT>, property: KProperty<*>): Boolean {
        val targetProperty = this.targetPropertyFunction.invoke(ReflectionUtilities.createNewInstance(elementClass))
        return property.name == targetProperty.name
    }

    override fun mapsFromProperty(elementClass: KClass<SourceT>, property: KProperty<*>): Boolean {
        return false
    }
}