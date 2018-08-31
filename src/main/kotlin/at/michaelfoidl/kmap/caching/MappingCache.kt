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

package at.michaelfoidl.kmap.caching

import at.michaelfoidl.kmap.initializable.Initializable
import java.util.*
import kotlin.reflect.KClass


/**
 * Stores recently mapped objects in order to be reused. This is also necessary to prevent stack overflow errors when
 * mapping circular references.
 *
 * @since 0.1
 * @constructor Creates a new, empty cache instance.
 */
@PublishedApi
internal class MappingCache {
    private val cache: MutableList<MappingCacheEntry<*, *>> = ArrayList()

    /**
     * Checks if there is an entry corresponding to the source object and the class of the target object in the cache.
     *
     * @param source the source object for which a corresponding entry should be found.
     * @param targetClass the class of the target object for which a corresponding entry should be found.
     * @return the cached entry or `null`, if none was found.
     */
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

    /**
     * Checks if there is an entry correponding to the source object and the class of the target object in the cache. If
     * there is not, a new entry is created.
     *
     * @param source the source object for which a corresponding entry should be created.
     * @param targetClass the class of the target object for which a corresponding entry should be created.
     * @return the entry, either newly created or fetched from the cache.
     */
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
