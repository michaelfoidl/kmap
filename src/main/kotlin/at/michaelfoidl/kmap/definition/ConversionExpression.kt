package at.michaelfoidl.kmap.definition

import at.michaelfoidl.kmap.exceptions.MappingException
import at.michaelfoidl.kmap.ReflectionUtilities
import at.michaelfoidl.kmap.initializable.Initializable
import at.michaelfoidl.kmap.caching.MappingCache
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty


class ConversionExpression<SourceT : Any, TargetT : Any, SourcePropertyT : Any?, TargetPropertyT : Any?>(
        private val sourcePropertyFunction: (SourceT) -> KProperty<SourcePropertyT?>,
        private val targetPropertyFunction: (TargetT) -> KProperty<TargetPropertyT?>,
        private val fetchFunction: (SourcePropertyT?) -> Initializable<TargetPropertyT?>,
        private val executionFunction: (Initializable<TargetPropertyT?>) -> Unit
) : MappingExpression<SourceT, TargetT>() {

    constructor(
            sourcePropertyFunction: (SourceT) -> KProperty<SourcePropertyT?>,
            targetPropertyFunction: (TargetT) -> KProperty<TargetPropertyT?>
    ) : this(
            sourcePropertyFunction,
            targetPropertyFunction,
            { Initializable(it as TargetPropertyT) },
            {}
    )

    constructor(
            sourcePropertyFunction: (SourceT) -> KProperty<SourcePropertyT?>,
            targetPropertyFunction: (TargetT) -> KProperty<TargetPropertyT?>,
            conversionFunction: (SourcePropertyT?) -> TargetPropertyT?
    ) : this(
            sourcePropertyFunction,
            targetPropertyFunction,
            { Initializable(conversionFunction(it)) },
            {}
    )

    private lateinit var result: Initializable<TargetPropertyT?>
    private lateinit var sourcePropertyName: String
    private lateinit var sourceClassName: String

    override fun doFetch(source: SourceT, cache: MappingCache) {
        val sourceProperty = this.sourcePropertyFunction.invoke(source)

        ReflectionUtilities.ensureThatPropertyExists(source::class, sourceProperty)

        this.sourcePropertyName = sourceProperty.name
        this.sourceClassName = source::class.qualifiedName!!

        val value: SourcePropertyT? = sourceProperty.getter.call()
        this.result = this.fetchFunction(value)
    }

    override fun doExecute(target: TargetT) {
        val targetProperty = this.targetPropertyFunction.invoke(target)

        ReflectionUtilities.validateThatPropertyExists(target::class, targetProperty)

        if (targetProperty is KMutableProperty<*>) {
            this.executionFunction(this.result)
            try {
                targetProperty.setter.call(this.result.value)
            } catch (exception: Exception) {
                throw MappingException("Property '" + this.sourcePropertyName + "' of " + this.sourceClassName + " could not be mapped to property '" + targetProperty.name + "' of " + target::class.qualifiedName + " due to conversion issues.", exception)
            }
        } else {
            throw MappingException("Property '" + targetProperty.name + "' of " + target::class.qualifiedName + " is immutable and therefore could not be set.")
        }
    }

    override fun mapsToProperty(elementClass: KClass<TargetT>, property: KProperty<*>): Boolean {
        val targetProperty = this.targetPropertyFunction.invoke(ReflectionUtilities.createNewInstance(elementClass))
        return property.name == targetProperty.name
    }

    override fun mapsFromProperty(elementClass: KClass<SourceT>, property: KProperty<*>): Boolean {
        val sourceProperty = this.sourcePropertyFunction.invoke(ReflectionUtilities.createNewInstance(elementClass))
        return property.name == sourceProperty.name
    }
}