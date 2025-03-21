package app.util.in;

import java.math.BigDecimal;
import java.util.Scanner;

public class UserInput {
    private final Scanner scanner;

    public UserInput(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public int readInt(String prompt) {
        System.out.print(prompt);
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    public long readLong(String prompt) {
        return readInt(prompt);
    }

    public BigDecimal readBigDecimal(String prompt) {
        System.out.print(prompt);
        BigDecimal value = scanner.nextBigDecimal();
        scanner.nextLine();
        return value;
    }


    public void close() {
        scanner.close();
    }
}
