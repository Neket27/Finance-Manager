package app.service.impl;

import app.aspect.auditable.Auditable;
import app.aspect.loggable.CustomLogging;
import app.entity.Token;
import app.exception.common.NotFoundException;
import app.repository.TokenRepository;
import app.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CustomLogging
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;

    @Override
    @Auditable
    @Transactional
    public Token getTokenById(Long id) {
        return tokenRepository.findById(id).orElseThrow(() -> new NotFoundException("Token with id: " + id+ " not found"));
    }

    @Override
    @Auditable
    @Transactional
    public Token getTokenByUserId(Long userId){
        return tokenRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException("Token with userId: " + userId+" not found"));
    }

    @Override
    @Auditable
    @Transactional
    public Token getTokenByUserEmail(String email) {
        return tokenRepository.getTokenByUserEmail(email).orElseThrow(() -> new NotFoundException("Token with email  " + email+ "not found"));
    }

    @Override
    @Auditable
    @Transactional(rollbackFor = Exception.class)
    public Token saveToken(Token token) {
        return tokenRepository.save(token);
    }

    @Override
    @Auditable
    @Transactional(rollbackFor = Exception.class)
    public void deleteToken(Token token) {
        tokenRepository.delete(token);
    }

    @Override
    @Auditable
    @Transactional(rollbackFor = Exception.class)
    public void deleteTokenByUserId(Long userId) {
        tokenRepository.deleteByUserId(userId);
    }

}
