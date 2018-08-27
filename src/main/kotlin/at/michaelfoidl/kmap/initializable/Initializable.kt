/*
 * kmap
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

package at.michaelfoidl.kmap.initializable


class Initializable<T : Any?>() {

    constructor(value: T?) : this() {
        initialize(value)
    }

    var isInitialized: Boolean = false
        private set

    private var initializedValue: T? = null

    var value: T? = null
        get() {
            if (this.isInitialized) {
                return this.initializedValue
            } else {
                throw UnsupportedOperationException()
            }
        }

    fun initialize(value: T?) {
        this.isInitialized = true
        this.initializedValue = value
    }
}