package at.michaelfoidl.kmap.definition

import at.michaelfoidl.kmap.ReflectionUtilities
import at.michaelfoidl.kmap.caching.MappingCache
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


class RemovalExpression<SourceT : Any, TargetT : Any, SourcePropertyT : Any?>(
        private val sourcePropertyFunction: (SourceT) -> KProperty<SourcePropertyT?>,
        private val actionFunction: (SourcePropertyT?) -> Unit
) : MappingExpression<SourceT, TargetT>() {

    override fun doConvert(source: SourceT, cache: MappingCache) {
        val sourceProperty = this.sourcePropertyFunction.invoke(source)

        ReflectionUtilities.ensureThatPropertyExists(source::class, sourceProperty)

        val value: SourcePropertyT? = sourceProperty.getter.call()

        this.actionFunction(value)
    }

    override fun doExecute(target: TargetT) {

    }

    override fun mapsToProperty(elementClass: KClass<TargetT>, property: KProperty<*>): Boolean {
        return false
    }

    override fun mapsFromProperty(elementClass: KClass<SourceT>, property: KProperty<*>): Boolean {
        val sourceProperty = this.sourcePropertyFunction.invoke(ReflectionUtilities.createNewInstance(elementClass))
        return property.name == sourceProperty.name
    }
}