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