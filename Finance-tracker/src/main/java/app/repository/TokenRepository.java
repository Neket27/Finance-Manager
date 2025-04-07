package app.repository;

import app.entity.Token;

import java.util.Optional;

public interface TokenRepository extends BaseRepository<Token, Long> {

    Optional<Token> findByUserId(Long userId);

    Optional<Token> getTokenByUserEmail(String email);

    void deleteByUserId(Long userId);
}
