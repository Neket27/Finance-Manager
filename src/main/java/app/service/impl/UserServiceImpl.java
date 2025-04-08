package app.service.impl;

import app.aspect.auditable.Auditable;
import app.aspect.loggable.CustomLogging;
import app.context.UserContext;
import app.dto.finance.CreateFinanceDto;
import app.dto.user.CreateUserDto;
import app.dto.user.UpdateUserDto;
import app.dto.user.UserDto;
import app.entity.Role;
import app.entity.User;
import app.exception.user.UserAlreadyExistsException;
import app.exception.user.UserException;
import app.mapper.UserMapper;
import app.repository.UserRepository;
import app.service.FinanceService;
import app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public UserDto createUser(CreateUserDto createUserDto) {
        if (userRepository.existsByEmail(createUserDto.email()))
            throw new UserAlreadyExistsException(String.format("User with email %s already exists", createUserDto.email()));

        User user = userMapper.toEntity(createUserDto);
        //TODO убрать загрузку всех пользователей
        user.setRole(userRepository.getAll().isEmpty() ? Role.ADMIN : Role.USER);
        user.setActive(true);

        CreateFinanceDto financeDto = new CreateFinanceDto.Builder()
                .currentSavings(BigDecimal.ZERO)
                .monthlyBudget(BigDecimal.ZERO)
                .savingsGoal(BigDecimal.ZERO)
                .totalExpenses(BigDecimal.ZERO)
                .transactionsId(new ArrayList<>())
                .build();

        Long financeId = financeService.createEmptyFinance(financeDto);
        user.setFinanceId(financeId);

        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    /**
     * Обновляет данные пользователя.
     *
     * @param userDto объект с обновленными данными пользователя
     * @param email   email пользователя
     * @return DTO обновленного пользователя
     * @throws IllegalArgumentException если userDto равен null
     */
    @Override
    @Auditable
    @Transactional(rollbackFor = Exception.class)
    public UserDto updateDataUser(UpdateUserDto userDto, String email) {
        if (userDto == null)
            throw new IllegalArgumentException("User Dto не может быть null");

        User user = this.find(email);
        user = userMapper.updateEntity(userDto, user);
        return userMapper.toDto(user);
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
    public UserDto getUserByEmail(String email) {
        return userMapper.toDto(this.find(email));
    }

    private User find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserException(String.format("User with id %s not found", id)));
    }


    private User find(String email) {
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
    public List<UserDto> list() {
        return userMapper.toListDto(userRepository.getAll());
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
            User user = this.find(email);
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
            User user = this.find(email);
            user.setRole(role);
            userRepository.save(user);
            UserContext.setCurrentUser(userMapper.toDto(user));
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    @Auditable
    @Transactional
    public UserDto getUserById(Long id) {
        return userMapper.toDto(find(id));
    }
}
