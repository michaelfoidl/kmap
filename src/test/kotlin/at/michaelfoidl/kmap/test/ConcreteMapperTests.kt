/*
 * kmap
 * version 0.1
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
import at.michaelfoidl.kmap.definition.MappingDefinition
import at.michaelfoidl.kmap.exceptions.MappingException
import at.michaelfoidl.kmap.initializable.Initializable
import at.michaelfoidl.kmap.mapper.ConcreteMapper
import at.michaelfoidl.kmap.test.extensions.map
import at.michaelfoidl.kmap.test.helpers.SourceTestObject
import at.michaelfoidl.kmap.test.helpers.TargetTestObject
import at.michaelfoidl.kmap.test.helpers.TargetTestObjectWithPrivateConstructor
import at.michaelfoidl.kmap.test.helpers.TargetTestObjectWithoutConstructor
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.amshove.kluent.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

class ConcreteMapperTests {

    private val mappingCacheMock: MappingCache = mock()

    @BeforeEach
    fun setup() {
        When calling this.mappingCacheMock.createEntryIfNotExists(any<Any>(), any<KClass<Any>>()) itAnswers { Initializable() }
    }

    @Test
    fun validMapper_mapping_shouldCreateNewInstanceOfTarget() {

        // Arrange
        val mapper = ConcreteMapper(
                MappingDefinition(SourceTestObject::class, TargetTestObject::class),
                this.mappingCacheMock)
        val sourceObject = SourceTestObject("string", 1)

        // Act
        val result = mapper.map(sourceObject)

        // Assert
        result shouldNotBe null
        result shouldBeInstanceOf TargetTestObject::class
    }

    @Test
    fun validMapper_mappingToTargetWithoutEmptyConstructor_shouldThrowException() {

        // Arrange
        val mapper = ConcreteMapper(
                MappingDefinition(SourceTestObject::class, TargetTestObjectWithoutConstructor::class),
                this.mappingCacheMock)
        val sourceObject = SourceTestObject("Test", 123)
        val func = {
            mapper.map(sourceObject)
        }

        // Assert
        func shouldThrow MappingException::class
    }

    @Test
    fun validMapper_mappingToTargetWithPrivateConstructor_shouldBeSuccessful() {
        // Arrange
        val mapper = ConcreteMapper(
                MappingDefinition(SourceTestObject::class, TargetTestObjectWithPrivateConstructor::class),
                this.mappingCacheMock)
        val sourceObject = SourceTestObject("string", 1)

        // Act
        val result = mapper.map(sourceObject)

        // Assert
        result shouldNotBe null
        result shouldBeInstanceOf TargetTestObjectWithPrivateConstructor::class
    }

    @Test
    fun validMapper_mappingElement_shouldStoreResultInCache() {

        // Arrange
        val mapper = ConcreteMapper(
                MappingDefinition(SourceTestObject::class, TargetTestObject::class)
                        .convert({ it::id }, { it::id })
                        .convert({ it::string }, { it::string }),
                MappingCache())
        val sourceObject = SourceTestObject("string", 1)

        // Act
        val result = mapper.map(sourceObject)
        val cached = mapper.mappingCache.getEntry(sourceObject, TargetTestObject::class)

        // Assert
        cached shouldNotBe null
        cached!!.value shouldEqual result
    }

    @Test
    fun validMapper_mappingElementTwice_shouldUseCache() {

        // Arrange
        val mapper = ConcreteMapper(
                MappingDefinition(SourceTestObject::class, TargetTestObject::class)
                        .convert({ it::id }, { it::id })
                        .convert({ it::string }, { it::string }),
                this.mappingCacheMock)
        val sourceObject = SourceTestObject("string", 1)

        // Act
        mapper.map(sourceObject)
        mapper.map(sourceObject)

        // Assert
        verify(this.mappingCacheMock, times(2)).createEntryIfNotExists(sourceObject, TargetTestObject::class)
    }

    @Test
    fun validMapper_mappingElementTwice_shouldEqualElementTwice() {

        // Arrange
        val mapper = ConcreteMapper(
                MappingDefinition(SourceTestObject::class, TargetTestObject::class)
                        .convert({ it::id }, { it::id })
                        .convert({ it::string }, { it::string }),
                MappingCache())
        val sourceObject = SourceTestObject("string", 1)

        // Act
        val result1 = mapper.map(sourceObject)
        val result2 = mapper.map(sourceObject)

        // Assert
        result1 shouldEqual result2
    }
}