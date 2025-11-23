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

        # Build-phase derivation: run Maven and produce the fat-jar
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

        # Wrapper script to run with the minimal JRE
        panelaScript = pkgs.writeShellScriptBin "panela" ''
          exec ${jre}/bin/java -jar ${panelaJar}/panela.jar "$@"
        '';

      in
      {

        ###############################
        ##  nix build → result/bin/panela
        ###############################
        packages.default = panelaScript;

        ###############################
        ##  nix run . → runs the CLI
        ###############################
        apps.default = {
          type = "app";
          program = "${panelaScript}/bin/panela";
        };

        ###############################
        ## nix develop → shell with JDK + Maven
        ###############################
        devShells.default = pkgs.mkShell {
          buildInputs = [
            jdk
            pkgs.maven
          ];
        };
      }
    );
}
