package app.mapper;


import app.dto.finance.CreateFinanceDto;
import app.dto.finance.FinanceDto;
import app.entity.Finance;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FinanceMapper extends BaseMapper<Finance, FinanceDto> {

    Finance toEntity(CreateFinanceDto dto);
}
