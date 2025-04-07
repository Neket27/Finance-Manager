package app.service.impl;

import app.dto.finance.CreateFinanceDto;
import app.dto.finance.FinanceDto;
import app.entity.Finance;
import app.entity.Role;
import app.entity.User;
import app.exception.user.UserAlreadyExistsException;
import app.exception.user.UserException;
import app.mapper.UserMapper;
import app.repository.UserRepository;
import app.service.FinanceService;
import app.service.UserService;
import app.springbootstartercustomloggerforpersonalfinancialtracker.aspect.auditable.Auditable;
import app.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable.CustomLogging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neket27.context.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация сервиса управления пользователями.
 */

@Slf4j
@Service
@CustomLogging
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final FinanceService financeService;

    @Override
    @Auditable
    @Transactional(rollbackFor = Exception.class)
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail()))
            throw new UserAlreadyExistsException(String.format("User with email %s already exists", user.getEmail()));

        user.setRole(userRepository.tableIsEmpty() ? Role.ADMIN : Role.USER);
        user.setActive(true);

        Finance finance = Finance.builder()
                .currentSavings(BigDecimal.ZERO)
                .monthlyBudget(BigDecimal.ZERO)
                .savingsGoal(BigDecimal.ZERO)
                .totalExpenses(BigDecimal.ZERO)
                .transactionsId(new ArrayList<>())
                .build();

        Long financeId = financeService.createEmptyFinance(finance);
        user.setFinanceId(financeId);

        return userRepository.save(user);
    }


    @Override
    @Auditable
    @Transactional(rollbackFor = Exception.class)
    public User updateDataUser(User user, String email) {
        if (user == null)
            throw new IllegalArgumentException("User Dto не может быть null");

        User _user = this.find(email);
        return userMapper.updateEntity(_user, user);
    }

    /**
     * Удаляет пользователя по email.
     *
     * @param email email пользователя
     * @return true, если удаление прошло успешно, иначе false
     */
    @Override
    @Auditable
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(String email) {
        try {
            userRepository.delete(this.find(email));
            log.debug("User {} removed", email);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    @Auditable
    @Transactional
    public User getUserByEmail(String email) {
        return find(email);
    }

    private app.entity.User find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserException(String.format("User with id %s not found", id)));
    }


    private app.entity.User find(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(String.format("User with email %s not found", email)));
    }

    /**
     * Получает список всех пользователей.
     *
     * @return список DTO пользователей
     */
    @Override
    @Auditable
    @Transactional
    public List<User> list() {
        return userMapper.toList(userRepository.getAll());
    }

    /**
     * Блокирует пользователя по email.
     *
     * @param email email пользователя
     * @return true, если блокировка прошла успешно, иначе false
     */
    @Override
    @Auditable
    @Transactional
    public boolean blockUser(String email) {
        try {
            app.entity.User user = this.find(email);
            user.setActive(false);
            userRepository.save(user);
            log.debug("Пользователь {} заблокирован.", email);
            return true;
        } catch (UserException e) {
            log.debug("Пользователь с email {} не найден.", email);
            return false;
        }
    }

    /**
     * Изменяет роль пользователя.
     *
     * @param email email пользователя
     * @param role  новая роль пользователя
     * @return true, если роль успешно изменена, иначе false
     */
    @Override
    @Auditable
    @Transactional
    public boolean changeUserRole(String email, Role role) {
        try {
            app.entity.User user = this.find(email);
            user.setRole(role);
            userRepository.save(user);
            UserContext.setCurrentUser(user);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    @Auditable
    @Transactional
    public User getUserById(Long id) {
        return find(id);
    }
}
