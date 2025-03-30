package app.service.impl;

import app.aspect.loggable.CustomLogging;
import app.entity.Token;
import app.exception.common.NotFoundException;
import app.repository.TokenRepository;
import app.service.TokenService;
import org.springframework.stereotype.Service;

@Service
@CustomLogging
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;

    public TokenServiceImpl(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public Token getTokenById(Long id) {
        return tokenRepository.findById(id).orElseThrow(() -> new NotFoundException("Token with id: " + id+ " not found"));
    }

    @Override
    public Token getTokenByUserId(Long userId){
        return tokenRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException("Token with userId: " + userId+" not found"));
    }

    @Override
    public Token getTokenByUserEmail(String email) {
        return tokenRepository.getTokenByUserEmail(email).orElseThrow(() -> new NotFoundException("Token with email  " + email+ "not found"));
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
