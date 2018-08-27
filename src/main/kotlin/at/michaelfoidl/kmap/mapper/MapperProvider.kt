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

package at.michaelfoidl.kmap.mapper

import at.michaelfoidl.kmap.caching.MappingCache
import at.michaelfoidl.kmap.definition.MappingDefinition
import kotlin.reflect.KClass

class MapperProvider(
        private val mappingDefinitionFunction: (sourceClass: KClass<*>, targetClass: KClass<*>) -> MappingDefinition<*, *>
) {
    private val mappingCache: MappingCache = MappingCache()
    private val cache: HashMap<Int, ConcreteMapper<*, *>?> = HashMap()

    @PublishedApi
    internal inline fun <reified SourceT : Any, reified TargetT : Any> provideMapper(context: Any): ConcreteMapper<SourceT, TargetT> {
        return provideMapper(SourceT::class, TargetT::class, context)
    }

    @PublishedApi
    internal fun <SourceT : Any, TargetT : Any> provideMapper(sourceClass: KClass<out SourceT>, targetClass: KClass<out TargetT>, context: Any): ConcreteMapper<SourceT, TargetT> {
        val result: ConcreteMapper<*, *> =
                if (this.cache.containsKey(context.hashCode())) {
                    this.cache[context.hashCode()]!!
                } else {
                    val mapper = ConcreteMapper(
                            this.mappingDefinitionFunction(sourceClass, targetClass),
                            this.mappingCache
                    )
                    this.cache[context.hashCode()] = mapper
                    mapper
                }

        @Suppress("UNCHECKED_CAST")
        return result as ConcreteMapper<SourceT, TargetT>
    }
}