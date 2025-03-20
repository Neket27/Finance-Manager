package app.service.impl;

import app.context.UserContext;
import app.dto.finance.CreateFinanceDto;
import app.mapper.FinanceMapper;
import app.repository.FinanceRepository;
import app.dto.user.CreateUserDto;
import app.dto.user.UpdateUserDto;
import app.dto.user.UserDto;
import app.entity.Finance;
import app.entity.Role;
import app.entity.User;
import app.exception.NotFoundException;
import app.exception.UserExistException;
import app.mapper.UserMapper;
import app.repository.UserRepository;
import app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Реализация сервиса управления пользователями.
 */
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final FinanceRepository financeRepository;
    private final FinanceMapper financeMapper;

    /**
     * Конструктор сервиса пользователей.
     *
     * @param userMapper        маппер пользователей
     * @param userRepository    репозиторий пользователей
     * @param financeRepository репозиторий финансов
     * @param financeMapper     маппер финансов
     */
    public UserServiceImpl(UserMapper userMapper, UserRepository userRepository, FinanceRepository financeRepository, FinanceMapper financeMapper) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.financeRepository = financeRepository;
        this.financeMapper = financeMapper;
    }

    /**
     * Создает нового пользователя.
     *
     * @param createUserDto объект с данными нового пользователя
     * @param  role для первго пользователя устанавливается  Role.Admin
     * @return DTO созданного пользователя
     * @throws UserExistException если пользователь с таким email уже существует
     */
    @Override
    public UserDto createUser(CreateUserDto createUserDto) {
        if (userRepository.existsByEmail(createUserDto.email()))
            throw new UserExistException(String.format("User with email %s already exists", createUserDto.email()));

        User user = userMapper.toEntity(createUserDto);

        if (userRepository.getAll().isEmpty())
            user.setRole(Role.ADMIN);
        else
            user.setRole(Role.USER);

        CreateFinanceDto dto = new CreateFinanceDto.Builder()
                .currentSavings(0.0)
                .monthlyBudget(0.0)
                .savingsGoal(0.0)
                .totalExpenses(0.0)
                .transactionsId(new ArrayList<>())
                .build();

        Finance finance = financeRepository.save(financeMapper.toEntity(dto));
        user.setFinanceId(finance.getId());
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
     * @param id  пользователя
     * @return объект пользователя
     * @throws NotFoundException если пользователь не найден
     */
    private User find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %s not found", id)));
    }

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
}
