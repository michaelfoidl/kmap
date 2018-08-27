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
import at.michaelfoidl.kmap.caching.MappingCache
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


abstract class MappingExpression<SourceT : Any, TargetT : Any> {

    private var isConverted: Boolean = false

    fun convert(source: SourceT, cache: MappingCache) {
        doConvert(source, cache)
        isConverted = true
    }

    fun execute(target: TargetT) {
        if (!isConverted) {
            throw MappingException("Mapping can only be executed after converting. Call convert() first.")
        }
        doExecute(target)
    }

    protected abstract fun doConvert(source: SourceT, cache: MappingCache)

    protected abstract fun doExecute(target: TargetT)

    internal abstract fun mapsToProperty(elementClass: KClass<TargetT>, property: KProperty<*>): Boolean

    internal abstract fun mapsFromProperty(elementClass: KClass<SourceT>, property: KProperty<*>): Boolean
}