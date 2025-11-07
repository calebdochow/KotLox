SRC = src/Lox.kt src/Scanner.kt src/Token.kt src/TokenType.kt src/Parser.kt src/Expr.kt src/RuntimeError.kt src/Interpreter.kt src/tools/ASTPrinter.kt
TEST = src/Testbed.kt
OUT_DIR = out

all: run

generateast:
	mkdir -p $(OUT_DIR)
	kotlinc src/tools/GenerateAst.kt -include-runtime -d $(OUT_DIR)/generateast.jar
	java -jar $(OUT_DIR)/generateast.jar src

run: generateast
	mkdir -p $(OUT_DIR)
	kotlinc $(SRC) -include-runtime -d $(OUT_DIR)/KotLox.jar
	java -jar $(OUT_DIR)/KotLox.jar

test: generateast
	mkdir -p $(OUT_DIR)
	kotlinc $(SRC) $(TEST) -d $(OUT_DIR)
	kotlin -cp $(OUT_DIR) kotlox.TestbedKt

clean:
	rm -rf $(OUT_DIR)
