/*
 * kmap
 *
 * Copyright (c) 2018, Michael Foidl.
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