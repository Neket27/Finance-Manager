package app.entity;

import lombok.*;
import neket27.entity.UserDetails;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    private Long id;
    private String name;
    private String email;
    private String password;
    private boolean isActive;
    private Role role;
    private Long financeId;

}
