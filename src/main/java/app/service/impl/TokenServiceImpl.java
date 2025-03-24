package app.service.impl;

import app.aspect.loggable.Loggable;
import app.entity.Token;
import app.exception.TokenException;
import app.repository.TokenRepository;
import app.service.TokenService;

@Loggable
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;

    public TokenServiceImpl(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public Token getTokenById(Long id) throws TokenException {
        return tokenRepository.findById(id).orElseThrow(() -> new TokenException("Token not found"));
    }

    @Override
    public Token getTokenByUserId(Long userId) throws TokenException {
        return tokenRepository.findByUserId(userId).orElseThrow(() -> new TokenException("Token not found"));
    }

    @Override
    public Token getTokenByUserEmail(String email) throws TokenException {
        return tokenRepository.getTokenByUserEmail(email).orElseThrow(() -> new TokenException("Token not found"));
    }

    @Override
    public Token saveToken(Token token) {
        return tokenRepository.save(token);
    }

    @Override
    public void deleteToken(Token token) {
        tokenRepository.delete(token);
    }

    @Override
    public void deleteTokenByUserId(Long userId) {
        tokenRepository.deleteByUserId(userId);
    }

}
