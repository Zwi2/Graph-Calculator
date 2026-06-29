package calc.graphcalc;

public class InputHandler {

    public static String input;
    public static void updateInput() {

        input = GUI.inputField.getText();
        GUI.inputField.setText("");

        if (input == null) {
            return;
        }

        String cleaned = input.replaceAll("\\s+", "");
        cleaned = cleaned.toLowerCase();

        boolean timeOut = true;
        int extraXOffset = 0;
        int extraYOffset = 0;

        if (cleaned.startsWith("y=")) {
            cleaned = cleaned.substring(2);
        }

        // Prevent multiple equals signs
        if (cleaned.chars().filter(ch -> ch == '=').count() > 1) {

            GUI.showSuggestion("Too many '=' symbols.", timeOut, extraXOffset, extraYOffset);
            return;
        }

        // Prevent empty input
        if (cleaned.isEmpty()) {

            GUI.showSuggestion("Equation is empty.", timeOut, extraXOffset, extraYOffset);
            return;
        }

        // Prevent equation without x
        if (!cleaned.contains("x")) {

            GUI.showSuggestion("Equation must contain x.", timeOut, extraXOffset, extraYOffset);
            return;
        }
        if (cleaned.contains("xx")) {
            GUI.showSuggestion("'xx' is disallowed syntax. Use x^2", timeOut, extraXOffset, extraYOffset);
            return;
        }


        GUI.suggestionPopup.setVisible(false);
        Equation equation = new Equation();
        equation.startDrawing(cleaned);
    }

}

