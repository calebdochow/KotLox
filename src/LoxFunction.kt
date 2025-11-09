package kotlox

class LoxFunction(
    private val declaration: Stmt.Function,
    private val closure: Environment,
    private val isInitializer: Boolean = false
) : LoxCallable {

    override fun arity(): Int {
        // declaration.params is an Iterable<Token> — convert to list and count
        return declaration.params.count()
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        // create a new environment whose enclosing scope is the closure
        val env = Environment(closure)
        // bind parameters
        val params = declaration.params.toList()
        for (i in params.indices) {
            env.define(params[i].lexeme, arguments[i])
        }

        try {
            // execute function body in new environment
            interpreter.executeBlock(declaration.body, env)
        } catch (r: Return) {
            // return value from the function
            return if (isInitializer) closure.get(Token(TokenType.IDENTIFIER, "this", null, 0)) else r.value
        }

        // if no explicit return, return null (or 'this' for initializers)
        return if (isInitializer) closure.get(Token(TokenType.IDENTIFIER, "this", null, 0)) else null
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }
}
