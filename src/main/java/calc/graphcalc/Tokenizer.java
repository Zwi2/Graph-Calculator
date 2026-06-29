package calc.graphcalc;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    private static boolean canEndExpr(Type t) {
        return t == Type.NUMBER || t == Type.VARIABLE || t == Type.CLOSEP;
    }

    private static boolean canStartExpr(Type t) {
        return t == Type.NUMBER || t == Type.VARIABLE || t == Type.OPENP;
    }

    public static List<Argument> tokenize(String input) {
        ArrayList<Argument> tokens = new ArrayList<>();
        ArrayList<Argument> result = new ArrayList<>();

        double temp = 0;
        boolean buildingNumber = false;

        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);

            // Builds a multi digit number
            if (Character.isDigit(current)) {
                temp = temp * 10 + Character.getNumericValue(current);
                buildingNumber = true;
            }

            else if (current == '(') {

                if (buildingNumber) {
                    tokens.add(new Argument(temp, Type.NUMBER));
                    temp = 0;
                    buildingNumber = false;
                }

                tokens.add(new Argument(Type.OPENP));
            }

            else if (current == ')') {

                if (buildingNumber) {
                    tokens.add(new Argument(temp, Type.NUMBER));
                    temp = 0;
                    buildingNumber = false;
                }

                tokens.add(new Argument(Type.CLOSEP));
            }

            // x or 5x
            else if (current == 'x' || current == 'X') {

                if (buildingNumber) {
                    tokens.add(new Argument(temp, Type.NUMBER));
                    tokens.add(new Argument(Type.MULTIPLY));
                }

                tokens.add(new Argument(Type.VARIABLE));


                temp = 0;
                buildingNumber = false;
            }

            else if (current == 'l'){
                if(input.startsWith("log", i)){
                    if (buildingNumber) {
                        tokens.add(new Argument(temp, Type.NUMBER));
                        tokens.add(new Argument(Type.MULTIPLY));
                        temp = 0;
                        buildingNumber = false;
                    }
                    tokens.add(new Argument(Type.LOG));

                    i+=2;
                }
            }

            else if (current == 's'){
                if(input.startsWith("sin", i)){
                    if (buildingNumber) {
                        tokens.add(new Argument(temp, Type.NUMBER));
                        tokens.add(new Argument(Type.MULTIPLY));
                        temp = 0;
                        buildingNumber = false;
                    }
                    tokens.add(new Argument(Type.SINE));

                    i+=2;
                }
            }
            else if (current == 'c'){
                if(input.startsWith("cos", i) || input.startsWith("ctg", i)){
                    if (buildingNumber) {
                        tokens.add(new Argument(temp, Type.NUMBER));
                        tokens.add(new Argument(Type.MULTIPLY));
                        temp = 0;
                        buildingNumber = false;
                    }

                    Type type = (input.startsWith("cos", i)) ? Type.COSINE : Type.COTANGENT;
                    tokens.add(new Argument(type));

                    i+=2;
                }
            }
            else if (current == 't'){
                if(input.startsWith("tg", i)){
                    if (buildingNumber) {
                        tokens.add(new Argument(temp, Type.NUMBER));
                        tokens.add(new Argument(Type.MULTIPLY));
                        temp = 0;
                        buildingNumber = false;
                    }
                    tokens.add(new Argument(Type.TANGENT));

                    i+=1;
                }
            }

            // Operator handling
            else if (current == '+') {

                if (buildingNumber) {
                    tokens.add(new Argument(temp, Type.NUMBER));
                }

                tokens.add(new Argument(Type.PLUS));

                temp = 0;
                buildingNumber = false;
            }

            else if (current == '-') {

                if (buildingNumber) {
                    tokens.add(new Argument(temp, Type.NUMBER));
                }

                tokens.add(new Argument(Type.MINUS));

                temp = 0;
                buildingNumber = false;
            }

            else if (current == '*') {

                if (buildingNumber) {
                    tokens.add(new Argument(temp, Type.NUMBER));
                }

                tokens.add(new Argument(Type.MULTIPLY));

                temp = 0;
                buildingNumber = false;
            }

            // Standalone divide operator
            else if (current == '/') {

                if (buildingNumber) {
                    tokens.add(new Argument(temp, Type.NUMBER));
                }

                tokens.add(new Argument(Type.DIVIDE));

                temp = 0;
                buildingNumber = false;
            }

            else if (current == '^') {

                if (buildingNumber) {
                    tokens.add(new Argument(temp, Type.NUMBER));
                    temp = 0;
                    buildingNumber = false;
                }

                tokens.add(new Argument(Type.POWER));
            }

            // Any other character ends a number
            else {

                if (buildingNumber) {
                    tokens.add(new Argument(temp, Type.NUMBER));

                    temp = 0;
                    buildingNumber = false;
                }
            }
        }

        // Handle trailing number
        if (buildingNumber) {
            tokens.add(new Argument(temp, Type.NUMBER));
        }

        for (int i = 0; i < tokens.size(); i++) {

            result.add(tokens.get(i));

            if (i == tokens.size() - 1)
                continue;

            boolean insertMultiply = canInsertMultiply(tokens, i);

            if (insertMultiply) {
                result.add(new Argument(Type.MULTIPLY));
            }
        }
        for(Argument a : tokens){
            System.out.println(a.getType() + " "+ a.getValue());
        }
    return result;
    }

    private static boolean canInsertMultiply(ArrayList<Argument> tokens, int i) {
        Type current = tokens.get(i).type;
        Type next = tokens.get(i + 1).type;

        boolean insertMultiply = canEndExpr(current) && canStartExpr(next);

        if (current == Type.NUMBER && next == Type.OPENP) {

            boolean isLogBase = i > 0 && tokens.get(i - 1).type == Type.LOG;

            if (isLogBase) {
                insertMultiply = false;
            }
        }
        return insertMultiply;
    }

}
