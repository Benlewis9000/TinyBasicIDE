package io.benlewis.tinybasicide;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class EditorController implements Initializable {

    @FXML
    private TextField textFieldWorkingDir;
    @FXML
    private TextField textFieldGccCompiler;
    @FXML
    private TextField textFieldTbCompiler;
    @FXML
    private Button buttonCompile;
    @FXML
    private Button buttonRun;
    @FXML
    private Button buttonLoad;
    @FXML
    private Button buttonSave;
    @FXML
    private Button buttonKill;
    @FXML
    private TextArea textAreaSource;
    @FXML
    private TextArea textAreaProgram;
    @FXML
    private TextArea textAreaInfo;
    @FXML
    private Text textFeedback;

    /**
     * Initialise a controller and its components.
     * @param url The location used to resolve relative paths for the root object, or null if the location is not known
     * @param resourceBundle The resources used to localize the root object, or null if the root object was not localized
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // TODO: delete, for faster testing
        textFieldWorkingDir.setText("C:\\Users\\benja\\Desktop\\JnaTest\n");
       /* textFieldGccCompiler.setText("C:\\Qt\\Tools\\mingw810_64\\bin\\x86_64-w64-mingw32-gcc.exe");
        textFieldTbCompiler.setText("C:\\Users\\benja\\source\\repos\\TinyBASIC_Compiler\\x64\\Debug\\TinyBASIC_Compiler.exe");*/

        // Load monospaced font
        Font mono = Font.loadFont(App.class.getResourceAsStream("/JetBrainsMono-Regular.ttf"), 16);
        // Configure text areas
        textAreaSource.setFont(mono);
        textAreaProgram.setEditable(false);
        textAreaProgram.setFont(mono);
        textAreaInfo.setEditable(false);
        textAreaInfo.setFont(mono);

    }

    /**
     * Disable the Compile and Run buttons.
     */
    public void lockButtons(){
        Platform.runLater(() -> {
            buttonCompile.setDisable(true);
            buttonRun.setDisable(true);
        });
    }

    /**
     * Enable the Compile and Run buttons.
     */
    public void unlockButtons(){
        Platform.runLater(() -> {
            buttonCompile.setDisable(false);
            buttonRun.setDisable(false);
        });
    }

    /**
     * Attempt to compile tiny basic source in IDE.
     */
    @FXML
    void buttonCompileAction(){

        // Lock sensitive buttons on GUI
        lockButtons();

        // Get app instance
        App app = App.getInstance();
        // Set program status
        app.setProgramStatus(App.ProgramStatus.COMPILING);

        // Clear all text areas
        resetFeedback();
        textAreaProgram.setText("");
        textAreaInfo.setText("");

        // Ensure workspace initialised and up to date
        Path workingDir = Paths.get(textFieldWorkingDir.getText());
        Path gccCompiler = Paths.get(textFieldGccCompiler.getText());
        Path tbCompiler = Paths.get(textFieldTbCompiler.getText());
        String tbSource = textAreaSource.getText();
        app.setWorkspace(new Workspace(workingDir, gccCompiler, tbCompiler, tbSource));

        try {
            // Save tiny basic source to file
            TinyBasicCompiler.saveStringToFile(
                    Paths.get(app.getWorkspace().getRootPath().toString(), "source.tb"),
                    app.getWorkspace().getTbSource()
            );
        }
        catch (IOException e){
            errorFeedback("ERROR: Compilation failed. Could not save source code (IOException).");
            unlockButtons();
            return;
        }

        try {
            // Pass files to TB compiler
            if (TinyBasicCompiler.compile(
                        app.getWorkspace(),
                        this.textAreaInfo
                    )) {
                // Update program status
                app.setProgramStatus(App.ProgramStatus.READY);
                Platform.runLater(() -> {
                    textAreaInfo.appendText(System.lineSeparator() + "Executable compiled successfully." + System.lineSeparator());
                });
            }
            else {
                errorFeedback("ERROR: Compilation failed.");
                unlockButtons();
            }
        }
        catch (IOException e){
            errorFeedback("ERROR: Compilation failed (IOException).");
            app.setProgramStatus(App.ProgramStatus.NONE);
            System.err.printf("IOException during compilation. Cancelling.\n%s\n", e.toString());
            unlockButtons();
        }
        catch (InterruptedException e){
            errorFeedback("ERROR: Compilation failed (InterruptedException).");
            app.setProgramStatus(App.ProgramStatus.NONE);
            System.err.printf("Compilation unexpectedly interrupted. Cancelling.\n%s\n", e.toString());
            unlockButtons();
        }

    }

    /**
     * Attempt to run tiny basic program.
     */
    @FXML
    void buttonRunAction(){

        // Lock sensitive buttons on GUI
        lockButtons();

        // Reset text areas
        resetFeedback();
        textAreaProgram.setText("");

        App app = App.getInstance();

        if (app.getProgramStatus() == App.ProgramStatus.READY) {

            try {
                // Get path to program executable
                Path program = Paths.get(app.getWorkspace().getRootPath().toString(), "program.exe");
                // Run executable
                TinyBasicCompiler.run(program, textAreaProgram);

            } catch (IOException e) {
                errorFeedback("ERROR: Could open program.");
                System.err.printf("Could not open program.\n%s\n", e.toString());
                unlockButtons();
            } catch (InterruptedException e) {
                errorFeedback("ERROR: Program execution interrupted.");
                System.err.printf("Program unexpectedly interrupted.\n%s\n", e.toString());
                unlockButtons();
            }

        }
        else {
            textFeedback.setText("Please compile your program before running it.");
            unlockButtons();
        }

    }

    /**
     * Attempt to load a workspace from the path provided in the text field.
     */
    @FXML
    void buttonLoadAction(){

        resetFeedback();

        // Get path to workspace
        String pathStr = textFieldWorkingDir.getText();

        // Ensure path is not blank
        if (pathStr.isEmpty()){

            textFeedback.setText("Please provide a path for the working directory.");

        }
        else {

            // Try to load workspace
            try {
                Workspace workspace = Workspace.load(Paths.get(pathStr));
                App.getInstance().setWorkspace(workspace);
                // Update GUI
                textFieldGccCompiler.setText(workspace.getGccCompilerPath().toString());
                textFieldTbCompiler.setText(workspace.getTbCompilerPath().toString());
                textAreaSource.setText(workspace.getTbSource());
                textFeedback.setText("Loaded workspace.");
            }
            catch (IOException e){
                errorFeedback("ERROR: Failed to load workspace.");
                System.err.printf("Failed to load a workspace in \"%s\".\n%s\n", pathStr, e.toString());
            }

        }

    }

    /**
     * Attempt to save current IDE workspace to file.
     */
    @FXML
    void buttonSaveAction(){

        resetFeedback();

        // Get user input
        Path workingDir = Paths.get(textFieldWorkingDir.getText());
        Path gccCompiler = Paths.get(textFieldGccCompiler.getText());
        Path tbCompiler = Paths.get(textFieldTbCompiler.getText());
        String tbSource = textAreaSource.getText();

        // Construct workspace
        Workspace workspace = new Workspace(workingDir, gccCompiler, tbCompiler, tbSource);

        try {
            // Try to save workspace
            workspace.save(workingDir);
            App.getInstance().setWorkspace(workspace);
            textFeedback.setText("Saved workspace.");
        }
        catch(IOException e){
            textFeedback.setFill(Color.RED);
            textFeedback.setText("ERROR: Failed to save workspace.");
            System.err.printf("Failed to save a workspace to \"%s\".\n%s\n", workingDir.toString(), e.toString());
        }

    }

    /**
     * Attempt to kill any running program process on the applications main instance.
     */
    @FXML
    void buttonKillAction(){

        if (App.getInstance().killProgram()) {
            textFeedback.setText("Successfully killed program.");
        }
        else {
            textFeedback.setText("Could not find a program to kill.");
        }

    }

    /**
     * Reset the feedback text component.
     */
    public void resetFeedback(){
        textFeedback.setFill(Color.BLACK);
        textFeedback.setText("");
    }

    /**
     * Display an error message on the text component.
     * @param error message to display
     */
    public void errorFeedback(String error){
        textFeedback.setFill(Color.RED);
        textFeedback.setText(error);
    }

}
