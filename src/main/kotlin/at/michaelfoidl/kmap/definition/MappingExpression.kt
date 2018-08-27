/*
 * kmap
 *
 * Copyright (c) 2018, Michael Foidl.
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