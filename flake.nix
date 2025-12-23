{
  description = "Panela CLI (Java fat-jar + Maven + Nix wrapper)";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };

        jdk = pkgs.jdk21;
        jre = pkgs.jre_minimal;

        # Build JAR with Maven
        panelaJar = pkgs.stdenv.mkDerivation {
          pname = "panela-jar";
          version = "1.0";

          src = ./.;

          nativeBuildInputs = [ jdk pkgs.maven ];

          MAVEN_OPTS = "-Dmaven.repo.local=$TMPDIR/.m2";

          buildPhase = ''
            mvn -DskipTests package
          '';

          installPhase = ''
            mkdir -p $out
            cp target/panela.jar $out/panela.jar
          '';
        };

        # Create full package with bin + lib
        panela = pkgs.stdenv.mkDerivation {
          pname = "panela";
          version = "1.0";

          dontUnpack = true;

          installPhase = ''
            mkdir -p $out/bin
            mkdir -p $out/lib

            # Copy JAR to lib
            cp ${panelaJar}/panela.jar $out/lib/panela.jar

            # Create wrapper script in bin
            cat > $out/bin/panela <<EOF
            #!/usr/bin/env bash
            exec ${jre}/bin/java -jar $out/lib/panela.jar "\$@"
            EOF

            chmod +x $out/bin/panela
          '';
        };

      in
      {
        # nix build → result/bin/panela
        packages.default = panela;

        # nix run → ejecuta directamente
        apps.default = {
          type = "app";
          program = "${panela}/bin/panela";
        };

        # nix develop → shell con JDK + Maven
        devShells.default = pkgs.mkShell {
          buildInputs = [
            jdk
            pkgs.maven
          ];
        };
      }
    );
}