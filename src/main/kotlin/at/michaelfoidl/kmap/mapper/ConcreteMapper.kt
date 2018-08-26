package at.michaelfoidl.kmap.mapper

import at.michaelfoidl.kmap.ReflectionUtilities
import at.michaelfoidl.kmap.initializable.Initializable
import at.michaelfoidl.kmap.caching.MappingCache
import at.michaelfoidl.kmap.definition.MappingDefinition
import at.michaelfoidl.kmap.definition.MappingExpression
import java.util.*


class ConcreteMapper<SourceT : Any, TargetT : Any>(
        mappingDefinition: MappingDefinition<SourceT, TargetT>,
        @PublishedApi
        internal val mappingCache: MappingCache
) {
    @PublishedApi
    internal var mappingExpressions: ArrayList<MappingExpression<SourceT, TargetT>> = mappingDefinition.mappingExpressions

    @PublishedApi
    internal inline fun <reified MappingTargetT : TargetT> convert(source: SourceT): Initializable<MappingTargetT?> {
        val cached = this.mappingCache.getEntry(source, MappingTargetT::class)
        return if (cached == null) {
            val result = this.mappingCache.createEntryIfNotExists(source, MappingTargetT::class)
            this.mappingExpressions.forEach {
                it.convert(source, this.mappingCache)
            }
            result
        } else {
            cached
        }
    }

    @PublishedApi
    internal inline fun <reified MappingTargetT : TargetT> execute(converted: Initializable<in MappingTargetT?>) {
        if (!converted.isInitialized) {
            val target = ReflectionUtilities.createNewInstance(MappingTargetT::class)
            converted.initialize(target)
            this.mappingExpressions.forEach {
                it.execute(target)
            }
        }
    }
}