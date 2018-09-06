/*
 * kmap
 * version 0.1.2
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
import kotlin.reflect.KClass


/**
 * A mapper implementation that can be used for mapping between one specific source object and one specific target object.
 * The direction cannot be reversed.
 *
 * @since 0.1
 * @constructor Creates a new instance with a single [MappingDefinition] to use.
 * @param mappingDefinition the definition that should be used for mapping.
 * @param cache the cache to be used for mapping.
 */
@PublishedApi
internal class ConcreteMapper<SourceT : Any, TargetT : Any>(
        mappingDefinition: MappingDefinition<SourceT, TargetT>,
        @PublishedApi
        internal val cache: MappingCache
) {
    @PublishedApi
    internal var mappingExpressions: ArrayList<MappingExpression<SourceT, TargetT>> = mappingDefinition.mappingExpressions

    /**
     * Executes the conversion step of the mapping process for every [MappingExpression].
     *
     * @param source the source object that should be mapped.
     * @return the result of the conversion step which might not be initialized yet.
     */
    inline fun <reified MappingTargetT : TargetT> convert(source: SourceT): Initializable<MappingTargetT?> {
        return convert(source, MappingTargetT::class)
    }

    /**
     * Executes the conversion step of the mapping process for every [MappingExpression].
     *
     * @param source the source object that should be mapped.
     * @param targetClass the class of the target object.
     * @return the result of the conversion step which might not be initialized yet.
     */
    fun <MappingTargetT: TargetT> convert(source: SourceT, targetClass: KClass<MappingTargetT>): Initializable<MappingTargetT?> {
        val cached = this.cache.getEntry(source, targetClass)
        return if (cached == null) {
            val result = this.cache.createEntryIfNotExists(source, targetClass)
            this.mappingExpressions.forEach {
                it.convert(source, this.cache)
            }
            result
        } else {
            cached
        }
    }

    /**
     * Executes the execution step of the mapping process for every [MappingExpression].
     *
     * @param converted the result of the conversion step to be used in the execution step.
     */
    inline fun <reified MappingTargetT : TargetT> execute(converted: Initializable<in MappingTargetT?>) {
        execute(converted, MappingTargetT::class)
    }

    /**
     * Executes the execution step of the mapping process for every [MappingExpression].
     *
     * @param converted the result of the conversion step to be used in the execution step.
     * @param targetClass the class of the target object.
     */
    fun <MappingTargetT: TargetT> execute(converted: Initializable<in MappingTargetT?>, targetClass: KClass<MappingTargetT>) {
        if (!converted.isInitialized) {
            val target = ReflectionUtilities.createNewInstance(targetClass)
            converted.initialize(target)
            this.mappingExpressions.forEach {
                it.execute(target)
            }
        }
    }
}