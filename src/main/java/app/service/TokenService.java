package app.service;

import app.entity.Token;
import app.exception.TokenException;

public interface TokenService {

    Token getTokenById(Long id) throws TokenException;

    Token getTokenByUserId(Long userId) throws TokenException;

    Token getTokenByUserEmail(String email) throws TokenException;

    Token saveToken(Token token);

    void deleteToken(Token token);

    void deleteTokenByUserId(Long userId);
}
