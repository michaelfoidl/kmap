package at.michaelfoidl.kmap.test

import at.michaelfoidl.kmap.caching.MappingCache
import at.michaelfoidl.kmap.definition.AdditionExpression
import at.michaelfoidl.kmap.exceptions.MappingException
import at.michaelfoidl.kmap.test.helpers.SourceTestObject
import at.michaelfoidl.kmap.test.helpers.TargetTestObject
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test


class AdditionExpressionTests {

    @Test
    fun validAdditionExpression_mapping_shouldBeSuccessful() {

        // Arrange
        val expression = AdditionExpression<SourceTestObject, TargetTestObject, String>(
                { it::string },
                { "test" }
        )
        val source = SourceTestObject("string", 1)
        val target = TargetTestObject("abc", 1, "def")
        val cache = MappingCache()

        // Act
        expression.fetch(source, cache)
        expression.execute(target)

        // Assert
        target.string shouldEqual "test"
    }

    @Test
    fun additionExpressionWithFakeProperty_mapping_shouldThrowException() {

        // Arrange
        val expression = AdditionExpression<SourceTestObject, TargetTestObject, Int>(
                { String::length },
                { 42 }
        )
        val source = SourceTestObject("string", 1)
        val target = TargetTestObject("abc", 1, "def")
        val cache = MappingCache()

        val func = {
            expression.fetch(source, cache)
            expression.execute(target)
        }

        // Assert
        func shouldThrow MappingException::class
    }

    @Test
    fun additionExpressionWithImmutableProperty_mapping_shouldThrowException() {
        // Arrange
        val expression = AdditionExpression<SourceTestObject, TargetTestObject, Int>(
                { it::immutableProperty },
                { 0 }
        )
        val source = SourceTestObject("string", 1)
        val target = TargetTestObject("abc", 1, "def")
        val cache = MappingCache()

        val func = {
            expression.fetch(source, cache)
            expression.execute(target)
        }

        // Assert
        func shouldThrow MappingException::class
    }
}