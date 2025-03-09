package app.dto.user;

public record CreateUserDto(
        String name,
        String email,
        String password
) {
}
