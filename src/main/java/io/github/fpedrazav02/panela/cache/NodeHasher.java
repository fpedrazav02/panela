package io.github.fpedrazav02.panela.cache;

import io.github.fpedrazav02.panela.PanelaHome;
import io.github.fpedrazav02.panela.dag.JobDAG;
import io.github.fpedrazav02.panela.model.Input;
import io.github.fpedrazav02.panela.model.Output;
import io.github.fpedrazav02.panela.model.Transform;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class NodeHasher {

    private final String jobName;
    private final Map<String, String> hashes = new HashMap<>();

    public NodeHasher(String jobName) {
        this.jobName = jobName;
    }

    public String hashNode(JobDAG.Node node) throws Exception {
        return switch (node.type()) {
            case INPUT     -> hashInput((Input) node.data());
            case TRANSFORM -> hashTransform((Transform) node.data());
            case OUTPUT    -> hashOutput((Output) node.data());
        };
    }

    private String hashInput(Input input) throws Exception {
        String hash = switch (input.type()) {
            case "file" -> {
                String rawPath = (String) input.config().get("path");
                Path resolved = PanelaHome.getInstance().getJobBaseDir(jobName).resolve(rawPath);
                yield sha256(Files.readAllBytes(resolved));
            }
            case "value" -> {
                String data = String.valueOf(input.config().get("data"));
                String type = String.valueOf(input.config().get("type"));
                yield sha256(("value:" + data + ":" + type).getBytes());
            }
            default -> sha256(("input:" + input.type() + ":" + input.config()).getBytes());
        };
        hashes.put(input.name(), hash);
        return hash;
    }

    private String hashTransform(Transform transform) throws Exception {
        String upstream = hashes.getOrDefault(transform.from(), "");
        String raw = "transform:" + upstream + ":" + configString(transform.type(), transform.config());
        String hash = sha256(raw.getBytes());
        hashes.put(transform.name(), hash);
        return hash;
    }

    private String hashOutput(Output output) throws Exception {
        String upstream = hashes.getOrDefault(output.from(), "");
        String raw = "output:" + upstream + ":" + configString(output.type(), output.config());
        String hash = sha256(raw.getBytes());
        hashes.put(output.name(), hash);
        return hash;
    }

    private String configString(String type, Map<String, Object> config) {
        StringBuilder sb = new StringBuilder(type).append(":");
        config.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> sb.append(e.getKey()).append("=").append(e.getValue()).append(";"));
        return sb.toString();
    }

    private String sha256(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(data);
        StringBuilder hex = new StringBuilder();
        for (byte b : digest) hex.append(String.format("%02x", b));
        return hex.toString();
    }
}
