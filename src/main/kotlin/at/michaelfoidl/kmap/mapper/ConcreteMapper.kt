/*
 * kmap
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

package at.michaelfoidl.kmap.mapper

import at.michaelfoidl.kmap.ReflectionUtilities
import at.michaelfoidl.kmap.initializable.Initializable
import at.michaelfoidl.kmap.caching.MappingCache
import at.michaelfoidl.kmap.definition.MappingDefinition
import at.michaelfoidl.kmap.definition.MappingExpression
import java.util.*


class ConcreteMapper<SourceT : Any, TargetT : Any>(
        mappingDefinition: MappingDefinition<SourceT, TargetT>,
        @PublishedApi
        internal val mappingCache: MappingCache
) {
    @PublishedApi
    internal var mappingExpressions: ArrayList<MappingExpression<SourceT, TargetT>> = mappingDefinition.mappingExpressions

    @PublishedApi
    internal inline fun <reified MappingTargetT : TargetT> convert(source: SourceT): Initializable<MappingTargetT?> {
        val cached = this.mappingCache.getEntry(source, MappingTargetT::class)
        return if (cached == null) {
            val result = this.mappingCache.createEntryIfNotExists(source, MappingTargetT::class)
            this.mappingExpressions.forEach {
                it.convert(source, this.mappingCache)
            }
            result
        } else {
            cached
        }
    }

    @PublishedApi
    internal inline fun <reified MappingTargetT : TargetT> execute(converted: Initializable<in MappingTargetT?>) {
        if (!converted.isInitialized) {
            val target = ReflectionUtilities.createNewInstance(MappingTargetT::class)
            converted.initialize(target)
            this.mappingExpressions.forEach {
                it.execute(target)
            }
        }
    }
}