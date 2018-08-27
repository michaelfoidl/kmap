/*
 * kmap
 *
 * Copyright (c) 2018, Michael Foidl.
 */

package at.michaelfoidl.kmap.test.helpers

class TargetTestObjectWithCircularReference private constructor() {
    constructor(id: Long, child: TargetTestObjectWithCircularReference? = null, parent: TargetTestObjectWithCircularReference? = null) : this() {
        this.id = id
        this.child = child
        this.parent = parent
    }

    var id: Long = -1
    var child: TargetTestObjectWithCircularReference? = null
    var parent: TargetTestObjectWithCircularReference? = null
}