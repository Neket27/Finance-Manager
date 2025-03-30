//package app.mapper.mapstruct;
//
//import app.dto.user.CreateUserDto;
//import app.dto.user.UpdateUserDto;
//import app.dto.user.UserDto;
//import app.entity.User;
//import app.mapper.BaseMapper;
//import org.mapstruct.Mapper;
//import org.mapstruct.MappingTarget;
//
//import java.util.Collection;
//import java.util.List;
//
//@Mapper(componentModel = "default")
//public interface UserMapper extends BaseMapper<User, UserDto> {
//
//
//    User toEntity(CreateUserDto userDto);
//
//    //    public User toEntity(CreateUserDto userDto) {
////        return new User.Builder()
////                .setName(userDto.name())
////                .email(userDto.email())
////                .password(userDto.password())
////                .build();
////    }
////
//    User updateEntity(UpdateUserDto userDto, @MappingTarget User user);
//
//    //    public User updateEntity(UpdateUserDto userDto, User user) {
////        user.setName(userDto.name());
////        user.setEmail(userDto.email());
////        user.setPassword(userDto.password());
////        user.setRole(userDto.role());
////        user.setFinanceId(userDto.financeId());
////        return user;
////    }
////
////    public UserDto toDto(User user) {
////        return new UserDto(
////                user.getId(),
////                user.getName(),
////                user.getEmail(),
////                user.getPassword(),
////                user.isActive(),
////                user.getRole(),
////                user.getFinanceId());
////    }
////
//    UserDto toDto(CreateUserDto userDto);
////    public UserDto toDto(CreateUserDto userDto) {
////        return new UserDto.Builder()
////                .name(userDto.name())
////                .email(userDto.email())
////                .password(userDto.password())
////                .build();
////    }
////
//        List<UserDto> toListDto(Collection<User> users);
//
////    public List<UserDto> toListDto(Collection<User> users) {
////        return users.stream().map(this::toDto).toList();
////    }
//}
