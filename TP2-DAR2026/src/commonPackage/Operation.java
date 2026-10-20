package commonPackage;

public class Operation {

    private double operand1;
    private char operator;
    private double operand2;

    public Operation(double operand1, char operator, double operand2) {
        this.operand1 = operand1;
        this.operator = operator;
        this.operand2 = operand2;
    }

    public double getOperand1() {
        return operand1;
    }

    public char getOperator() {
        return operator;
    }

    public double getOperand2() {
        return operand2;
    }

    public double compute() {
        switch (operator) {
            case '+':
                return operand1 + operand2;
            case '-':
                return operand1 - operand2;
            case '*':
                return operand1 * operand2;
            case '/':
                if (operand2 == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return operand1 / operand2;
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }

    public static Operation parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty input");
        }

        String[] parts = input.trim().split("\\s+");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid format. Expected: <operand1> <operator> <operand2>");
        }

        double op1;
        try {
            op1 = Double.parseDouble(parts[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid first operand: " + parts[0]);
        }

        String opStr = parts[1];
        if (opStr.length() != 1 || !"+-*/".contains(opStr)) {
            throw new IllegalArgumentException("Invalid operator: " + opStr);
        }
        char op = opStr.charAt(0);

        double op2;
        try {
            op2 = Double.parseDouble(parts[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid second operand: " + parts[2]);
        }

        return new Operation(op1, op, op2);
    }

    @Override
    public String toString() {
        return operand1 + " " + operator + " " + operand2;
    }
}
