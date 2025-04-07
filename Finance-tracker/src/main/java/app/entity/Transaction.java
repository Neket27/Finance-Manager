package app.entity;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    private Long id;
    private BigDecimal amount;
    private String category;
    private Instant date;
    private String description;
    private TypeTransaction typeTransaction;
    private Long financeId;

}
