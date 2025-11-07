package kotlox

import java.io.File
import java.io.IOException

class Lox {
    companion object {
        private val interpreter = Interpreter()
        private var hadError = false
        private var hadRuntimeError = false

        @JvmStatic
        fun main(args: Array<String>) {
            when (args.size) {
                0 -> runPrompt()
                1 -> runFile(args[0])
                else -> {
                    println("Usage: kotlox [script]")
                    System.exit(64)
                }
            }
        }

        private fun runPrompt() {
            while (true) {
                print("> ")
                val line = readlnOrNull() ?: break
                run(line)
                hadError = false
            }
        }

        private fun runFile(path: String) {
            val bytes = File(path).readBytes()
            run(String(bytes))

            if (hadError) System.exit(65)
            if (hadRuntimeError) System.exit(70)
        }

        private fun run(source: String) {
            val scanner = Scanner(source)
            val tokens = scanner.scanTokens()

            val parser = Parser(tokens)
            val expression = parser.parse()

            // Stop if there was a syntax error.
            if (hadError || expression == null) return

            interpreter.interpret(expression)
        }

        fun error(line: Int, message: String) {
            report(line, "", message)
        }

        fun error(token: Token, message: String) {
            if (token.type == TokenType.EOF) {
                report(token.line, " at end", message)
            } else {
                report(token.line, " at '${token.lexeme}'", message)
            }
        }

        private fun report(line: Int, where: String, message: String) {
            System.err.println("[line $line] Error$where: $message")
            hadError = true
        }

        fun runtimeError(error: RuntimeError) {
            System.err.println("${error.message}\n[line ${error.token.line}]")
            hadRuntimeError = true
        }
    }
}
