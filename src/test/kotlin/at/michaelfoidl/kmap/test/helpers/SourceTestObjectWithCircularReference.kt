package at.michaelfoidl.moody.common.mapping.test.internal.helpers

class SourceTestObjectWithCircularReference private constructor() {
    constructor(id: Long, child: SourceTestObjectWithCircularReference? = null, parent: SourceTestObjectWithCircularReference? = null): this() {
        this.id = id
        this.child = child
        this.parent = parent
    }

    var id: Long = -1
    var child: SourceTestObjectWithCircularReference? = null
    var parent: SourceTestObjectWithCircularReference? = null
}