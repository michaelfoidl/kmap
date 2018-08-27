/*
 * kmap
 *
 * Copyright (c) 2018, Michael Foidl.
 */

package at.michaelfoidl.kmap.test.helpers


interface Echo {
    fun <T: Any> echo(value: T): T
}