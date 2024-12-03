package com.dependence.plugins.cytoscape_dot_plugin;

import org.apache.maven.plugin.logging.Log;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupColorCsvGenerator {

    private final Log log;

    public GroupColorCsvGenerator(Log log) {
        this.log = log;
    }

    public void generateColoredCsv(File dotFile, File csvFile) throws IOException {
        Pattern edgePattern = Pattern.compile("\"([^\"]+)\" -> \"([^\"]+)\"");
        Map<String, String> nodeToGroup = new HashMap<>();
        Map<String, String> groupToColor = new HashMap<>();
        List<String[]> edges = new ArrayList<>();
        String specialColor = "#89D0F5";

        String originalGroup = null;
        String originalNode = null; 

        try (BufferedReader reader = Files.newBufferedReader(dotFile.toPath())) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = edgePattern.matcher(line);
                if (matcher.find()) {
                    String source = matcher.group(1);
                    String target = matcher.group(2);

                    if (originalNode == null) {
                        originalNode = source;
                        originalGroup = extractGroup(source);
                    }

                    nodeToGroup.putIfAbsent(source, extractGroup(source));
                    nodeToGroup.putIfAbsent(target, extractGroup(target));

                    edges.add(new String[]{source, target});
                }
            }
        }

        for (String group : new HashSet<>(nodeToGroup.values())) {
            if (!groupToColor.containsKey(group)) {
                groupToColor.put(group, generateDynamicColor(groupToColor.size()));
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(csvFile.toPath())) {
            writer.write("Source,Target,Group,Color\n");

            for (String[] edge : edges) {
                String source = edge[0];
                String target = edge[1];

                String sourceGroup = nodeToGroup.get(source);
                String targetGroup = nodeToGroup.get(target);

                String targetColor = targetGroup.equals(originalGroup) ? specialColor : groupToColor.get(targetGroup);

                writer.write(String.format("%s,%s,%s,%s\n", source, target, targetGroup, targetColor));
            }
        }

        log.info("Colored CSV generated successfully at: " + csvFile.getAbsolutePath());
    }

    private String extractGroup(String node) {
        if (node.contains(":")) {
            return node.split(":")[0];
        }
        return "default";
    }

    private String generateDynamicColor(int index) {
        float hue = (index * 137.508f) % 360;
        int rgb = java.awt.Color.HSBtoRGB(hue / 360, 0.7f, 0.9f);
        return String.format("#%06X", (0xFFFFFF & rgb));
    }
}
