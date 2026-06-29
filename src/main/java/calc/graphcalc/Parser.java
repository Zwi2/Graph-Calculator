package calc.graphcalc;

import java.util.List;

class Parser {
    List<Argument> tokens;
    int pos = 0;

    Parser(List<Argument> tokens) {
        this.tokens = tokens;
    }

    Argument peek() {
        return pos < tokens.size() ? tokens.get(pos) : null;
    }

    Argument consume() {
        Argument a = tokens.get(pos++);
        System.out.println("Consumed: " + a.type + " " + a.getValue());
        return a;
    }
    Expr parse() {
        return parseAddSub();
    }
    Expr parseAddSub() {
        Expr left = parseMulDiv();

        while (peek() != null && (peek().type == Type.PLUS || peek().type == Type.MINUS)) {

            Type op = consume().type;
            Expr right = parseMulDiv();

            left = new BinaryExpr(left, op, right);
        }

        return left;
    }
    Expr parseMulDiv() {
        Expr left = parsePower();

        while (peek() != null && (peek().type == Type.MULTIPLY || peek().type == Type.DIVIDE)) {

            Type op = consume().type;
            Expr right = parsePower();

            left = new BinaryExpr(left, op, right);
        }

        return left;
    }

    Expr parsePower() {
        Expr left = parseUnary();

        if (peek() != null && peek().type == Type.POWER) {
            consume();
            Expr right = parsePower();

            return new BinaryExpr(left, Type.POWER, right);
        }
        return left;
    }
    Expr parseUnary() {

        if (peek() != null && peek().type == Type.MINUS) {

            consume();

            return new BinaryExpr(new NumberExpr(0), Type.MINUS, parseUnary());
        }

        return parsePrimary();
    }
    Expr parsePrimary() {
        Argument token = consume();

        switch (token.type) {
            case NUMBER -> {
                return new NumberExpr(token.value);
            }
            case VARIABLE -> {
                return new VariableExpr();
            }
            case LOG -> {
                Expr base = new NumberExpr(10);
                if (peek().getType() != Type.OPENP){
                     base = parsePrimary();
                }
//                System.out.println("peek type: "+peek().getType());
                if (peek() == null || peek().type != Type.OPENP) {
                    throw new RuntimeException("Expected '(' after log base");
                }
                consume(); // (
                Expr argument = parseAddSub();

                if (peek() == null || peek().type != Type.CLOSEP) {
                    throw new RuntimeException("Expected ')'");
                }
                consume();
                return new LogExpr(base, argument);
            }
            case SINE, COSINE, TANGENT, COTANGENT -> {

                if (peek() == null || peek().type != Type.OPENP) {
                    throw new RuntimeException("Expected '(' after "+ token.type.toString().toLowerCase());
                }
                consume(); // (
                Expr argument = parseAddSub();

                if (peek() == null || peek().type != Type.CLOSEP) {
                    throw new RuntimeException("Expected ')'");
                }
                consume();
                return new TrigExpr(token.type, argument);
            }
            case OPENP -> {
                Expr inside = parseAddSub();

                if (peek() == null || peek().type != Type.CLOSEP) {

                    throw new RuntimeException("Missing ')'");
                }

                consume();

                return inside;
            }
            default -> throw new RuntimeException("Unexpected token: " + token.type);
        }
    }
}
