package kotlox

import kotlox.TokenType
import kotlox.Token
import kotlox.Scanner
import kotlox.Parser
import kotlox.Stmt
import kotlox.Lox
import kotlox.tools.AstPrinter

fun main() {
    println("Extended Testbed running...\n")

    // Operator Tests
    runStatementTest("Addition", "print 2 + 3;", "5")
    runStatementTest("Subtraction", "print 10 - 4;", "6")
    runStatementTest("Multiplication", "print 6 * 7;", "42")
    runStatementTest("Division", "print 20 / 4;", "5")
    runStatementTest("Division by zero", "print 1 / 0;", "Infinity")
    runStatementTest("Unary minus", "print -42;", "-42")
    runStatementTest("Unary bang", "print !true;", "false")
    runStatementTest("Comparison greater", "print 5 > 3;", "true")
    runStatementTest("Comparison less", "print 2 < 5;", "true")
    runStatementTest("Comparison equal", "print 4 == 4;", "true")
    runStatementTest("Comparison not equal", "print 4 != 5;", "true")
    runStatementTest("Mixed addition", "print 2 + 3 * 4;", "14")
    runStatementTest("Parentheses precedence", "print (2 + 3) * 4;", "20")
    runStatementTest("String concatenation", """print "Hello " + "World";""", "Hello World")


    // Basic Statements
    runStatementTest("Variable Declaration", "var a = 123;", "")
    runStatementTest("Print Literal", "print 42;", "42")
    runStatementTest("Print Variable", "var b = 7; print b;", "7")
    runStatementTest("Unary Expression", "print -5;", "-5")
    runStatementTest("Binary Expression", "print 2 + 3 * 4;", "14")
    runStatementTest("Grouping Expression", "print (2 + 3) * 4;", "20")

    // If/Else and While
    runStatementTest("If True Branch", "if (true) print 1; else print 2;", "1")
    runStatementTest("If False Branch", "if (false) print 1; else print 2;", "2")
    runStatementTest("While Loop", "var i = 0; while (i < 3) { print i; i = i + 1; }", "0\n1\n2")

    // Functions
    runStatementTest(
        "Function Declaration and Call",
        """
        fun add(x, y) { return x + y; }
        print add(2, 3);
        """.trimIndent(),
        "5"
    )

    runStatementTest(
        "Function with local variable",
        """
        fun test() { var x = 10; return x * 2; }
        print test();
        """.trimIndent(),
        "20"
    )

    runStatementTest(
        "Nested function calls",
        """
        fun add(a, b) { return a + b; }
        fun mult(x, y) { return x * y; }
        print mult(add(2,3), 4);
        """.trimIndent(),
        "20"
    )

    // Logical expressions (not yet implemented)
    runStatementTest(
        "Logical AND/OR",
        """
        var x = true and false;
        var y = false or true;
        print x;
        print y;
        """.trimIndent(),
        "false\ntrue"
    )

    // Recursive function (factorial)
    runStatementTest(
        "Recursive function",
        """
        fun fact(n) { 
            if (n <= 1) return 1; 
            return n * fact(n - 1); 
        }
        print fact(5);
        """.trimIndent(),
        "120"
    )

    println("\nAll extended tests finished.")
}

// run statement test
private fun runStatementTest(name: String, source: String, expectedOutput: String) {
    println("Running Statement Test: $name")
    println("Input:\n$source")
    println("Expected Output:\n$expectedOutput")

    // Capture printed output
    val out = java.io.ByteArrayOutputStream()
    val originalOut = System.out
    System.setOut(java.io.PrintStream(out))

    try {
        Lox.run(source)
    } catch (e: Exception) {
        System.setOut(originalOut)
        println("Exception occurred: ${e.message}\n[FAIL]\n")
        return
    }

    System.setOut(originalOut)
    val actualOutput = out.toString().trim().replace("\r\n", "\n") // normalize newlines

    println("Actual Output:\n$actualOutput")
    if (actualOutput == expectedOutput) {
        println("[OK] Passed\n")
    } else {
        println("[FAIL] Failed\n")
    }
}
