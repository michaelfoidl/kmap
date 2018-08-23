package at.michaelfoidl.kmap.mapper

import at.michaelfoidl.kmap.caching.MappingCache
import at.michaelfoidl.kmap.definition.MappingDefinition
import kotlin.reflect.KClass

class MapperProvider(
        private val mappingDefinitionFunction: (sourceClass: KClass<*>, targetClass: KClass<*>) -> MappingDefinition<*, *>
) {
    private val mappingCache: MappingCache = MappingCache()
    private val cache: HashMap<Int, ConcreteMapper<*, *>?> = HashMap()

    @PublishedApi
    internal inline fun <reified SourceT : Any, reified TargetT : Any> provideMapper(context: Any): ConcreteMapper<SourceT, TargetT> {
        return provideMapper(SourceT::class, TargetT::class, context)
    }

    @PublishedApi
    internal fun <SourceT : Any, TargetT : Any> provideMapper(sourceClass: KClass<out SourceT>, targetClass: KClass<out TargetT>, context: Any): ConcreteMapper<SourceT, TargetT> {
        val result: ConcreteMapper<*, *> =
                if (this.cache.containsKey(context.hashCode())) {
                    this.cache[context.hashCode()]!!
                } else {
                    val mapper = ConcreteMapper(
                            this.mappingDefinitionFunction(sourceClass, targetClass),
                            this.mappingCache
                    )
                    this.cache[context.hashCode()] = mapper
                    mapper
                }

        @Suppress("UNCHECKED_CAST")
        return result as ConcreteMapper<SourceT, TargetT>
    }
}