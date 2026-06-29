package calc.graphcalc;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;


public class GUI extends Application {

    public static TextField inputField;

    static Canvas canvas = new Canvas();
    static GraphicsContext gc = canvas.getGraphicsContext2D();

    // These are for bad text inputs
    static StackPane suggestionPopup;
    static Label suggestionLabel;

    // Zoom scale
    static double scale = 25;
    static double squareSize = scale;

    // Pan offsets
    static double offsetX = 0;
    static double offsetY = 0;

    // Mouse tracking
    static double lastMouseX;
    static double lastMouseY;

    public static double getCenterX() {
        return Math.round(canvas.getWidth() / 2 + offsetX) + 0.5;
    }

    public static double getCenterY() {
        return Math.round(canvas.getHeight() / 2 + offsetY) + 0.5;
    }


    @Override
    public void start(Stage stage) {


        StackPane root = new StackPane();

        // Top bar
        inputField = new TextField();
        inputField.setPrefWidth(400);

        Button b = new Button("Enter");
        b.setDefaultButton(true);
        b.setOnAction(_ -> InputHandler.updateInput());

        // Bar for the input field and enter button
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));
        topBar.getChildren().addAll(inputField, b);
        topBar.setStyle("-fx-background-color: rgba(0, 0, 0, 0)");

        topBar.setMaxHeight(HBox.USE_PREF_SIZE);

        StackPane.setAlignment(topBar, Pos.TOP_LEFT);
        root.getChildren().addAll(canvas, topBar);


        // Resizeable canvas
        canvas.widthProperty().bind(root.widthProperty());

        canvas.heightProperty().bind(root.heightProperty().subtract(topBar.heightProperty()));

        canvas.widthProperty().addListener(_ -> redraw());

        canvas.heightProperty().addListener(_ -> redraw());

        inputField.setPrefWidth(400);

        // Detect input field focus
        inputField.textProperty().addListener((_, _, _) -> {
            int xOffset = 0;
            int yOffset = 50;
            showSuggestion("""
                            Syntax:
                            Multiplication: a*b
                            Division: a/b (only simple division implemented)
                            Trig functions:
                            Sin(a), Cos(a)
                            tg(a), ctg(a)
                            """,
                    false, xOffset, yOffset);
        });

        // Panning
        canvas.setOnMousePressed((MouseEvent event) -> {
            lastMouseX = event.getX();
            lastMouseY = event.getY();
        });

        canvas.setOnMouseDragged((MouseEvent event) -> {

            double moveX = event.getX() - lastMouseX;
            double moveY = event.getY() - lastMouseY;

            offsetX += moveX;
            offsetY += moveY;

            lastMouseX = event.getX();
            lastMouseY = event.getY();

            redraw();
        });


        // Scene stuff
        Scene scene = getScene(root);
        initSuggestionPopup(root);
        stage.setTitle("Graph Calculator");
        stage.setScene(scene);
        stage.show();

        redraw();
    }

    private void initSuggestionPopup(StackPane root) {

        // Readies a rounded, invisible popup
        suggestionLabel = new Label();

        suggestionLabel.setStyle(
                "-fx-background-color: rgba(0,0,0,0.85);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 8 12 8 12;" +
                        "-fx-background-radius: 14;" +
                        "-fx-font-size: 14px;"
        );

        suggestionPopup = new StackPane(suggestionLabel);

        suggestionPopup.setVisible(false);

        root.getChildren().add(suggestionPopup);
    }

    public static void showSuggestion(String message, boolean timeOut, int extraXOffset, int extraYOffset) {

        suggestionLabel.setText(message);

        suggestionPopup.setOpacity(1);
        suggestionPopup.setVisible(true);

        // Position directly under text field
        double x = inputField.localToScene(0, 0).getX();
        double y = inputField.localToScene(0, 0).getY();

        suggestionPopup.setTranslateX(x - (canvas.getWidth() / 2) + 200 + extraXOffset);

        suggestionPopup.setTranslateY(y - (canvas.getHeight() / 2) + 45 + extraYOffset);
        if (timeOut) {
            FadeTransition fade = new FadeTransition(Duration.seconds(3), suggestionPopup);

            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            fade.setOnFinished(_ -> suggestionPopup.setVisible(false));

            fade.play();
        }
    }

    private static Scene getScene(StackPane root) {

        Scene scene = new Scene(root, 900, 900);

        // Zoom
        canvas.setOnScroll(event -> {

            double delta = event.getDeltaY();

            // Fallback for laptop or other weird scroll inputs
            if (delta == 0) {
                delta = event.getTextDeltaY();
            }

            double zoomFactor = 1.1;
            double oldScale = scale;

            // Simple zoom logic
            if (delta > 0) {
                scale *= zoomFactor;
            } else if (delta < 0) {
                scale /= zoomFactor;
            }
            // Forces the scale to be within reasonable bounds, default is 25px width for each square in the grid
            scale = Math.clamp(scale, 5, 500);

            double scaleChange = scale / oldScale;
            // Offset logic, used by other methods
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            double centerX = width / 2 + offsetX;
            double centerY = height / 2 + offsetY;

            double mouseX = event.getX();
            double mouseY = event.getY();

            offsetX -= (mouseX - centerX) * (scaleChange - 1);
            offsetY -= (mouseY - centerY) * (scaleChange - 1);

            squareSize = scale;
            redraw();
        });

        return scene;
    }

    public static void clear() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        gc.clearRect(0, 0, width, height);
    }

    public static void redraw() {

        clear();
        drawGrid();

        if (Equation.hasEquation) {
            Equation.redraw();
        }
    }

    private static void drawGrid() {

        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // Background
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);

        // Center with panning
        double centerX = width / 2 + offsetX;
        double centerY = height / 2 + offsetY;

        double gridSize = scale;

        // Grid
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);

        // Vertical lines
        for (double x = centerX; x < width; x += gridSize) {
            gc.strokeLine(x, 0, x, height);
        }
        for (double x = centerX; x > 0; x -= gridSize) {
            gc.strokeLine(x, 0, x, height);
        }

        // Horizontal lines
        for (double y = centerY; y < height; y += gridSize) {
            gc.strokeLine(0, y, width, y);
        }

        for (double y = centerY; y > 0; y -= gridSize) {
            gc.strokeLine(0, y, width, y);
        }

        // Axes
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        // X-axis
        gc.strokeLine(0, centerY, width, centerY);

        // Y-axis
        gc.strokeLine(centerX, 0, centerX, height);
    }

    public static void setLineColor(Color color) {
        gc.setStroke(color);
    }

    public static void drawLine(Double xStart, Double yStart, Double xEnd, Double yEnd) {
        gc.strokeLine(xStart, yStart, xEnd, yEnd);
    }
}