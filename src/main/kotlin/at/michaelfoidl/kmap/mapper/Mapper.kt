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

import at.michaelfoidl.kmap.exceptions.MappingException
import at.michaelfoidl.kmap.definition.MappingDefinition
import kotlin.reflect.KClass


abstract class Mapper {

    var mapperProvider: MapperProvider = MapperProvider { sourceClass, targetClass -> findDefinition(sourceClass, targetClass) }
        private set

    inline fun <reified TargetT : Any> map(source: Any): TargetT {
        val concreteMapper = this.mapperProvider.provideMapper(source::class, TargetT::class, source)
        val result = concreteMapper.convert<TargetT>(source)
        concreteMapper.execute(result)
        return result.value!!
    }

    @PublishedApi
    internal fun findDefinition(sourceClass: KClass<*>, targetClass: KClass<*>): MappingDefinition<*, *> {
        return provideDefinitions().find { it.doesApply(sourceClass, targetClass) }
                ?: throw MappingException("There is no mapping definition specified for mapping " + sourceClass.qualifiedName + " to " + targetClass.qualifiedName + "")
    }

    abstract fun provideDefinitions(): List<MappingDefinition<*, *>>
}