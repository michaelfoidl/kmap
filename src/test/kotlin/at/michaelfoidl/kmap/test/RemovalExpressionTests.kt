/*
 * kmap
 *
 * Copyright (c) 2018, Michael Foidl.
 */

package at.michaelfoidl.kmap.test

import at.michaelfoidl.kmap.caching.MappingCache
import at.michaelfoidl.kmap.definition.RemovalExpression
import at.michaelfoidl.kmap.exceptions.MappingException
import at.michaelfoidl.kmap.test.helpers.Echo
import at.michaelfoidl.kmap.test.helpers.SourceTestObject
import at.michaelfoidl.kmap.test.helpers.TargetTestObject
import org.amshove.kluent.*
import org.junit.jupiter.api.Test


class RemovalExpressionTests {

    private val echo: Echo = mock()

    @Test
    fun validRemovalExpression_mapping_shouldBeSuccessful() {

        // Arrange
        val expression = RemovalExpression<SourceTestObject, TargetTestObject, String>(
                { it::string },
                { this.echo.echo(it!!.length) }
        )
        val source = SourceTestObject("string", 1)
        val target = TargetTestObject("abc", 1, "def")
        val cache = MappingCache()

        // Act
        expression.convert(source, cache)
        expression.execute(target)

        // Assert
        target.string shouldEqual "abc"
        Verify on this.echo that this.echo.echo(6) was called
    }

    @Test
    fun validRemovalExpressionWithImmutableProperty_mapping_shouldBeSuccessful() {
        // Arrange
        val expression = RemovalExpression<SourceTestObject, TargetTestObject, Int>(
                { it::immutableProperty },
                { this.echo.echo(it!! + 2) }
        )
        val source = SourceTestObject("string", 1)
        val target = TargetTestObject("abc", 1, "def")
        val cache = MappingCache()

        // Act
        expression.convert(source, cache)
        expression.execute(target)

        // Assert
        Verify on this.echo that this.echo.echo(113) was called
    }

    @Test
    fun removalExpressionWithFakeProperty_mapping_shouldThrowException() {

        // Arrange
        val expression = RemovalExpression<SourceTestObject, TargetTestObject, Int>(
                { String::length },
                { it!! + 2 }
        )
        val source = SourceTestObject("string", 1)
        val target = TargetTestObject("abc", 1, "def")
        val cache = MappingCache()

        val func = {
            expression.convert(source, cache)
            expression.execute(target)
        }

        // Assert
        func shouldThrow MappingException::class
    }
}