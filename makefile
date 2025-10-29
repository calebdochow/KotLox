KOTLIN_SRC = src/Lox.kt src/Scanner.kt src/Token.kt src/TokenType.kt
OUT = out/KotLox.jar

all:
	mkdir -p out
	kotlinc $(KOTLIN_SRC) -include-runtime -d $(OUT)

run: all
	java -jar $(OUT)

clean:
	rm -rf out
