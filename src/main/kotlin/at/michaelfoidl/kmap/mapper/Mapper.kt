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

package at.michaelfoidl.kmap.mapper

import at.michaelfoidl.kmap.exceptions.MappingException
import at.michaelfoidl.kmap.definition.MappingDefinition
import kotlin.reflect.KClass


/**
 * Base class for a mapper implementation. When mapping a concrete source object to a target type, the according [MappingDefinition]
 * is chosen from the pool of provided definitions.
 *
 * @since 0.1
 */
abstract class Mapper {

    /**
     * Provides the concrete mappers executing the actual mapping process.
     */
    @PublishedApi
    internal var mapperProvider: MapperProvider = MapperProvider { sourceClass, targetClass -> findDefinition(sourceClass, targetClass) }
        private set

    /**
     * Maps the given source object to the defined target type.
     *
     * @param source the source object to be mapped.
     * @return the mapped object.
     */
    inline fun <reified TargetT : Any> map(source: Any): TargetT {
        val concreteMapper = this.mapperProvider.provideMapper(source::class, TargetT::class, source)
        val result = concreteMapper.convert<TargetT>(source)
        concreteMapper.execute(result)
        return result.value!!
    }

    /**
     * Searches in the pool of given definitions for a [MappingDefinition] capable of mapping an object of the given
     * source class to an object of the given target class.
     *
     * @param sourceClass the class of the source object the definition should be found for.
     * @param targetClass the class of the target object the definition should be found for.
     * @return the [MappingDefinition] for mapping between the given types.
     * @throws MappingException if there is no [MappingDefinition] matching the requirements.
     */
    @PublishedApi
    internal fun findDefinition(sourceClass: KClass<*>, targetClass: KClass<*>): MappingDefinition<*, *> {
        return provideDefinitions().find { it.doesApply(sourceClass, targetClass) }
                ?: throw MappingException("There is no mapping definition specified for mapping " + sourceClass.qualifiedName + " to " + targetClass.qualifiedName + "")
    }

    /**
     * Provides the pool of [MappingDefinition]s defining the mapping processes the mapper can execute.
     *
     * @return the pool of [MappingDefinition]s the mapper can use.
     */
    abstract fun provideDefinitions(): List<MappingDefinition<*, *>>
}