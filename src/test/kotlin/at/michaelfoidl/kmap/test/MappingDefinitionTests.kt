package at.michaelfoidl.kmap.test

import at.michaelfoidl.kmap.caching.MappingCache
import at.michaelfoidl.kmap.definition.MappingDefinition
import at.michaelfoidl.kmap.mapper.ConcreteMapper
import at.michaelfoidl.kmap.test.extensions.map
import at.michaelfoidl.moody.common.mapping.test.internal.helpers.*
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.junit.jupiter.api.Test


class MappingDefinitionTests {

    @Test
    fun validMappingDefinition_mappingWithConversionExpression_shouldBeSuccessful() {

        // Arrange
        val definition = MappingDefinition(SourceTestObject::class, TargetTestObject::class)
                .convert({ it::id }, { it::id })
                .convert({ it::string }, { it::string })
        val mapper = ConcreteMapper(definition, MappingCache())
        val sourceObject = SourceTestObject("Test", 123)

        // Act
        val result = mapper.map(sourceObject)

        // Assert
        result shouldNotBe null
        result.string shouldEqual "Test"
        result.id shouldEqual 123
    }

    @Test
    fun validMappingDefinition_mappingWithAdditionExpression_shouldBeSuccessful() {

        // Arrange
        val definition = MappingDefinition(SourceTestObject::class, TargetTestObject::class)
                .add({ it::additionalProperty }, { "Hi" })
        val mapper = ConcreteMapper(definition, MappingCache())
        val sourceObject = SourceTestObject("Test", 123)

        // Act
        val result = mapper.map(sourceObject)

        // Assert
        result shouldNotBe null
        result.additionalProperty shouldEqual "Hi"
    }

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