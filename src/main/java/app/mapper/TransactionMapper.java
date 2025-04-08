package app.mapper;

import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface TransactionMapper extends BaseMapper<Transaction, TransactionDto> {

    Set<TransactionDto> toDtoSet(List<Transaction> entities);

    Transaction toEntity(CreateTransactionDto entity);

    Transaction updateEntity(@MappingTarget Transaction transaction, UpdateTransactionDto dto);
}
