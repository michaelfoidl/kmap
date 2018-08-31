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

package at.michaelfoidl.kmap.test

import at.michaelfoidl.kmap.definition.MappingDefinition
import at.michaelfoidl.kmap.exceptions.MappingException
import at.michaelfoidl.kmap.mapper.Mapper
import at.michaelfoidl.kmap.test.helpers.SourceTestObject
import at.michaelfoidl.kmap.test.helpers.TargetTestObject
import at.michaelfoidl.kmap.test.helpers.TargetTestObjectWithPrivateConstructor
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test


class MapperTests {

    @Test
    fun mapper_mappingWithAvailableDefinition_shouldChooseCorrectMappingDefinition() {

        // Arrange
        val mappingDefinition1 = MappingDefinition(SourceTestObject::class, TargetTestObject::class)
                .convert({ it::id }, { it::id })
                .convert({ it::string }, { it::string })

        val mappingDefinition2 = MappingDefinition(SourceTestObject::class, TargetTestObjectWithPrivateConstructor::class)
                .convert({ it::id }, { it::string }, { it.toString() })

        val mapper = object : Mapper() {
            override fun provideDefinitions(): List<MappingDefinition<*, *>> {
                return listOf(mappingDefinition1, mappingDefinition2)
            }
        }

        // Act
        val result = mapper.findDefinition(SourceTestObject::class, TargetTestObject::class)

        // Assert
        result shouldEqual mappingDefinition1
    }

    @Test
    fun validMapper_mappingWithMissingDefinition_shouldThrowException() {

        // Arrange
        val mappingDefinition1 = MappingDefinition(SourceTestObject::class, TargetTestObject::class)
                .convert({ it::id }, { it::id })
                .convert({ it::string }, { it::string })

        val mappingDefinition2 = MappingDefinition(SourceTestObject::class, TargetTestObjectWithPrivateConstructor::class)
                .convert({ it::id }, { it::string }, { it.toString() })

        val mapper = object : Mapper() {
            override fun provideDefinitions(): List<MappingDefinition<*, *>> {
                return listOf(mappingDefinition1, mappingDefinition2)
            }
        }

        // Act
        val func = { mapper.findDefinition(TargetTestObject::class, SourceTestObject::class) }

        // Assert
        func shouldThrow MappingException::class
    }
}