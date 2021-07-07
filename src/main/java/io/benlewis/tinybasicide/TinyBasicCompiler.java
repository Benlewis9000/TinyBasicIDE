package io.benlewis.tinybasicide;

import javafx.scene.control.TextArea;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

public class TinyBasicCompiler {

    /**
     * Compile Tiny Basic source code into an executable program.
     * @param workspace with links to working directory and compiler executables
     * @param textAreaInfo to output compilation info to
     * @return true if compilation was successful, otherwise false
     * @throws IOException accessing files
     * @throws InterruptedException compilation processes may be interrupted
     */
    static boolean compile (Workspace workspace, TextArea textAreaInfo) throws IOException, InterruptedException {

        // Build paths based on workspace
        String cPath = workspace.getRootPath() + System.getProperty("file.separator") + "program.c";
        String tbPath = workspace.getRootPath() + System.getProperty("file.separator") + "source.tb";
        String programPath = workspace.getRootPath() + System.getProperty("file.separator") + "program.exe";

        // Tiny Basic compiler process
        Process tbCompiler = new ProcessBuilder(
                    workspace.getTbCompilerPath().toString(),
                    tbPath,
                    cPath)
                // Inherit error streams of child processes (required to prevent block if IO not read)
                .redirectErrorStream(true)
                .start();
        // Redirect IO to GUI
        IODirector.redirectIO(tbCompiler.getInputStream(), textAreaInfo);

        // Wait for process AFTER redirect
        tbCompiler.waitFor();

        // Return false if a process exited with a code other than 0 (OK)
        if (tbCompiler.exitValue() != 0) return false;

        // Gcc Processes (drop output from all processes, otherwise may bottleneck)
        Process cCompiler = new ProcessBuilder(
                workspace.getGccCompilerPath().toString(),
                "-E", cPath
        ).redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
        cCompiler.waitFor();
        if (cCompiler.exitValue() != 0) return false;
        cCompiler = new ProcessBuilder(
                workspace.getGccCompilerPath().toString(),
                "-S", cPath
        ).redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
        cCompiler.waitFor();
        if (cCompiler.exitValue() != 0) return false;
        cCompiler = new ProcessBuilder(
                workspace.getGccCompilerPath().toString(),
                "-c", cPath
        ).redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
        cCompiler.waitFor();
        if (cCompiler.exitValue() != 0) return false;

        // Gcc Linker
        cCompiler = new ProcessBuilder(
                workspace.getGccCompilerPath().toString(),
                cPath, "-o", programPath
        ).redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
        cCompiler.waitFor();
        if (cCompiler.exitValue() != 0) return false;

        // Successful compilation
        return true;
    }

    /**
     * Run an executable.
     * @param programPath path to executable file
     * @param textAreaOutput to display program output
     * @throws IOException accessing executable
     * @throws InterruptedException if process interrupted
     */
    static void run(Path programPath, TextArea textAreaOutput) throws IOException, InterruptedException {

        // Start process
        final Process program = new ProcessBuilder(
                    programPath.toString()
            ).redirectErrorStream(true).start();

        App.getInstance().setProgram(program);

        // Redirect output to text area
        IODirector.redirectIO(program.getInputStream(), textAreaOutput);

    }

    /**
     * Write a string out to file.
     * @param path to write file to
     * @param string to write in file
     * @throws FileNotFoundException if file at path not found
     */
    public static void saveStringToFile(Path path, String string) throws FileNotFoundException {

        // Get File
        File file = path.toFile();
        // Use a PrintWriter to write string to file
        PrintWriter pw = new PrintWriter(file);
        pw.print(string);
        pw.close();

    }

}
