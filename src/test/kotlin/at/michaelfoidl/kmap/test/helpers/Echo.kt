package at.michaelfoidl.moody.common.mapping.test.internal.helpers


interface Echo {
    fun <T: Any> echo(value: T): T
}