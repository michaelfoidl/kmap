/*
 * kmap
 *
 * Copyright (c) 2018, Michael Foidl.
 */

package at.michaelfoidl.kmap.test.helpers

class TargetTestObject() {
    constructor(string: String, id: Long, additionalProperty: String): this() {
        this.string = string
        this.id = id
        this.additionalProperty = additionalProperty
    }

    lateinit var string: String
    lateinit var additionalProperty: String
    var nullableProperty: Int? = null
    var id: Long = -1
    val immutableProperty = 42
}