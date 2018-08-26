package at.michaelfoidl.kmap.definition

import at.michaelfoidl.kmap.exceptions.MappingException
import at.michaelfoidl.kmap.caching.MappingCache
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


abstract class MappingExpression<SourceT : Any, TargetT : Any> {

    private var isFetched: Boolean = false

    fun fetch(source: SourceT, cache: MappingCache) {
        doFetch(source, cache)
        isFetched = true
    }

    fun execute(target: TargetT) {
        if (!isFetched) {
            throw MappingException("Mapping can only be executed after fetching. Call fetch() first.")
        }
        doExecute(target)
    }

    protected abstract fun doFetch(source: SourceT, cache: MappingCache)

    protected abstract fun doExecute(target: TargetT)

    internal abstract fun mapsToProperty(elementClass: KClass<TargetT>, property: KProperty<*>): Boolean

    internal abstract fun mapsFromProperty(elementClass: KClass<SourceT>, property: KProperty<*>): Boolean
}