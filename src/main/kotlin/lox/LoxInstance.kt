package lox

class LoxInstance(
    private val klass: LoxClass,
) {
    private val fields = mutableMapOf<String, Any?>()

    fun get(name: Token): Any? {
        if (fields.containsKey(name.lexeme)) {
            return fields[name.lexeme]
        }

        // Fields shadow methods; a method access binds `this` to this instance.
        val method = klass.findMethod(name.lexeme)
        if (method != null) return method.bind(this)

        throw RuntimeError(name, "Undefined property '${name.lexeme}'.")
    }

    fun set(
        name: Token,
        value: Any?,
    ) {
        fields[name.lexeme] = value
    }

    override fun toString(): String = "${klass.name} instance"
}
