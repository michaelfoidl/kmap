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

package at.michaelfoidl.kmap.validation


/**
 * The result of the validation of a mapping definition.
 *
 * @since 0.1
 */
class ValidationResult {
    /**
     * The list of errors that occurred during the validation.
     */
    var errors: ArrayList<String> = ArrayList()
        private set

    /**
     * The list of warnings that occurred during the validation.
     */
    var warnings: ArrayList<String> = ArrayList()
        private set

    /**
     * Indicates if any errors occurred during the validation.
     *
     * @return if any errors occurred.
     */
    fun isFailure(): Boolean {
        return !this.errors.isEmpty()
    }

    /**
     * Indicates if only warnings occurred during the validation.
     *
     * @return if only warnings occurred.
     */
    fun isWarning(): Boolean {
        return this.errors.isEmpty() && !this.warnings.isEmpty()
    }

    /**
     * Indicates if no warnings and no errors occurred during the validation.
     *
     * @return if neither warnings nor errors occurred.
     */
    fun isSuccess(): Boolean {
        return this.errors.isEmpty() && this.warnings.isEmpty()
    }

    /**
     * Adds a new error to the list of errors.
     *
     * @param errorMessage a description of the error.
     */
    internal fun addError(errorMessage: String) {
        this.errors.add("ERROR: $errorMessage")
    }

    /**
     * Adds a new warning to the list of warnings.
     *
     * @param warningMessage a description of the warning.
     */
    internal fun addWarning(warningMessage: String) {
        this.warnings.add("WARNING: $warningMessage")
    }
}