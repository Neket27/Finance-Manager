package app.entity;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Finance {

    @Id
    private Long id;
    private BigDecimal monthlyBudget;
    private BigDecimal savingsGoal;
    private BigDecimal currentSavings;
    private BigDecimal totalExpenses;
    private List<Long> transactionsId = new ArrayList<>();

}
