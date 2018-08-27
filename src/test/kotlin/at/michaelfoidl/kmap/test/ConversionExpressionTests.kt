/*
 * kmap
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