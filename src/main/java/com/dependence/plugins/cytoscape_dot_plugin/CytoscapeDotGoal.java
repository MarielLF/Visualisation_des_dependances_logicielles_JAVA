package com.dependence.plugins.cytoscape_dot_plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.jgit.api.Git;

import java.io.*;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mojo(name = "generate-graph", requiresProject = false)
public class CytoscapeDotGoal extends AbstractMojo {

    @Parameter(property = "repoUrl", required = true)
    private String repoUrl;

    @Parameter(property = "outputFormat", defaultValue = "CSV")
    private String outputFormat;

    @Override
    public void execute() {
        try {
            getLog().info("========================================");
            getLog().info("       STEP 1: Preparing Directories");
            getLog().info("========================================");

            File targetDir = new File("target");
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                throw new IOException("could not create target directory: " + targetDir.getAbsolutePath());
            }

            String repoName = extractRepoName(repoUrl);
            File cloneDir = new File(targetDir, repoName + "-repo");
            File dotFile = new File(targetDir, repoName + ".dot");
            File outputFile = new File(targetDir, repoName + "." + outputFormat.toLowerCase());
            File coloredCsvFile = new File(targetDir, repoName + "-colored.csv");

            cleanDirectory(cloneDir);
            deleteFile(dotFile);
            deleteFile(outputFile);
            deleteFile(coloredCsvFile);

            getLog().info("========================================");
            getLog().info("       STEP 2: Cloning Repository");
            getLog().info("========================================");

            getLog().info("Cloning repository from: " + repoUrl);
            Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(cloneDir)
                .call();
            getLog().info("Repository cloned to: " + cloneDir.getAbsolutePath());

            getLog().info("========================================");
            getLog().info("   STEP 3: Generating Dependency Tree");
            getLog().info("========================================");

            ProcessBuilder mavenBuilder = new ProcessBuilder(
                "cmd", "/c", "mvn dependency:tree -DoutputType=dot -DoutputFile=" + dotFile.getAbsolutePath());
            mavenBuilder.directory(cloneDir);
            Process mavenProcess = mavenBuilder.start();
            captureProcessOutput(mavenProcess);

            int mavenExitCode = mavenProcess.waitFor();
            if (mavenExitCode != 0) {
                throw new IOException("Maven failed with exit code: " + mavenExitCode);
            }
            getLog().info("Dependency graph generated successfully at: " + dotFile.getAbsolutePath());

            getLog().info("========================================");
            getLog().info("       STEP 4: Converting Output");
            getLog().info("========================================");

            switch (outputFormat.toUpperCase()) {
                case "CSV":
                    convertDotToCsv(dotFile, outputFile);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported output format: " + outputFormat);
            }
            getLog().info(outputFormat + " file generated successfully at: " + outputFile.getAbsolutePath());

            // Generate the colored CSV
            getLog().info("Generating colored CSV...");
            GroupColorCsvGenerator colorCsvGenerator = new GroupColorCsvGenerator(getLog());
            colorCsvGenerator.generateColoredCsv(dotFile, coloredCsvFile);
            getLog().info("Colored CSV generated successfully at: " + coloredCsvFile.getAbsolutePath());

            getLog().info("========================================");
            getLog().info("       STEP 5: Launching Cytoscape");
            getLog().info("========================================");

            ProcessBuilder cytoscapeBuilder = new ProcessBuilder(
                "C:\\Program Files\\Cytoscape_v3.10.3\\Cytoscape.exe", "-N", coloredCsvFile.getAbsolutePath());
            cytoscapeBuilder.redirectErrorStream(true);
            Process cytoscapeProcess = cytoscapeBuilder.start();
            captureProcessOutput(cytoscapeProcess);

            int cytoscapeExitCode = cytoscapeProcess.waitFor();
            if (cytoscapeExitCode != 0) {
                throw new IOException("Cytoscape failed with exit code: " + cytoscapeExitCode);
            }
            getLog().info("Cytoscape launched successfully with the colored CSV file!");

            getLog().info("========================================");
            getLog().info("              TASK COMPLETED");
            getLog().info("========================================");
        } catch (Exception e) {
            getLog().error("Error during execution: " + e.getMessage(), e);
        }
    }

    private String extractRepoName(String repoUrl) {
        return repoUrl.substring(repoUrl.lastIndexOf('/') + 1).replace(".git", "");
    }

    private void cleanDirectory(File directory) throws IOException {
        if (directory.exists()) {
            Files.walk(directory.toPath())
                .map(java.nio.file.Path::toFile)
                .forEach(File::delete);
            directory.delete();
        }
    }

    private void deleteFile(File file) {
        if (file.exists() && !file.delete()) {
            getLog().warn("Failed to delete existing file: " + file.getAbsolutePath());
        }
    }

    private void captureProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                getLog().info(line);
            }
        }
    }

    private void convertDotToCsv(File dotFile, File csvFile) throws IOException {
        Pattern edgePattern = Pattern.compile("\"([^\"]+)\" -> \"([^\"]+)\"");

        try (BufferedReader reader = Files.newBufferedReader(dotFile.toPath());
             BufferedWriter writer = Files.newBufferedWriter(csvFile.toPath())) {

            writer.write("Source,Target\n");

            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = edgePattern.matcher(line);
                if (matcher.find()) {
                    String source = matcher.group(1);
                    String target = matcher.group(2);
                    writer.write(String.format("%s,%s\n", source, target));
                }
            }
        }
        getLog().info("CSV conversion completed. File saved at: " + csvFile.getAbsolutePath());
    }
}
