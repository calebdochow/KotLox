package kotlox

class LoxFunction(
    private val declaration: Stmt.Function,
    private val closure: Environment,
    private val isInitializer: Boolean = false
) : LoxCallable {

    override fun arity(): Int {
        return declaration.params.count()
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {

        val env = Environment(closure)

        val params = declaration.params.toList()
        for (i in params.indices) {
            env.define(params[i].lexeme, arguments[i])
        }

        try {
            interpreter.executeBlock(declaration.body, env)
        } catch (r: Return) {
            return if (isInitializer) closure.get(Token(TokenType.IDENTIFIER, "this", null, 0)) else r.value
        }

        return if (isInitializer) closure.get(Token(TokenType.IDENTIFIER, "this", null, 0)) else null
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }
}
