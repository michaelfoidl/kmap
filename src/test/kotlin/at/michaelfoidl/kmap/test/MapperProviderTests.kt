package at.michaelfoidl.kmap.test

import at.michaelfoidl.kmap.definition.MappingDefinition
import at.michaelfoidl.kmap.mapper.ConcreteMapper
import at.michaelfoidl.kmap.mapper.MapperProvider
import at.michaelfoidl.moody.common.mapping.test.internal.helpers.SourceTestObject
import at.michaelfoidl.moody.common.mapping.test.internal.helpers.TargetTestObject
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.amshove.kluent.shouldNotEqual
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

class MapperProviderTests {

    private val mappingDefintionFunctionMock = { sourceClass: KClass<*>, targetClass: KClass<*> -> MappingDefinition(sourceClass, targetClass) }

    @Test
    fun mapperProvider_request_shouldBeSuccessful() {

        // Arrange
        val provider = MapperProvider(mappingDefintionFunctionMock)

        // Act
        val result = provider.provideMapper<SourceTestObject, TargetTestObject>(this)

        // Assert
        result shouldNotBe null
        result shouldBeInstanceOf ConcreteMapper::class
    }

    @Test
    fun mapperProvider_providingWithSameContextTwice_shouldReturnSameMapperTwice() {

        // Arrange
        val provider = MapperProvider(mappingDefintionFunctionMock)

        // Act
        val result1 = provider.provideMapper<SourceTestObject, TargetTestObject>(this)
        val result2 = provider.provideMapper<SourceTestObject, TargetTestObject>(this)

        // Assert
        result1 shouldNotBe null
        result2 shouldNotBe null
        result1 shouldEqual result2
    }

    @Test
    fun mapperProvider_providingWithDifferentContext_shouldReturnDifferentMapper() {

        // Arrange
        val provider = MapperProvider(mappingDefintionFunctionMock)
        val context1 = "context1"
        val context2 = "context2"

        // Act
        val result1 = provider.provideMapper<SourceTestObject, TargetTestObject>(context1)
        val result2 = provider.provideMapper<SourceTestObject, TargetTestObject>(context2)

        // Assert
        result1 shouldNotBe null
        result2 shouldNotBe null
        result1 shouldNotEqual result2
    }
}