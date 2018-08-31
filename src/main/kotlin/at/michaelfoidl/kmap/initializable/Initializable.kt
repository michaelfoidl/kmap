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

package at.michaelfoidl.kmap.initializable


/**
 * Any object that is unknown when creating this instance. This enables storing references to this object although its
 * value will only be known at a later point in time.
 *
 * @since 0.1
 * @constructor Creates a new, uninitialized instance.
 */
class Initializable<T : Any?>() {

    /**
     * @constructor Creates a new, already initialized instance. If you do not have a specific reason for storing the
     * value inside an [Initializable], consider declaring it directly.
     * @param value the value this instance should be initialized with.
     */
    constructor(value: T?) : this() {
        initialize(value)
    }

    /**
     * Indicates if the instance has already been initialized or not.
     */
    var isInitialized: Boolean = false
        private set

    private var initializedValue: T? = null

    /**
     * The value stored inside this instance.
     *
     * @throws UnsupportedOperationException if this instance has not been initialized yet.
     */
    var value: T? = null
        get() {
            if (this.isInitialized) {
                return this.initializedValue
            } else {
                throw UnsupportedOperationException()
            }
        }

    /**
     * Initializes this instance with a new value. If it already is initialized, the old value is overridden.
     *
     * @param value the value this instance should be initialized with.
     */
    fun initialize(value: T?) {
        this.isInitialized = true
        this.initializedValue = value
    }
}