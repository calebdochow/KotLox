package kotlox

import java.io.File
import java.io.IOException

class Lox{
    companion object{
        private var hadError = false

        @JvmStatic
        fun main(args: Array<String>){
            when(args.size){
                0 -> runPrompt()
                1 -> runFile(args[0]) 
                else -> {
                    println("Usage: kotlox [script]")
                    System.exit(64)
                }
            }
        }

        private fun runPrompt(){
            while(true){
                print("> ")
                val line = readlnOrNull() ?: break
                run(line)
                hadError = false
            }
        }

        private fun run(source: String){
            val scanner = Scanner(source)
            val tokens = scanner.scanTokens()

            for (token in tokens){
                println(token)
            }
        }

        private fun runFile(path: String){
            val bytes = File(path).readBytes()
            run(String(bytes))
            if (hadError) System.exit(65)
        }

        fun error(line: Int, message: String){
            report(line, "", message)
        }

        private fun report(line: Int, where: String, message: String){
            System.err.println("[line $line] Error$where: $message")
            hadError = true
        }
    }
}