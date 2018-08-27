/*
 * kmap
 *
 * Copyright (c) 2018, Michael Foidl.
 */

package at.michaelfoidl.kmap.definition

import at.michaelfoidl.kmap.exceptions.MappingException
import at.michaelfoidl.kmap.ReflectionUtilities
import at.michaelfoidl.kmap.caching.MappingCache
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty


class AdditionExpression<SourceT : Any, TargetT : Any, TargetPropertyT : Any?>(
        private val targetPropertyFunction: (TargetT) -> KProperty<TargetPropertyT?>,
        private val targetValueFunction: (SourceT) -> TargetPropertyT?
) : MappingExpression<SourceT, TargetT>() {

    private var result: TargetPropertyT? = null

    override fun doConvert(source: SourceT, cache: MappingCache) {
        this.result = targetValueFunction.invoke(source)
    }

    override fun doExecute(target: TargetT) {
        val targetProperty = this.targetPropertyFunction.invoke(target)

        ReflectionUtilities.ensureThatPropertyExists(target::class, targetProperty)

        if (targetProperty is KMutableProperty<*>) {
            targetProperty.setter.call(this.result)
        } else {
            throw MappingException("Property '" + targetProperty.name + "' of " + target::class.qualifiedName + " is immutable and therefore could not be set.")
        }
    }

    override fun mapsToProperty(elementClass: KClass<TargetT>, property: KProperty<*>): Boolean {
        val targetProperty = this.targetPropertyFunction.invoke(ReflectionUtilities.createNewInstance(elementClass))
        return property.name == targetProperty.name
    }

    override fun mapsFromProperty(elementClass: KClass<SourceT>, property: KProperty<*>): Boolean {
        return false
    }
}