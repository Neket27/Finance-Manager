package app.util.out;

import app.dto.transaction.TransactionDto;
import app.entity.TypeTransaction;

import java.util.List;

public class UserOutput {
    public void print(String message) {
        System.out.println(message);
    }

    public void printTransactions(List<TransactionDto> transactions) {
        for (TransactionDto transaction : transactions) {
            System.out.println("Сумма: " + transaction.amount() +
                    ", Категория: " + transaction.category() +
                    ", Описание: " + transaction.description() +
                    ", Тип: " + (transaction.typeTransaction().equals(TypeTransaction.PROFIT) ? "Доход" : "Расход"));
        }
    }

}
