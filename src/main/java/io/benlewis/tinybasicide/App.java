package io.benlewis.tinybasicide;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    public enum ProgramStatus {
        NONE, READY, COMPILING
    }

    private static App instance;

    private Stage stage;
    private Scene editorScene;
    private FXMLLoader editorLoader;
    private Process program;

    private Workspace workspace = null;

    private ProgramStatus programStatus = ProgramStatus.NONE;

    public App(){

        super();

        // Enforce singleton pattern
        if (instance != null)
            throw new UnsupportedOperationException("MainApp constructor called more than once.");
        else
            instance = this;

    }

    public static App getInstance(){
        return instance;
    }

    public FXMLLoader getEditorLoader(){
        return editorLoader;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public ProgramStatus getProgramStatus() {
        return programStatus;
    }

    public void setProgramStatus(ProgramStatus programStatus){
        this.programStatus = programStatus;
    }

    public void setProgram(Process process){
        this.program = process;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    /**
     * Kill any running program process and set the variable to null.
     * @return true if a process was found and killed
     */
    public boolean killProgram(){

        // Null check program
        if (program != null){
            // Destroy process if alive
            if (program.isAlive()) {
                program.destroy();
                return true;
            }
            program = null;
        }

        return false;
    }

    /**
     * Initialise the JavaFX GUI stage and display it.
     * @param stage to display.
     */
    public void start(Stage stage) {

        this.stage = stage;

        try {
            // Load view
            this.editorLoader = new FXMLLoader(this.getClass().getResource("/EditorView.fxml"));
            Parent editorRoot = editorLoader.load();
            this.editorScene = new Scene(editorRoot, 1200, 800);
        }
        catch (IOException e){
            // Report error and exit if view could not be loaded
            System.err.println("Failed to open EditorView.fxml.");
            e.printStackTrace(System.err);
            System.exit(1);
        }

        // Configure stage
        stage.setScene(editorScene);
        stage.setTitle("Tiny Basic IDE");
        stage.setMaxWidth(1220);
        stage.setMaxHeight(850);
        stage.setMinWidth(500);
        stage.setMinHeight(300);
        stage.show();

    }

    public static void main(String[] args){

        launch(args);
        // Destroy program if still running
        App.getInstance().killProgram();

    }

}
