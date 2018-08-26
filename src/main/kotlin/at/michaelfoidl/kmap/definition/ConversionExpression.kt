package at.michaelfoidl.kmap.definition

import at.michaelfoidl.kmap.exceptions.MappingException
import at.michaelfoidl.kmap.ReflectionUtilities
import at.michaelfoidl.kmap.initializable.Initializable
import at.michaelfoidl.kmap.caching.MappingCache
import kotlin.reflect.*


class ConversionExpression<SourceT : Any, TargetT : Any, SourcePropertyT : Any?, TargetPropertyT : Any?>(
        private val sourcePropertyFunction: (SourceT) -> KProperty0<SourcePropertyT?>,
        private val targetPropertyFunction: (TargetT) -> KMutableProperty0<out TargetPropertyT?>,
        private val conversionFunction: (SourcePropertyT?) -> Initializable<TargetPropertyT?>,
        private val executionFunction: (Initializable<TargetPropertyT?>) -> Unit
) : MappingExpression<SourceT, TargetT>() {

    constructor(
            sourcePropertyFunction: (SourceT) -> KProperty0<SourcePropertyT?>,
            targetPropertyFunction: (TargetT) -> KMutableProperty0<out TargetPropertyT?>
    ) : this(
            sourcePropertyFunction,
            targetPropertyFunction,
            null
    )

    constructor(
            sourcePropertyFunction: (SourceT) -> KProperty0<SourcePropertyT?>,
            targetPropertyFunction: (TargetT) -> KMutableProperty0<out TargetPropertyT?>,
            mappingFunction: ((SourcePropertyT) -> TargetPropertyT?)? = null,
            defaultValueFunction: () -> TargetPropertyT? = { null }
    ) : this(
            sourcePropertyFunction,
            targetPropertyFunction,
            { sourceProperty: SourcePropertyT? ->
                if (mappingFunction == null) {
                    Initializable(sourceProperty as TargetPropertyT)
                } else {
                    if (sourceProperty == null) {
                        Initializable<TargetPropertyT?>(defaultValueFunction())
                    } else {
                        Initializable<TargetPropertyT?>(mappingFunction(sourceProperty))
                    }
                }
            },
            { _: Initializable<TargetPropertyT?> -> }
    )

    private lateinit var result: Initializable<TargetPropertyT?>
    private lateinit var sourcePropertyName: String
    private lateinit var sourceClassName: String

    override fun doConvert(source: SourceT, cache: MappingCache) {
        val sourceProperty = this.sourcePropertyFunction.invoke(source)

        ReflectionUtilities.ensureThatPropertyExists(source::class, sourceProperty)

        this.sourcePropertyName = sourceProperty.name
        this.sourceClassName = source::class.qualifiedName!!

        val value: SourcePropertyT? = sourceProperty.getter.call()
        this.result = this.conversionFunction(value)
    }

    override fun doExecute(target: TargetT) {
        val targetProperty = this.targetPropertyFunction.invoke(target)

        ReflectionUtilities.validateThatPropertyExists(target::class, targetProperty)

        this.executionFunction(this.result)
        try {
            targetProperty.setter.call(this.result.value)
        } catch (exception: Exception) {
            throw MappingException("Property '" + this.sourcePropertyName + "' of " + this.sourceClassName + " could not be mapped to property '" + targetProperty.name + "' of " + target::class.qualifiedName + " due to conversion issues.", exception)
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