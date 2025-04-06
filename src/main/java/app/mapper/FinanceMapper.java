package app.mapper;


import app.dto.finance.CreateFinanceDto;
import app.dto.finance.FinanceDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FinanceMapper extends BaseMapper<app.entity.Finance, FinanceDto> {

    app.entity.Finance toEntity(CreateFinanceDto dto);
}
