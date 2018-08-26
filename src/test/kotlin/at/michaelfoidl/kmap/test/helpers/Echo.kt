package at.michaelfoidl.kmap.test.helpers


interface Echo {
    fun <T: Any> echo(value: T): T
}