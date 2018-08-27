/*
 * kmap
 *
 * Copyright (c) 2018, Michael Foidl.
 */

package at.michaelfoidl.kmap.integrationTest

import at.michaelfoidl.kmap.definition.MappingDefinition
import at.michaelfoidl.kmap.mapper.Mapper
import at.michaelfoidl.kmap.test.helpers.SourceTestObject
import at.michaelfoidl.kmap.test.helpers.SourceTestObjectWithCircularReference
import at.michaelfoidl.kmap.test.helpers.TargetTestObject
import at.michaelfoidl.kmap.test.helpers.TargetTestObjectWithCircularReference
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.junit.jupiter.api.Test


class MappingTests {

    @Test
    fun validMapper_mappingWithAvailableDefinition_shouldBeSuccessful() {

        // Arrange
        val mapper = object : Mapper() {
            override fun provideDefinitions(): List<MappingDefinition<*, *>> {
                return listOf(
                        MappingDefinition(SourceTestObject::class, TargetTestObject::class)
                                .convert({ it::id }, { it::id })
                                .convert({ it::string }, { it::string }),
                        MappingDefinition(TargetTestObject::class, SourceTestObject::class)
                                .convert({ it::additionalProperty }, { it::string })
                )
            }

        }
        val sourceObject1 = SourceTestObject("string", 1)
        val sourceObject2 = TargetTestObject("string", 0, "additional")

        // Act
        val result1 = mapper.map<TargetTestObject>(sourceObject1)
        val result2 = mapper.map<SourceTestObject>(sourceObject2)

        // Assert
        result1 shouldNotBe null
        result1 shouldBeInstanceOf TargetTestObject::class
        result1.string shouldEqual "string"
        result1.id shouldEqual 1

        result2 shouldNotBe null
        result2.string shouldEqual "additional"
    }

    @Test
    fun validMapper_mappingCircularReferenceWithOneMapper_shouldBeSuccessful() {

        // Arrange
        val mapper = object : Mapper() {
            override fun provideDefinitions(): List<MappingDefinition<*, *>> {
                return listOf(
                        MappingDefinition(SourceTestObjectWithCircularReference::class, TargetTestObjectWithCircularReference::class)
                                .convert({ it::id }, { it::id })
                                .map({ it::child },
                                        { it::child },
                                        this)
                                .map({ it::parent },
                                        { it::parent },
                                        this)
                )
            }
        }

        val parentObject = SourceTestObjectWithCircularReference(1)
        val childObject = SourceTestObjectWithCircularReference(2)
        childObject.parent = parentObject
        parentObject.child = childObject

        // Act
        val result = mapper.map<TargetTestObjectWithCircularReference>(parentObject)

        // Assert
        result.parent shouldBe null
        result.child shouldNotBe null
        result.child!!.child shouldBe null
        result.child!!.parent shouldEqual result
    }

    @Test
    fun validMapper_mappingCircularReferenceWithSeveralMappers_shouldBeSuccessful() {

        // Arrange
        lateinit var mapper1: Mapper
        lateinit var mapper2: Mapper

        mapper1 = object : Mapper() {
            override fun provideDefinitions(): List<MappingDefinition<*, *>> {
                return listOf(
                        MappingDefinition(SourceTestObjectWithCircularReference::class, TargetTestObjectWithCircularReference::class)
                                .convert({ it::id }, { it::id })
                                .map({ it::child },
                                        { it::child },
                                        mapper2)
                                .map({ it::parent },
                                        { it::parent },
                                        mapper2)
                )
            }
        }

        mapper2 = object : Mapper() {
            override fun provideDefinitions(): List<MappingDefinition<*, *>> {
                return listOf(
                        MappingDefinition(SourceTestObjectWithCircularReference::class, TargetTestObjectWithCircularReference::class)
                                .convert({ it::id }, { it::id })
                                .map({ it::child },
                                        { it::child },
                                        mapper1)
                                .map(
                                        { it::parent },
                                        { it::parent },
                                        mapper1)
                )
            }
        }

        val parentObject = SourceTestObjectWithCircularReference(1)
        val childObject = SourceTestObjectWithCircularReference(2)
        childObject.parent = parentObject
        parentObject.child = childObject

        // Act
        val result = mapper1.map<TargetTestObjectWithCircularReference>(parentObject)

        // Assert
        result.parent shouldBe null
        result.child shouldNotBe null
        result.child!!.child shouldBe null
        result.child!!.parent shouldEqual result
    }
}