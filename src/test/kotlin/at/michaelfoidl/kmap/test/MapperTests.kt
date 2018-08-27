/*
 * kmap
 *
 * Copyright (c) 2018, Michael Foidl.
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