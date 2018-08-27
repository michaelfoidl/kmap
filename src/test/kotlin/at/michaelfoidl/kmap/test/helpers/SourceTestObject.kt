/*
 * kmap
 *
 * Copyright (c) 2018, Michael Foidl.
 */

package at.michaelfoidl.kmap.test.helpers

class SourceTestObject private constructor() {
    constructor(string: String, id: Long): this() {
        this.string = string
        this.id = id
    }

    var id: Long = -1
    lateinit var string: String
    val immutableProperty = 111
}