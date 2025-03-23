package app.mapper;

import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.entity.Transaction;

import java.util.List;

public class TransactionMapper implements BaseMapper<Transaction, TransactionDto> {

    @Override
    public Transaction toEntity(TransactionDto dto) {
        return new Transaction.Builder()
                .id(dto.id())
                .amount(dto.amount())
                .category(dto.category())
                .date(dto.date())
                .description(dto.description())
                .typeTransaction(dto.typeTransaction())
                .financeId(dto.financeId())
                .build();
    }

    @Override
    public TransactionDto toDto(Transaction entity) {
        return new TransactionDto.Builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .category(entity.getCategory())
                .date(entity.getDate())
                .description(entity.getDescription())
                .typeTransaction(entity.getTypeTransaction())
                .financeId(entity.getFinanceId())
                .build();
    }

    @Override
    public List<Transaction> toEntityList(List<TransactionDto> dtoList) {
        return dtoList.stream().map(this::toEntity).toList();
    }

    @Override
    public List<TransactionDto> toDtoList(List<Transaction> entityList) {
        return entityList.stream().map(this::toDto).toList();
    }

    public Transaction toEntity(CreateTransactionDto dto) {
        return new Transaction.Builder()
                .amount(dto.amount())
                .category(dto.category())
                .description(dto.description())
                .typeTransaction(dto.typeTransaction())
                .build();
    }

    public Transaction updateEntity(Transaction transaction, UpdateTransactionDto dto) {
        transaction.setId(dto.id());
        transaction.setAmount(dto.amount());
        transaction.setCategory(dto.category());
        transaction.setDate(dto.date());
        transaction.setDescription(dto.description());
        transaction.setTypeTransaction(dto.typeTransaction());
        return transaction;
    }
}
