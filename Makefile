# Variables
MVN := mvn -DskipTests
MAIN := io.github.fpedrazav02.panela.Panela
JAR := target/panela.jar

.DEFAULT_GOAL := build

build:
	@$(MVN) package

rebuild:
	@$(MVN) clean package

clean:
	@$(MVN) clean

run:
	@$(MVN) exec:java -Dexec.mainClass=$(MAIN) -Dexec.args="$(ARGS)"

jar-run:
	@$(MVN) exec:java -Dexec.mainClass=$(MAIN) -Dexec.args="$(ARGS)"

test:
	@mvn test

nix-build:
	@nix build

nix-run: nix-build
	@./result/bin/panela $(ARGS)

.PHONY: build rebuild clean run jar-run test nix-build nix-run
