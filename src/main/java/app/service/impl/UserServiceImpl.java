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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация сервиса управления пользователями.
 */

@Service
@CustomLogging
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final FinanceService financeService;

    public UserServiceImpl(UserMapper userMapper, UserRepository userRepository, FinanceService financeService) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.financeService = financeService;
    }

    @Override
    @Auditable
    public UserDto createUser(CreateUserDto createUserDto) {
        if (userRepository.existsByEmail(createUserDto.email()))
            throw new UserAlreadyExistsException(String.format("User with email %s already exists", createUserDto.email()));

        User user = userMapper.toEntity(createUserDto);
        user.setRole(userRepository.getAll().isEmpty() ? Role.ADMIN : Role.USER);

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
    public UserDto getUserById(Long id) {
        return userMapper.toDto(find(id));
    }
}
