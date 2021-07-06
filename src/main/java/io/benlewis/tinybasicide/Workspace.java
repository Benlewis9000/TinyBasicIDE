package io.benlewis.tinybasicide;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Workspace {

    private String rootPath;
    private String gccCompilerPath;
    private String tbCompilerPath;
    private String tbSource;

    public Path getRootPath() {
        return Paths.get(rootPath);
    }

    public void setRootPath(Path rootPath) {
        this.rootPath = rootPath.toString();
    }

    public Path getGccCompilerPath() {
        return Paths.get(gccCompilerPath);
    }

    public void setGccCompilerPath(Path gccCompilerPath) {
        this.gccCompilerPath = gccCompilerPath.toString();
    }

    public Path getTbCompilerPath() {
        return Paths.get(tbCompilerPath);
    }

    public void setTbCompilerPath(Path tbCompilerPath) {
        this.tbCompilerPath = tbCompilerPath.toString();
    }

    public String getTbSource() {
        return tbSource;
    }

    public void setTbSource(String tbSource){
        this.tbSource = tbSource;
    }

    /**
     * Workspace constructor.
     * @param rootPath path to root directory of workspace where files will be stored
     * @param gccCompilerPath path to GCC compiler
     * @param tbCompilerPath path to Tiny Basic compiler
     * @param tbSource tiny basic source code
     */
    public Workspace(Path rootPath, Path gccCompilerPath, Path tbCompilerPath, String tbSource){

        this.rootPath = rootPath.toString();
        this.gccCompilerPath = gccCompilerPath.toString();
        this.tbCompilerPath = tbCompilerPath.toString();
        this.tbSource = tbSource;

    }

    /**
     * Save a workspace configuration.
     * @param rootPath of workspace where config will be saved
     * @throws IOException writing file
     */
    public void save(Path rootPath) throws IOException {

        // Convert workspace to Json
        Gson gson = new Gson();
        String json = gson.toJson(this);

        // Get config file
        Path config = Paths.get(rootPath.toString(), "config.json");
        File file = config.toFile();
        if (!file.exists()) {
            rootPath.toFile().mkdirs();
            file.createNewFile();
        }

        // Print json to file
        PrintWriter pw = new PrintWriter(file);
        pw.print(json);
        pw.close();

    }

    /**
     * Load a Workspace from a json file.
     * @param rootPath path to root directory of workspace
     * @return workspace loaded
     * @throws IOException scanning path to json file
     */
    public static Workspace load(Path rootPath) throws IOException {

        // Path to config
        Path config = Paths.get(rootPath.toString(), "config.json");

        // Scan config
        Scanner scanner = new Scanner(config);

        // Build string of config contents
        StringBuilder sb = new StringBuilder();

        while(scanner.hasNextLine())
            sb.append(scanner.nextLine() + System.getProperty("line.separator"));

        scanner.close();

        // Parse json to Workspace
        Gson gson = new Gson();
        Workspace workspace = gson.fromJson(sb.toString(), Workspace.class);

        return workspace;

    }

}
