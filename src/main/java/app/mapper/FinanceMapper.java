package app.mapper;

import app.dto.finance.CreateFinanceDto;
import app.dto.finance.FinanceDto;
import app.entity.Finance;

import java.util.List;
import java.util.stream.Collectors;

public class FinanceMapper implements BaseMapper<Finance, FinanceDto> {

    @Override
    public Finance toEntity(FinanceDto dto) {
        if (dto == null)
            return null;

        return new Finance.Builder()
                .id(dto.id())
                .monthlyBudget(dto.monthlyBudget())
                .savingsGoal(dto.savingsGoal())
                .currentSavings(dto.currentSavings())
                .totalExpenses(dto.totalExpenses())
                .transactionsId(dto.transactionsId())
                .build();
    }

    @Override
    public FinanceDto toDto(Finance entity) {
        if (entity == null) {
            return null;
        }
        return new FinanceDto.Builder()
                .id(entity.getId())
                .monthlyBudget(entity.getMonthlyBudget())
                .savingsGoal(entity.getSavingsGoal())
                .currentSavings(entity.getCurrentSavings())
                .totalExpenses(entity.getTotalExpenses())
                .transactionsId(entity.getTransactionsId())
                .build();
    }

    @Override
    public List<Finance> toEntityList(List<FinanceDto> dtoList) {
        if (dtoList == null) {
            return List.of();
        }
        return dtoList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<FinanceDto> toDtoList(List<Finance> entityList) {
        if (entityList == null) {
            return List.of();
        }
        return entityList.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Finance toEntity(CreateFinanceDto dto) {
        if (dto == null)
            return null;

        return new Finance.Builder()
                .monthlyBudget(dto.monthlyBudget())
                .savingsGoal(dto.savingsGoal())
                .currentSavings(dto.currentSavings())
                .totalExpenses(dto.totalExpenses())
                .transactionsId(dto.transactionsId())
                .build();
    }
}
