package calc.graphcalc;

class Evaluator {

        double evaluate(Expr expr, double xValue) {

            if (expr instanceof NumberExpr n) {
                return n.value;
            }

            if (expr instanceof VariableExpr) {
                return xValue;
            }

            if (expr instanceof BinaryExpr b) {

                double left = evaluate(b.left, xValue);
                double right = evaluate(b.right, xValue);

                switch (b.op) {
                    case PLUS -> {
                        return left + right;
                    }
                    case MINUS -> {
                        return left - right;
                    }
                    case MULTIPLY -> {
                        return left * right;
                    }
                    case DIVIDE -> {
                        return left / right;
                    }
                    case POWER -> {
                        return Math.pow(left, right);
                    }
                }
            }

            if (expr instanceof LogExpr l) {
                double base = evaluate(l.base, xValue);
                double arg = evaluate(l.argument, xValue);
                return Math.log(arg) / Math.log(base);
            }

            if (expr instanceof TrigExpr t){
                double arg = evaluate(t.argument, xValue);
                return switch (t.type) {
                    case SINE -> Math.sin(arg);
                    case COSINE -> Math.cos(arg);
                    case TANGENT -> Math.tan(arg);
                    case COTANGENT -> 1/Math.tan(arg);
                    default -> throw new RuntimeException("Evaluate: Unknown trigonometric function");
                };
            }

            throw new RuntimeException("Evaluate: Unknown expression type");
        }

    Expr simplify(Expr expr) {

        if (expr instanceof NumberExpr) {
            return expr;
        }

        if (expr instanceof VariableExpr) {
            return expr;
        }

        if (expr instanceof BinaryExpr b) {

            Expr left = simplify(b.left);
            Expr right = simplify(b.right);

            switch (b.op) {
                case PLUS -> {
                    return simplifyAdd(left, right);
                }
                case MINUS -> {
                    return simplifySub(left, right);
                }
                case MULTIPLY -> {
                    return simplifyMul(left, right);
                }
                case DIVIDE -> {
                    return simplifyDiv(left, right);
                }
                case POWER -> {
                    return simplifyPow(left, right);
                }
            }
        }
        if (expr instanceof LogExpr l) {

            Expr base = simplify(l.base);
            Expr arg = simplify(l.argument);

            if (base instanceof NumberExpr bn && arg instanceof NumberExpr an && bn.value == an.value) {

                return new NumberExpr(1);
            }
            if (arg instanceof NumberExpr an && an.value == 1) {

                return new NumberExpr(0);
            }

            return new LogExpr(base, arg);
        }
        if (expr instanceof TrigExpr t) {

            Expr arg = simplify(t.argument);

            if (arg instanceof NumberExpr n) {

                return switch (t.type) {
                    case SINE -> new NumberExpr(Math.sin(n.value));
                    case COSINE -> new NumberExpr(Math.cos(n.value));
                    case TANGENT -> new NumberExpr(Math.tan(n.value));
                    case COTANGENT -> new NumberExpr(1/(Math.tan(n.value)));
                    default -> new TrigExpr(t.type, arg);
                };
            }

            return new TrigExpr(t.type, arg);
        }

        throw new RuntimeException("Simplify: Unknown expression type");
    }
    Expr simplifyAdd(Expr a, Expr b) {

        if (a instanceof NumberExpr an && b instanceof NumberExpr bn) {
            return new NumberExpr(an.value + bn.value);
        }

        if (a instanceof VariableExpr && b instanceof VariableExpr) {
            return new BinaryExpr(new NumberExpr(2), Type.MULTIPLY, new VariableExpr());
        }

        return new BinaryExpr(a, Type.PLUS, b);
    }
    Expr simplifySub(Expr a, Expr b) {

        if (a instanceof NumberExpr an && b instanceof NumberExpr bn) {
            return new NumberExpr(an.value - bn.value);
        }

        if (a instanceof VariableExpr && b instanceof VariableExpr) {
            return new NumberExpr(0);
        }

        return new BinaryExpr(a, Type.MINUS, b);
    }
    Expr simplifyMul(Expr a, Expr b) {

        if (a instanceof NumberExpr an && b instanceof NumberExpr bn) {
            return new NumberExpr(an.value * bn.value);
        }

        // 0 * anything = 0
        if (a instanceof NumberExpr an && an.value == 0) return new NumberExpr(0);
        if (b instanceof NumberExpr bn && bn.value == 0) return new NumberExpr(0);

        // Implied multiplication
        if (a instanceof NumberExpr an && an.value == 1) return b;
        if (b instanceof NumberExpr bn && bn.value == 1) return a;

        return new BinaryExpr(a, Type.MULTIPLY, b);
    }
    Expr simplifyDiv(Expr a, Expr b) {

        if (a instanceof NumberExpr an && b instanceof NumberExpr bn) {
            return new NumberExpr(an.value / bn.value);
        }

        return new BinaryExpr(a, Type.DIVIDE, b);
    }
    Expr simplifyPow(Expr a, Expr b) {

        if (b instanceof NumberExpr bn) {

            if (bn.value == 0) return new NumberExpr(1);
            if (bn.value == 1) return a;
        }

        return new BinaryExpr(a, Type.POWER, b);
    }
}