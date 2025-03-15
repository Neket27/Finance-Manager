package app.util.in;

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

    public double readDouble(String prompt) {
        System.out.print(prompt);
        double value = scanner.nextDouble();
        scanner.nextLine();
        return value;
    }


    public void close() {
        scanner.close();
    }
}
