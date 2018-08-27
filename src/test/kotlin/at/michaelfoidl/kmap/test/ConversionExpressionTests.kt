/*
 * kmap
 *
 * Copyright (c) 2018, Michael Foidl.
 */

package at.michaelfoidl.kmap.test

import at.michaelfoidl.kmap.caching.MappingCache
import at.michaelfoidl.kmap.definition.ConversionExpression
import at.michaelfoidl.kmap.exceptions.MappingException
import at.michaelfoidl.kmap.test.helpers.SourceTestObject
import at.michaelfoidl.kmap.test.helpers.TargetTestObject
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test


class ConversionExpressionTests {

    @Test
    fun validConversionExpression_mapping_shouldBeSuccessful() {

        // Arrange
        val expression = ConversionExpression<SourceTestObject, TargetTestObject, String, String>(
                { it::string },
                { it::string }
        )
        val source = SourceTestObject("string", 1)
        val target = TargetTestObject("abc", 0, "def")
        val cache = MappingCache()

        // Act
        expression.convert(source, cache)
        expression.execute(target)

        // Assert
        target.string shouldEqual "string"
    }

    @Test
    fun validConversionExpressionWithConverter_mapping_shouldBeSuccessful() {

        // Arrange
        val expression = ConversionExpression<SourceTestObject, TargetTestObject, String, Int?>(
                { it::string },
                { it::nullableProperty },
                { it.length }
        )
        val source = SourceTestObject("string", 1)
        val target = TargetTestObject("abc", 0, "def")
        val cache = MappingCache()

        // Act
        expression.convert(source, cache)
        expression.execute(target)

        // Assert
        target.nullableProperty shouldEqual "string".length
    }

    @Test
    fun validConversionExpressionWithImplicitConverter_mapping_shouldBeSuccessful() {

        // Arrange
        val expression = ConversionExpression<SourceTestObject, TargetTestObject, Int, Long>(
                { it::immutableProperty },
                { it::id }
        )
        val source = SourceTestObject("string", 1)
        val target = TargetTestObject("abc", 0, "def")
        val cache = MappingCache()

        // Act
        expression.convert(source, cache)
        expression.execute(target)

        // Assert
        target.id shouldEqual 111
    }

    @Test
    fun conversionExpressionWithoutNecessaryConverter_mapping_shouldThrowException() {

        // Arrange
        val expression = ConversionExpression<SourceTestObject, TargetTestObject, String, Int?>(
                { it::string },
                { it::nullableProperty }
        )
        val source = SourceTestObject("string", 1)
        val target = TargetTestObject("abc", 0, "def")
        val cache = MappingCache()

        val func = {
            expression.convert(source, cache)
            expression.execute(target)
        }

        // Assert
        func shouldThrow MappingException::class
    }
}