package ide;

import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.Buffer;
import java.nio.file.Path;
import java.util.Scanner;

public class TinyBasicCompiler {

    static boolean compile (Workspace workspace, TextArea textAreaInfo) throws IOException, InterruptedException {

        String cPath = workspace.getRootPath() + System.getProperty("file.separator") + "program.c";
        String tbPath = workspace.getRootPath() + System.getProperty("file.separator") + "source.tb";
        String programPath = workspace.getRootPath() + System.getProperty("file.separator") + "program.exe";

        Process tbCompiler = new ProcessBuilder(
                workspace.getTbCompilerPath().toString(),
                tbPath,
                cPath
            ).inheritIO().start();
        ProcessBuilder pb = new ProcessBuilder();
        redirectIO(tbCompiler.getInputStream(), textAreaInfo);
        tbCompiler.waitFor();
        if (tbCompiler.exitValue() != 0) return false;

        Process cCompiler = new ProcessBuilder(
                workspace.getGccCompilerPath().toString(),
                "-E", cPath
        ).inheritIO().start();
        cCompiler.waitFor();
        if (cCompiler.exitValue() != 0) return false;

        cCompiler = new ProcessBuilder(
                workspace.getGccCompilerPath().toString(),
                "-S", cPath
        ).start();
        redirectIO(cCompiler.getErrorStream(), textAreaInfo);
        cCompiler.waitFor();
        if (cCompiler.exitValue() != 0) return false;

        cCompiler = new ProcessBuilder(
                workspace.getGccCompilerPath().toString(),
                "-c", cPath
        ).inheritIO().start();
        cCompiler.waitFor();
        if (cCompiler.exitValue() != 0) return false;

        cCompiler = new ProcessBuilder(
                workspace.getGccCompilerPath().toString(),
                cPath, "-o", programPath
        ).inheritIO().start();
        cCompiler.waitFor();
        if (cCompiler.exitValue() != 0) return false;

        return true;
    }

    static void run(Path programPath, TextArea textAreaOutput, TextArea textAreaInfo) throws IOException, InterruptedException {

        Process program = new ProcessBuilder(
                programPath.toString()
        ).start();

        redirectIO(program.getInputStream(), textAreaOutput);
//        redirectIO(program.getErrorStream(), textAreaInfo);

//        program.waitFor();
        // TODO: set RUNNING flag? How to check when done?

    }

    private static void redirectIO(InputStream src, TextArea ta){

        /*
            TODO:
                - Does not update in realtime
                - Errors if output (src?) is too big, e.g. 1000 lines
         */

        new Thread(() -> {
            Scanner scanner = new Scanner(src);

            StringBuilder sb = new StringBuilder();

            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine() + System.getProperty("line.separator"));
                if (sb.length() > 1000){
                    ta.appendText(sb.toString());
                    sb.setLength(0);
                }
            }
            ta.appendText(sb.toString());
        }).start();

       /* new Thread(() -> {
            Scanner scanner = new Scanner(src);
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine() + System.getProperty("line.separator"));
                if(sb.length() > 500){
                    ta.appendText(sb.toString());
                    sb = new StringBuilder();
                }
            }
        }).start();*/

    }

    public static void saveStringToFile(Path path, String string) throws FileNotFoundException {

        File file = path.toFile();
        PrintWriter pw = new PrintWriter(file);
        pw.print(string);
        pw.close();

    }

}
