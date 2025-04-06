package app.mapper;

import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface TransactionMapper extends BaseMapper<app.entity.Transaction, TransactionDto> {

    Set<TransactionDto> toDtoSet(List<app.entity.Transaction> entities);

    app.entity.Transaction toEntity(CreateTransactionDto entity);

    app.entity.Transaction updateEntity(@MappingTarget app.entity.Transaction transaction, UpdateTransactionDto dto);
}
