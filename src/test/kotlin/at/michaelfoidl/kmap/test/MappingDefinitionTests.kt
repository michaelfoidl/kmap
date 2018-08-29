/*
 * kmap
 * version 0.1.1
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

package at.michaelfoidl.kmap.test

import at.michaelfoidl.kmap.definition.MappingDefinition
import at.michaelfoidl.kmap.test.helpers.*
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test


class MappingDefinitionTests {

    @Test
    fun validMappingDefinition_shouldApplyForCorrectTypes() {

        // Arrange
        val definition = MappingDefinition(SourceTestObject::class, TargetTestObject::class)

        // Act
        val result = definition.doesApply(SourceTestObject::class, TargetTestObject::class)

        // Assert
        result shouldEqual true
    }

    @Test
    fun validMappingDefinition_shouldNotApplyForIncorrectTypes() {

        // Arrange
        val definition = MappingDefinition(SourceTestObject::class, TargetTestObject::class)

        // Act
        val result = definition.doesApply(SourceTestObject::class, TargetTestObjectWithoutConstructor::class)

        // Assert
        result shouldEqual false
    }

    @Test
    fun validMappingDefinitionForTargetWithEmptyPrimaryConstructor_validation_shouldBeSuccessful() {

        // Arrange
        val definition = MappingDefinition(SourceTestObject::class, TargetTestObject::class)
                .convert({ it::id }, { it::id })
                .convert({ it::string }, { it::string })
                .add({ it::additionalProperty }, { it.string.length })
                .add({ it::nullableProperty }, { null })
                .ignore { it::immutableProperty }

        // Act
        val result = definition.validate(SourceTestObject::class, TargetTestObject::class)

        // Assert
        result.isSuccess() shouldBe true
    }

    @Test
    fun validMappingDefintionForTargetWithDefaultValueConstructor_validation_shouldBeSuccessful() {

        // Arrange
        val definition = MappingDefinition(SourceTestObject::class, TargetTestObjectWithDefaultConstructor::class)
                .convert({ it::id }, { it::id })
                .convert({ it::string }, { it::string })
                .ignore { it::immutableProperty }

        // Act
        val result = definition.validate(SourceTestObject::class, TargetTestObjectWithDefaultConstructor::class)

        // Assert
        result.isSuccess() shouldBe true
    }

    @Test
    fun mappingDefinitionWithUnmappedSourceProperties_validation_shouldReturnWarning() {

        // Arrange
        val definition = MappingDefinition(SourceTestObject::class, TargetTestObject::class)
                .convert({ it::id }, { it::id })
                .add({ it::string }, { "abc" })
                .add({ it::additionalProperty }, { it.string.length.toString() })
                .add({ it::nullableProperty }, { null })
                .ignore { it::immutableProperty }

        // Act
        val result = definition.validate(SourceTestObject::class, TargetTestObject::class)

        // Assert
        result.isWarning() shouldBe true
    }

    @Test
    fun mappingDefinitionWithUnmappedRequiredTargetProperties_validation_shouldReturnError() {

        // Arrange
        val definition = MappingDefinition(SourceTestObject::class, TargetTestObject::class)
                .convert({ it::id }, { it::id })
                .convert({ it::string }, { it::additionalProperty }, { it.length.toString() })
                .add({ it::nullableProperty }, { null })
                .ignore { it::immutableProperty }

        // Act
        val result = definition.validate(SourceTestObject::class, TargetTestObject::class)

        // Assert
        result.isFailure() shouldBe true
    }

    @Test
    fun mappingDefinitionWithUnmappedNullableTargetProperties_validation_shouldReturnWarning() {

        // Arrange
        val definition = MappingDefinition(SourceTestObject::class, TargetTestObject::class)
                .convert({ it::id }, { it::id })
                .convert({ it::string }, { it::string })
                .add({ it::additionalProperty }, { it.string.length.toString() })
                .ignore { it::immutableProperty }

        // Act
        val result = definition.validate(SourceTestObject::class, TargetTestObject::class)

        // Assert
        result.isWarning() shouldBe true
    }

    @Test
    fun mappingDefinitionForTargetWithoutEmptyConstructor_validation_shouldReturnError() {

        // Arrange
        val definition = MappingDefinition(SourceTestObject::class, TargetTestObjectWithoutConstructor::class)
                .convert({ it::id }, { it::id })
                .convert({ it::string }, { it::string })
                .ignore { it::immutableProperty }

        // Act
        val result = definition.validate(SourceTestObject::class, TargetTestObjectWithoutConstructor::class)

        // Assert
        result.isFailure() shouldBe true
    }


    @Test
    fun mappingDefintionForSourceWithoutEmptyConstructor_validation_shouldReturnError() {

        // Arrange
        val definition = MappingDefinition(SourceTestObjectWithoutConstructor::class, TargetTestObject::class)
                .convert({ it::id }, { it::id })
                .convert({ it::string }, { it::string })
                .add({ it::additionalProperty }, { it.string.length.toString() })
                .add({ it::nullableProperty }, { null })

        // Act
        val result = definition.validate(SourceTestObjectWithoutConstructor::class, TargetTestObject::class)

        // Assert
        result.isFailure() shouldBe true
    }
}