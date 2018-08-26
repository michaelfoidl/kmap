package at.michaelfoidl.kmap.caching

import at.michaelfoidl.kmap.initializable.Initializable
import java.util.*
import kotlin.reflect.KClass

class MappingCache {
    private val cache: MutableList<MappingCacheEntry<*, *>> = ArrayList()

    fun <SourceT : Any, TargetT : Any> getEntry(source: SourceT, targetClass: KClass<TargetT>): Initializable<TargetT?>? {
        val cachedElement = this.cache.find {
            it.source == source && it.targetClassName == targetClass.qualifiedName!!
        }
        return if (cachedElement != null) {
            @Suppress("UNCHECKED_CAST")
            (cachedElement as MappingCacheEntry<SourceT, TargetT?>).target
        } else {
            null
        }
    }

    fun <SourceT : Any, TargetT : Any> createEntryIfNotExists(source: SourceT, targetClass: KClass<TargetT>): Initializable<TargetT?> {
        val cached = getEntry(source, targetClass)

        return if (cached == null) {
            val result = Initializable<TargetT?>()
            this.cache.add(MappingCacheEntry(source, targetClass.qualifiedName!!, result))
            result
        } else {
            cached
        }
    }
}
