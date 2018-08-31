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

package at.michaelfoidl.kmap.validation

import at.michaelfoidl.kmap.ReflectionUtilities
import at.michaelfoidl.kmap.definition.MappingDefinition
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties


/**
 * Validates a [MappingDefinition].
 *
 * @since 0.1.2
 */
internal object Validator {

    /**
     * Checks, if the given [MappingDefinition] is valid for mapping between the given classes.
     *
     * @param mappingDefinition the definition to be validated.
     * @param sourceClass the source class the definition should be validated for.
     * @param targetClass the target class the definition should be validated for.
     * @return the result of the validation process.
     */
    fun <SourceT : Any, TargetT : Any> validate(
            mappingDefinition: MappingDefinition<SourceT, TargetT>,
            sourceClass: KClass<SourceT>,
            targetClass: KClass<TargetT>
    ): ValidationResult {

        val mappingExpressions = mappingDefinition.mappingExpressions

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
                        mappingExpressions.any { expression ->
                            expression.mapsToProperty(targetClass, property)
                        }
                    }
            if (!areAllRequiredTargetPropertiesMapped) {
                result.addError("Not all required properties of target class " + targetClass.qualifiedName + " are mapped.")
            }

            val areAllTargetPropertiesMapped = targetClass.memberProperties
                    .filter { it is KMutableProperty<*> }
                    .all { property ->
                        mappingExpressions.any { expression ->
                            expression.mapsToProperty(targetClass, property)
                        }
                    }
            if (!areAllTargetPropertiesMapped) {
                result.addWarning("Not all properties of target class " + targetClass.qualifiedName + " are mapped.")
            }
        }

        if (isSourceClassValid) {
            val areAllSourcePropertiesMapped = sourceClass.memberProperties.all { property ->
                mappingExpressions.any { expression ->
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