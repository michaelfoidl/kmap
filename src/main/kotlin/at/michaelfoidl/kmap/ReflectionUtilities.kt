/*
 * kmap
 * version 0.1.1
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

package at.michaelfoidl.kmap

import at.michaelfoidl.kmap.exceptions.MappingException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible


/**
 * Provides helper methods for working with Kotlin reflection.
 *
 * @since 0.1
 */
@PublishedApi
internal object ReflectionUtilities {

    /**
     * Checks if the [property] exists for the given [elementClass].
     *
     * @return a value indicating if the [property] exists.
     */
    fun validateThatPropertyExists(elementClass: KClass<*>, property: KProperty<*>): Boolean {
        return elementClass.memberProperties.any {
            it.name == property.name
        }
    }

    /**
     * Ensures that the [property] exists for the given [elementClass].
     *
     * @throws MappingException if the [property] does not exist.
     */
    fun ensureThatPropertyExists(elementClass: KClass<*>, property: KProperty<*>) {
        if (!validateThatPropertyExists(elementClass, property)) {
            throw MappingException("No Property '" + property.name + "' was found for class " + elementClass.qualifiedName + ".")
        }
    }

    /**
     * Checks if a primary constructor exists for the given [elementClass].
     *
     * @return a value indicating if a primary constructor exists.
     */
    fun <ElementT : Any> validateThatPrimaryConstructorExists(elementClass: KClass<ElementT>): Boolean {
        return elementClass.primaryConstructor != null
    }

    /**
     * Ensures that a primary constructor exists for the given [elementClass].
     *
     * @throws MappingException if no primary constructor exists.
     */
    fun <ElementT : Any> ensureThatPrimaryConstructorExists(elementClass: KClass<ElementT>) {
        if (!validateThatPrimaryConstructorExists(elementClass)) {
            throw MappingException("No primary constructor was found for class " + elementClass.qualifiedName + ".")
        }
    }

    /**
     * Checks if an empty primary constructor or a primary constructor with optional parameters only exists for the given [elementClass].
     *
     * @returns a value indicating if such a primary constructor exists.
     */
    fun <ElementT : Any> validateThatEmptyPrimaryConstructorExists(elementClass: KClass<ElementT>): Boolean {
        return if (validateThatPrimaryConstructorExists(elementClass)) {
            getConstructor(elementClass).parameters.all { it.isOptional }
        } else {
            false
        }
    }

    /**
     * Ensures that an empty primary constructor or a primary constructor with optional parameters only exists for the given
     * [elementClass].
     *
     * @throws MappingException if no such primary constructor exists.
     */
    fun <ElementT : Any> ensureThatEmptyPrimaryConstructorExists(elementClass: KClass<ElementT>) {
        ensureThatPrimaryConstructorExists(elementClass)
        if (!getConstructor(elementClass).parameters.all { it.isOptional }) {
            throw MappingException("Primary constructor of class " + elementClass.qualifiedName + " must not have any non-optional parameters.")
        }
    }

    /**
     * Creates a new instance of the given [elementClass] by invoking its primary constructor. This only works if the
     * constructor has no parameters or if all of them are optional.
     *
     * @return an instance of the [elementClass].
     */
    fun <ElementT : Any> createNewInstance(elementClass: KClass<ElementT>): ElementT {
        ensureThatEmptyPrimaryConstructorExists(elementClass)
        return getConstructor(elementClass).callBy(HashMap())
    }

    /**
     * Gets the primary constructor for the given [elementClass] and makes sure that it is accessible.
     *
     * @return the primary constructor of the [elementClass].
     */
    private fun <ElementT : Any> getConstructor(elementClass: KClass<ElementT>): KFunction<ElementT> {
        ensureThatPrimaryConstructorExists(elementClass)
        val constructor = elementClass.primaryConstructor!!
        constructor.isAccessible = true
        return constructor
    }
}
