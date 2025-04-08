package app.service.impl;

import app.container.Component;
import app.context.UserContext;
import app.dto.finance.CreateFinanceDto;
import app.dto.user.CreateUserDto;
import app.dto.user.UpdateUserDto;
import app.dto.user.UserDto;
import app.entity.Role;
import app.entity.User;
import app.exception.NotFoundException;
import app.exception.UserAlreadyExistsException;
import app.mapper.FinanceMapper;
import app.mapper.UserMapper;
import app.repository.UserRepository;
import app.service.FinanceService;
import app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация сервиса управления пользователями.
 */

@Component
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
    public UserDto createUser(CreateUserDto createUserDto) {
        if (userRepository.existsByEmail(createUserDto.email()))
            throw new UserAlreadyExistsException(String.format("User with email %s already exists", createUserDto.email()));

        User user = userMapper.toEntity(createUserDto);
        user.setRole(userRepository.getAll().isEmpty() ? Role.ADMIN : Role.USER);

        // создание пустого кошелька
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

    /**
     * Получает пользователя по email.
     *
     * @param email email пользователя
     * @return DTO пользователя
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    public UserDto getUserByEmail(String email) {
        return userMapper.toDto(this.find(email));
    }

    /**
     * Ищет пользователя по id
     *
     * @param id пользователя
     * @return объект пользователя
     * @throws NotFoundException если пользователь не найден
     */
    private User find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %s not found", id)));
    }

    /**
     * Ищет пользователя по email
     *
     * @param email пользователя
     * @return объект пользователя
     * @throws NotFoundException если пользователь не найден
     */
    private User find(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(String.format("User with email %s not found", email)));
    }

    /**
     * Получает список всех пользователей.
     *
     * @return список DTO пользователей
     */
    @Override
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
    public boolean blockUser(String email) {
        try {
            User user = this.find(email);
            user.setActive(false);
            userRepository.save(user);
            log.debug("Пользователь {} заблокирован.", email);
            return true;
        } catch (NotFoundException e) {
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
    public UserDto getUserById(Long id) {
        return userMapper.toDto(find(id));
    }
}
