package at.michaelfoidl.moody.common.mapping.test.internal.helpers

class SourceTestObject private constructor() {
    constructor(string: String, id: Long): this() {
        this.string = string
        this.id = id
    }

    var id: Long = -1
    lateinit var string: String
    val immutableProperty = 111
}