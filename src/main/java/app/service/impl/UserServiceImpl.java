package app.service.impl;

import app.dto.finance.CreateFinanceDto;
import app.dto.user.CreateUserDto;
import app.dto.user.UpdateUserDto;
import app.dto.user.UserDto;
import app.entity.Finance;
import app.entity.Role;
import app.entity.User;
import app.exeption.NotFoundException;
import app.exeption.UserExistException;
import app.mapper.FinanceMapper;
import app.mapper.UserMapper;
import app.repository.FinanceRepository;
import app.repository.UserRepository;
import app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final FinanceRepository financeRepository;
    private final FinanceMapper financeMapper;

    public UserServiceImpl(UserMapper userMapper, UserRepository userRepository, FinanceRepository financeRepository, FinanceMapper financeMapper) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.financeRepository = financeRepository;
        this.financeMapper = financeMapper;
    }

    @Override
    public UserDto createUser(CreateUserDto createUserDto) {
        if (userRepository.existsByEmail(createUserDto.email()))
            throw new UserExistException(String.format("User with email %s already exists", createUserDto.email()));

        User user = userMapper.toEntity(createUserDto);

        if (userRepository.getAll().isEmpty())
            user.setRole(Role.Admin);
        else
            user.setRole(Role.User);


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


    @Override
    public UserDto updateDataUser(UpdateUserDto userDto, String email) {
        if (userDto == null)
            throw new IllegalArgumentException("User  Dto не может быть null");

        User user = this.find(email);
        user = userMapper.updateEntity(userDto, user);
        return userMapper.toDto(user);
    }

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

    @Override
    public UserDto getUserByEmail(String email) {
        return userMapper.toDto(this.find(email));
    }

    private User find(String email) {
        return userRepository.findById(email)
                .orElseThrow(() -> new NotFoundException(String.format("User with email %s not found", email)));
    }

    @Override
    public List<UserDto> list() {
        return userMapper.toListDto(userRepository.getAll());
    }

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

    @Override
    public boolean changeUserRole(String email, Role role) {
        try {
            User user = this.find(email);
            user.setRole(role);
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

}
