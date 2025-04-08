package app.service;

import app.entity.Token;

public interface TokenService {

    Token getTokenById(Long id);

    Token getTokenByUserId(Long userId);

    Token getTokenByUserEmail(String email);

    Token saveToken(Token token);

    void deleteToken(Token token);

    void deleteTokenByUserId(Long userId);
}
