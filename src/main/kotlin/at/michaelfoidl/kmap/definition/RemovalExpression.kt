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
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

/**
 * A subtype of [MappingExpression] that supports removing or ignoring a property of the source object that has no equivalent
 * at the target object.
 *
 * @since 0.1
 * @constructor Creates a new [RemovalExpression] defined by a [sourcePropertyFunction] returning the source property and
 * an [actionFunction] executing any task that might be done since there is a loss of information.
 */
class RemovalExpression<SourceT : Any, TargetT : Any, SourcePropertyT : Any?>(
        private val sourcePropertyFunction: (SourceT) -> KProperty0<SourcePropertyT?>,
        private val actionFunction: (SourcePropertyT?) -> Unit
) : MappingExpression<SourceT, TargetT>() {

    override fun doConvert(source: SourceT, cache: MappingCache) {
        val sourceProperty = this.sourcePropertyFunction.invoke(source)

        ReflectionUtilities.ensureThatPropertyExists(source::class, sourceProperty)

        val value: SourcePropertyT? = sourceProperty.getter.call()

        this.actionFunction(value)
    }

    override fun doExecute(target: TargetT) {

    }

    override fun mapsToProperty(elementClass: KClass<TargetT>, property: KProperty<*>): Boolean {
        return false
    }

    override fun mapsFromProperty(elementClass: KClass<SourceT>, property: KProperty<*>): Boolean {
        val sourceProperty = this.sourcePropertyFunction.invoke(ReflectionUtilities.createNewInstance(elementClass))
        return property.name == sourceProperty.name
    }
}