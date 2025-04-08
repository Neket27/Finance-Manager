package app.entity;

import lombok.*;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private Long id;
    private String name;
    private String email;
    private String password;
    private boolean isActive;
    private Role role;
    private Long financeId;
}
