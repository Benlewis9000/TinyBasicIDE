package io.benlewis.tinybasicide;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class IODirector {

    /**
     * Redirect input from an InputStream to a given textarea asynchronously in realtime.
     * @param src source of streamed input
     * @param ta TextArea to output to
     */
    public static void redirectIO(InputStream src, TextArea ta) {

        App app = App.getInstance();

        EditorController controller = app.getEditorLoader().getController();

        // Delay between executor schedules
        final int DELAY = 20;
        // Max lines displayed in the TextArea at any given time
        final int LINES_TO_DISPLAY = 1000;
        // Max amount of lines to accumulate before forcing an update to output
        final int BATCH_LIMIT = 100;

        // Read in lines, store in List
        Scanner scanner = new Scanner(src);
        List<String> lines = new LinkedList<>();

        // Create execute
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        // Schedule executor with anonymous Runnable
        executor.scheduleWithFixedDelay(() -> {

            // When input exhausted
            if (!scanner.hasNextLine()){
                // Unlock buttons and shutdown executor
                controller.unlockButtons();
                executor.shutdown();
            }

            int batchSize = 0;

            // Add next line to lines
            while(scanner.hasNextLine() && batchSize < BATCH_LIMIT) {
                batchSize++;
                lines.add(scanner.nextLine());
                // If lines exceeds limit, remove oldest (first) line
                if (lines.size() > LINES_TO_DISPLAY) {
                    lines.remove(0);
                }
            }

            // Queue task to run on the JavaFX application thread
            Platform.runLater(() -> {
                ta.setText(String.join(System.lineSeparator(), lines));
                ta.setScrollTop(1000000);
            });

        }, 0, DELAY, TimeUnit.MILLISECONDS);
    }

}
