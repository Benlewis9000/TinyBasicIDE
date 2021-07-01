package ide;

import javafx.event.ActionEvent;
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
    private TextArea textAreaSource;
    @FXML
    private TextArea textAreaProgram;
    @FXML
    private TextArea textAreaInfo;
    @FXML
    private Text textFeedback;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // TODO: delete, for faster testing
        textFieldWorkingDir.setText("C:\\Users\\benja\\Desktop\\JnaTest\n");
        textFieldGccCompiler.setText("C:\\Qt\\Tools\\mingw810_64\\bin\\x86_64-w64-mingw32-gcc.exe");
        textFieldTbCompiler.setText("C:\\Users\\benja\\source\\repos\\TinyBASIC_Compiler\\x64\\Debug\\TinyBASIC_Compiler.exe");

        Font mono = Font.loadFont(App.class.getResourceAsStream("/JetBrainsMono-Regular.ttf"), 16);
        textAreaSource.setFont(mono);
        textAreaProgram.setEditable(false);
        textAreaProgram.setStyle("-fx-opacity: 1;");
        textAreaProgram.setFont(mono);
        textAreaInfo.setEditable(false);
        textAreaInfo.setStyle("-fx-opacity: 1;");
        textAreaInfo.setFont(mono);

    }

    @FXML
    void buttonCompileAction(ActionEvent event){

        System.out.println("buttonCompilePressed");

        App app = App.getInstance();
        app.setProgramStatus(App.ProgramStatus.COMPILING);

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
            TinyBasicCompiler.saveStringToFile(
                    Paths.get(app.getWorkspace().getRootPath().toString(), "source.tb"),
                    app.getWorkspace().getTbSource()
            );
        }
        catch (IOException e){
            errorFeedback("ERROR: Compilation failed. Could not save source code (IOException).");
        }

        try {

            // Pass files to TB compiler
            if (TinyBasicCompiler.compile(
                        app.getWorkspace(),
                        this.textAreaInfo
                    )) {
                app.setProgramStatus(App.ProgramStatus.READY);
            }

        }
        catch (IOException e){
            errorFeedback("ERROR: Compilation failed (IOException).");
            app.setProgramStatus(App.ProgramStatus.NONE);
            System.err.printf("IOException during compilation. Cancelling.\n%s\n", e.toString());
        }
        catch (InterruptedException e){
            errorFeedback("ERROR: Compilation failed (InterruptedException).");
            app.setProgramStatus(App.ProgramStatus.NONE);
            System.err.printf("Compilation unexpectedly interrupted. Cancelling.\n%s\n", e.toString());
        }

    }

    @FXML
    void buttonRunAction(){

        resetFeedback();
        textAreaProgram.setText("");

        App app = App.getInstance();

        if (App.getInstance().getProgramStatus() == App.ProgramStatus.READY) {

            try {

                Path program = Paths.get(App.getInstance().getWorkspace().getRootPath().toString(), "program.exe");
                TinyBasicCompiler.run(program, textAreaProgram, textAreaInfo);

            } catch (IOException e) {
                errorFeedback("ERROR: Could open program.");
                System.err.printf("Could not open program.\n%s\n", e.toString());
            } catch (InterruptedException e) {
                errorFeedback("ERROR: Program execution interrupted.");
                System.err.printf("Program unexpectedly interrupted.\n%s\n", e.toString());
            }

        }
        else {
            textFeedback.setText("Please compile your program before running it.");
        }

    }

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

    @FXML
    boolean buttonSaveAction(){

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
            return true;
        }
        catch(IOException e){
            textFeedback.setFill(Color.RED);
            textFeedback.setText("ERROR: Failed to save workspace.");
            System.err.printf("Failed to save a workspace to \"%s\".\n%s\n", workingDir.toString(), e.toString());
            return false;
        }

    }

    public void resetFeedback(){
        textFeedback.setFill(Color.BLACK);
        textFeedback.setText("");
    }

    public void errorFeedback(String error){
        textFeedback.setFill(Color.RED);
        textFeedback.setText(error);
    }

}
