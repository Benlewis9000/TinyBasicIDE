package ide;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    public enum ProgramStatus {
        NONE, READY, COMPILING, RUNNING
    }

    private static App instance;

    private Stage stage;
    private Scene editorScene;

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

    public Workspace getWorkspace() {
        return workspace;
    }

    public ProgramStatus getProgramStatus() {
        return programStatus;
    }

    public void setProgramStatus(ProgramStatus programStatus){
        this.programStatus = programStatus;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public void start(Stage stage) {

        this.stage = stage;

        try {
            // Load view
            Parent editorRoot = FXMLLoader.load(this.getClass().getResource("/EditorView.fxml"));
            this.editorScene = new Scene(editorRoot, 1200, 800);
        }
        catch (IOException e){
            // Report error and exit if view could not be loaded
            System.err.println("Failed to open EditorView.fxml.");
            e.printStackTrace(System.err);
            System.exit(1);
        }

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

    }

}
