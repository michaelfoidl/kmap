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