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

package at.michaelfoidl.kmap.definition

import at.michaelfoidl.kmap.exceptions.MappingException
import at.michaelfoidl.kmap.caching.MappingCache
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


/**
 * Base class for defining different ways to map between source and target objects.
 *
 * @since 0.1
 */
@PublishedApi
internal abstract class MappingExpression<SourceT : Any, TargetT : Any> {

    private var isConverted: Boolean = false

    /**
     * Calls the conversion step of the mapping process and stores the result.
     * @param source the source object.
     * @param cache the cache to be used for the conversion process.
     */
    @PublishedApi
    internal fun convert(source: SourceT, cache: MappingCache) {
        doConvert(source, cache)
        isConverted = true
    }

    /**
     * Calls the execution step of the mapping process using the result of the conversion step. Therefore, the execution
     * step can only be executed after the conversion step.
     *
     * @param target the target object.
     * @throws MappingException if the conversion step has not been executed yet.
     */
    @PublishedApi
    internal fun execute(target: TargetT) {
        if (!isConverted) {
            throw MappingException("Mapping can only be executed after converting. Call convert() first.")
        }
        doExecute(target)
    }

    /**
     * Represents the conversion step of the mapping process where the value of the source property is fetched and
     * converted to the target type.
     *
     * @param source the source object.
     * @param cache the cache to be used for the conversion process.
     */
    protected abstract fun doConvert(source: SourceT, cache: MappingCache)

    /**
     * Represents the execution step of the mapping process where the result of the conversion step is written to the
     * target object.
     *
     * @param target the target object.
     */
    protected abstract fun doExecute(target: TargetT)

    /**
     * Checks, if the given property of the given class is the target property of this [MappingExpression]. Note
     * that some expressions might not have a target property at all.
     *
     * @param elementClass the class for which the test should be done.
     * @param property the property for which the test should be done.
     */
    internal abstract fun mapsToProperty(elementClass: KClass<TargetT>, property: KProperty<*>): Boolean

    /**
     * Checks, if the given property of the given class is the source property of this [MappingExpression]. Note
     * that some expressions might not have a source property at all.
     *
     * @param elementClass the class for which the test should be done.
     * @param property the property for which the test should be done.
     */
    internal abstract fun mapsFromProperty(elementClass: KClass<SourceT>, property: KProperty<*>): Boolean
}