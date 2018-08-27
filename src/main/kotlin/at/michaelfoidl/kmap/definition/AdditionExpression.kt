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

import at.michaelfoidl.kmap.exceptions.MappingException
import at.michaelfoidl.kmap.ReflectionUtilities
import at.michaelfoidl.kmap.caching.MappingCache
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty


class AdditionExpression<SourceT : Any, TargetT : Any, TargetPropertyT : Any?>(
        private val targetPropertyFunction: (TargetT) -> KProperty<TargetPropertyT?>,
        private val targetValueFunction: (SourceT) -> TargetPropertyT?
) : MappingExpression<SourceT, TargetT>() {

    private var result: TargetPropertyT? = null

    override fun doConvert(source: SourceT, cache: MappingCache) {
        this.result = targetValueFunction.invoke(source)
    }

    override fun doExecute(target: TargetT) {
        val targetProperty = this.targetPropertyFunction.invoke(target)

        ReflectionUtilities.ensureThatPropertyExists(target::class, targetProperty)

        if (targetProperty is KMutableProperty<*>) {
            targetProperty.setter.call(this.result)
        } else {
            throw MappingException("Property '" + targetProperty.name + "' of " + target::class.qualifiedName + " is immutable and therefore could not be set.")
        }
    }

    override fun mapsToProperty(elementClass: KClass<TargetT>, property: KProperty<*>): Boolean {
        val targetProperty = this.targetPropertyFunction.invoke(ReflectionUtilities.createNewInstance(elementClass))
        return property.name == targetProperty.name
    }

    override fun mapsFromProperty(elementClass: KClass<SourceT>, property: KProperty<*>): Boolean {
        return false
    }
}