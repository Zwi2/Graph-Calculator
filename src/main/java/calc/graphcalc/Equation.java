package calc.graphcalc;

import javafx.scene.paint.Color;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class Equation {

    static boolean hasEquation = false;
    static Function function;
    static Expr expr;
    static Evaluator evaluator = new Evaluator();

    public static Function detectFunction(String input) {

        if(input.contains("log")){
            return function = Function.LOGARITHMIC;
        }
        if(input.contains("sin")){
            return function = Function.SINE;
        }
        if(input.contains("cos")){
            return function = Function.COSINE;
        }
        if(input.contains("tg")){
            return function = Function.TAN;
        }
        if(input.contains("ctg")){
            return function = Function.COTAN;
        }
        if (input.contains("x^2")) {
            return function = Function.QUADRATIC;
        }
        if (input.contains("^x")) {
            return function = Function.EXPONENTIAL;
        }
        if (!input.contains("^") && !input.contains("log")) {
            return Function.LINEAR;
        }
        return Function.UNKNOWN;

    }

    public static double toScreenX(double mathX) {
        return GUI.getCenterX() + mathX * GUI.scale;
    }

    public static double toScreenY(double mathY) {
        return GUI.getCenterY() - mathY * GUI.scale;
    }

    public static double toMathX(double screenX) {
        return (screenX - GUI.getCenterX()) / GUI.scale;
    }

    public void startDrawing(String input){
        List<Argument> tokens = Tokenizer.tokenize(input);

        Parser parser = new Parser(tokens);
        Expr tree = parser.parse();

        expr = evaluator.simplify(tree);
        function = detectFunction(input);

        hasEquation = true;
        GUI.redraw();

        tokens.clear();
    }
    public static void redraw() {
//        drawFunction(x -> evaluator.evaluate(expr, x), PlotPolicy.DEFAULT);
        switch (function) {
            case LOGARITHMIC -> drawFunction(x -> evaluator.evaluate(expr, x), PlotPolicy.logarithmicPolicy());
            case LINEAR, QUADRATIC, EXPONENTIAL, SINE, TAN, COSINE, COTAN, UNKNOWN -> drawFunction(x -> evaluator.evaluate(expr, x), PlotPolicy.DEFAULT);
        }

    }
    interface PlotPolicy {

        // Reject points that should not exist in the function domain
        boolean invalidPoint(double x, double y);

        // Prevent drawing across discontinuities or asymptotes
        boolean isBreak(double x0, double y0, double x1, double y1);

        PlotPolicy DEFAULT = new PlotPolicy() {
            @Override
            public boolean invalidPoint(double x, double y) {
                return !Double.isFinite(y);
            }

            @Override
            public boolean isBreak(double x0, double y0,
                                   double x1, double y1) {
                return false;
            }
        };

        static PlotPolicy logarithmicPolicy() {
            return new PlotPolicy() {

                @Override
                public boolean invalidPoint(double x, double y) {
                    return x <= 0 || !Double.isFinite(y);
                }

                @Override
                public boolean isBreak(double x0, double y0, double x1, double y1) {

                    double jump = Math.abs(toScreenY(y1) - toScreenY(y0));
                    return jump > GUI.canvas.getHeight();
                }
            };
        }
    }
    static void drawFunction(DoubleUnaryOperator f, PlotPolicy policy) {
        GUI.setLineColor(Color.RED);
        double width = GUI.canvas.getWidth();

        double x0 = toMathX(0);
        double x1 = toMathX(width);
        subdivide(f, policy, x0, x1, 18);
    }

    // Adaptive subdivision renderer: refines segments until they are flat enough
    static void subdivide(DoubleUnaryOperator f, PlotPolicy policy, double x0, double x1, int depth) {
        double y0 = f.applyAsDouble(x0);
        double y1 = f.applyAsDouble(x1);
        if (depth <= 0) {
            return;
        }

        // If either endpoint is invalid, try splitting the interval
        if (policy.invalidPoint(x0, y0) || policy.invalidPoint(x1, y1)) {
            double xm = (x0 + x1) / 2;

            subdivide(f, policy, x0, xm, depth - 1);
            subdivide(f, policy, xm, x1, depth - 1);
            return;
        }

        double sx0 = toScreenX(x0);
        double sx1 = toScreenX(x1);

        double xm = (x0 + x1) / 2;
        double ym = f.applyAsDouble(xm);

        double sy0 = toScreenY(y0);
        double sy1 = toScreenY(y1);
        double sym = toScreenY(ym);
        double linearMid = (sy0 + sy1) * 0.5;
        double errorMid = Math.abs(sym - linearMid);
        double screenJump = Math.abs(sy1 - sy0);

        double xq1 = x0 + (x1 - x0) * 0.25;
        double xq3 = x0 + (x1 - x0) * 0.75;

        double yq1 = f.applyAsDouble(xq1);
        double yq3 = f.applyAsDouble(xq3);

        if (policy.invalidPoint(xq1, yq1) ||
                policy.invalidPoint(xq3, yq3)) {

            subdivide(f, policy, x0, xm, depth - 1);
            subdivide(f, policy, xm, x1, depth - 1);
            return;
        }

        double syq1 = toScreenY(yq1);
        double syq3 = toScreenY(yq3);

        double expectedQ1 = sy0 + (sy1 - sy0) * 0.25;
        double expectedQ3 = sy0 + (sy1 - sy0) * 0.75;

        double errorQ1 = Math.abs(syq1 - expectedQ1);
        double errorQ3 = Math.abs(syq3 - expectedQ3);

        double error = Math.max(errorMid, Math.max(errorQ1, errorQ3));

        // Stop if segment too small
        if (error < 1 && Math.abs(sx1 - sx0) < 8) {
            if (!policy.isBreak(x0, y0, x1, y1)) {
                drawSegment(x0, y0, x1, y1);
            }
            return;
        }
        if (screenJump > GUI.canvas.getHeight()) {
            subdivide(f, policy, x0, xm, depth - 1);
            subdivide(f, policy, xm, x1, depth - 1);
            return;
        }

        // If midpoint is invalid || If too curved
        if (policy.invalidPoint(xm, ym) || error > 0.5) {
            subdivide(f, policy, x0, xm, depth - 1);
            subdivide(f, policy, xm, x1, depth - 1);
            return;
        }

        if (policy.isBreak(x0, y0, x1, y1)) return;

        drawSegment(x0, y0, x1, y1);
    }
    static void drawSegment(double mathX0, double mathY0,
                            double mathX1, double mathY1) {

        double sx0 = toScreenX(mathX0);
        double sy0 = toScreenY(mathY0);

        double sx1 = toScreenX(mathX1);
        double sy1 = toScreenY(mathY1);

        if (Double.isFinite(sx0) && Double.isFinite(sy0) && Double.isFinite(sx1) && Double.isFinite(sy1)) {

            GUI.drawLine(sx0, sy0, sx1, sy1);
        }
    }

}