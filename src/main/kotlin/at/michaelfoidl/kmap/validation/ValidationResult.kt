/*
 * kmap
 *
 * Copyright (c) 2018, Michael Foidl.
 */

package at.michaelfoidl.kmap.validation


class ValidationResult {
    var errors: ArrayList<String> = ArrayList()
        private set
    var warnings: ArrayList<String> = ArrayList()
        private set

    fun isFailure(): Boolean {
        return !this.errors.isEmpty()
    }

    fun isWarning(): Boolean {
        return this.errors.isEmpty() && !this.warnings.isEmpty()
    }

    fun isSuccess(): Boolean {
        return this.errors.isEmpty() && this.warnings.isEmpty()
    }

    fun addError(errorMessage: String) {
        this.errors.add("ERROR: $errorMessage")
    }

    fun addWarning(warningMessage: String) {
        this.warnings.add("WARNING: $warningMessage")
    }
}