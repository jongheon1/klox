package lox

class LoxClass(
    val name: String,
    private val superclass: LoxClass?,
    private val methods: Map<String, LoxFunction>,
) : LoxCallable {
    fun findMethod(name: String): LoxFunction? {
        methods[name]?.let { return it }
        return superclass?.findMethod(name)
    }

    override fun toString(): String = name

    override fun call(
        interpreter: Interpreter,
        arguments: List<Any?>,
    ): Any {
        val instance = LoxInstance(this)
        // Run the constructor, bound to the new instance, if the class defines one.
        findMethod("init")?.bind(instance)?.call(interpreter, arguments)
        return instance
    }

    override fun arity(): Int = findMethod("init")?.arity() ?: 0
}
