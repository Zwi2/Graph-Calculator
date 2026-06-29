package calc.graphcalc;

public class Argument {
    public final Type type;
    public final Double value; // null for operators

    public Argument(Type type) {
        this.type = type;
        this.value = null;
    }

    public Argument(double value, Type type) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public Double getValue() {
        return value;
    }
}