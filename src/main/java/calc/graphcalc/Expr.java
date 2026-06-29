package calc.graphcalc;

abstract class Expr {}

class NumberExpr extends Expr {
    double value;

    NumberExpr(double value) {
        this.value = value;
    }
}

class VariableExpr extends Expr {}

class BinaryExpr extends Expr {
    Expr left;
    Expr right;
    Type op;

    BinaryExpr(Expr left, Type op, Expr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }
}
class LogExpr extends Expr {

    Expr base;
    Expr argument;

    LogExpr(Expr base, Expr argument) {
        this.base = base;
        this.argument = argument;
    }
}
class TrigExpr extends Expr {
    Type type;
    Expr argument;
    TrigExpr(Type type, Expr argument) {
        this.argument = argument;
        this.type = type;
    }
}
