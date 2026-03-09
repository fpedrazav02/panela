# Variables
MVN := mvn -DskipTests
MAIN := io.github.fpedrazav02.panela.Panela
JAR := target/panela.jar
PANELA_DIR := $(or $(PANELA_PATH),$(HOME)/.panela)
EXAMPLES_DIR := examples

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

load-examples:
	@mkdir -p $(PANELA_DIR)/jobs
	@for example in $(EXAMPLES_DIR)/*/; do \
		name=$$(basename $$example); \
		dest=$(PANELA_DIR)/jobs/$$name; \
		echo "  loading  $$name  ->  $$dest"; \
		mkdir -p $$dest; \
		cp -r $$example. $$dest/; \
	done
	@echo ""
	@echo "Done. Run 'panela list' to see available jobs."

unload-examples:
	@for example in $(EXAMPLES_DIR)/*/; do \
		name=$$(basename $$example); \
		dest=$(PANELA_DIR)/jobs/$$name; \
		if [ -d "$$dest" ]; then \
			echo "  removing  $$name"; \
			rm -rf $$dest; \
		fi \
	done
	@echo ""
	@echo "Done."

.PHONY: build rebuild clean run jar-run test nix-build nix-run load-examples unload-examples
