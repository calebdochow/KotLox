package kotlox

class RuntimeError(val token: Token, message: String) : RuntimeException(message)
