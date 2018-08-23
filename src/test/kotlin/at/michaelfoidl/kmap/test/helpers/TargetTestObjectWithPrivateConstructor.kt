package at.michaelfoidl.moody.common.mapping.test.internal.helpers

class TargetTestObjectWithPrivateConstructor private constructor() {
    constructor(string: String, id: Long) : this() {
        this.string = string
        this.id = id
    }

    lateinit var string: String
    var id: Long = -1
}