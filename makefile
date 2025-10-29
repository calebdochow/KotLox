KOTLIN_SRC = $(wildcard src/*.kt)
OUT = kotlox.jar

build:
	kotlinc $(KOTLIN_SRC) -include-runtime -d $(OUT)

run: build
	java -jar $(OUT)

clean:
	rm -f $(OUT)
