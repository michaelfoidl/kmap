package at.michaelfoidl.kmap.definition

import at.michaelfoidl.kmap.ReflectionUtilities
import at.michaelfoidl.kmap.initializable.Initializable
import at.michaelfoidl.kmap.mapper.Mapper
import at.michaelfoidl.kmap.validation.ValidationResult
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties


class MappingDefinition<SourceT : Any, TargetT : Any>(
        private var sourceClass: KClass<SourceT>,
        private var targetClass: KClass<TargetT>
) {

    @PublishedApi
    internal val mappingExpressions: ArrayList<MappingExpression<SourceT, TargetT>> = ArrayList()

    // TODO convert nullable types

    fun <SourcePropertyT : Any?, TargetPropertyT : Any> convert(
            source: (SourceT) -> KProperty<SourcePropertyT?>,
            target: (TargetT) -> KProperty<TargetPropertyT?>,
            converter: ((SourcePropertyT?) -> TargetPropertyT?)? = null
    ): MappingDefinition<SourceT, TargetT> {
        if (converter == null) {
            this.mappingExpressions.add(ConversionExpression(source, target))
        } else {
            this.mappingExpressions.add(ConversionExpression(source, target, { Initializable(converter(it)) }))
        }
        return this
    }

    // TODO check if nullable types are implemented correctly

    inline fun <reified SourcePropertyT : Any, reified TargetPropertyT : Any> convertWithMapper(
            noinline source: (SourceT) -> KProperty<SourcePropertyT?>,
            noinline target: (TargetT) -> KProperty<TargetPropertyT?>,
            mapper: Mapper
    ): MappingDefinition<SourceT, TargetT> {
        this.mappingExpressions.add(ConversionExpression(
                source,
                target,
                {
                    if (it == null) {
                        Initializable(null)
                    } else {
                        mapper.mapperProvider.provideMapper<SourcePropertyT, TargetPropertyT>(this).fetch(it)
                    }
                },
                {
                    mapper.mapperProvider.provideMapper<SourcePropertyT, TargetPropertyT>(this).execute(it)
                }))
        return this
    }

    fun <TargetPropertyT : Any?> add(
            target: (TargetT) -> KProperty<TargetPropertyT?>,
            targetValue: (SourceT) -> TargetPropertyT?
    ): MappingDefinition<SourceT, TargetT> {
        this.mappingExpressions.add(AdditionExpression(target, targetValue))
        return this
    }

    fun <SourcePropertyT : Any?> remove(
            source: (SourceT) -> KProperty<SourcePropertyT?>,
            action: (SourcePropertyT?) -> Unit
    ): MappingDefinition<SourceT, TargetT> {
        this.mappingExpressions.add(RemovalExpression(source, action))
        return this
    }

    fun <SourcePropertyT : Any?> ignore(
            source: (SourceT) -> KProperty<SourcePropertyT?>
    ): MappingDefinition<SourceT, TargetT> {
        this.mappingExpressions.add(RemovalExpression(source) {})
        return this
    }


    // TODO autoMap (just maps the property to the target property with the same name

    fun doesApply(sourceClass: KClass<*>, targetClass: KClass<*>): Boolean {
        return sourceClass == this.sourceClass && targetClass == this.targetClass
    }

    // TODO move validation to extra class

    fun validate(sourceClass: KClass<SourceT>, targetClass: KClass<TargetT>): ValidationResult {

        val result = ValidationResult()

        val isSourceClassValid = ReflectionUtilities.validateThatEmptyPrimaryConstructorExists(sourceClass)
        if (!isSourceClassValid) {
            result.addError("No empty or optional-parameter-only primary constructor was found for class " + sourceClass.qualifiedName + "")
        }

        val isTargetClassValid = ReflectionUtilities.validateThatEmptyPrimaryConstructorExists(targetClass)
        if (!isTargetClassValid) {
            result.addError("No empty or optional-parameter-only primary constructor was found for class " + targetClass.qualifiedName + "")
        }

        if (isTargetClassValid) {
            val areAllRequiredTargetPropertiesMapped = targetClass.memberProperties
                    .filter { it.isLateinit }
                    .all { property ->
                        this.mappingExpressions.any { expression ->
                            expression.mapsToProperty(targetClass, property)
                        }
                    }
            if (!areAllRequiredTargetPropertiesMapped) {
                result.addError("Not all required properties of target class " + targetClass.qualifiedName + " are mapped.")
            }


            val areAllTargetPropertiesMapped = targetClass.memberProperties
                    .filter { it is KMutableProperty<*> }
                    .all { property ->
                        this.mappingExpressions.any { expression ->
                            expression.mapsToProperty(targetClass, property)
                        }
                    }
            if (!areAllTargetPropertiesMapped) {
                result.addWarning("Not all properties of target class " + targetClass.qualifiedName + " are mapped.")
            }
        }

        if (isSourceClassValid) {
            val areAllSourcePropertiesMapped = sourceClass.memberProperties.all { property ->
                this.mappingExpressions.any { expression ->
                    expression.mapsFromProperty(sourceClass, property)
                }
            }
            if (!areAllSourcePropertiesMapped) {
                result.addWarning("Not all properties of source class " + sourceClass.qualifiedName + " are mapped.")
            }
        }

        return result
    }
}