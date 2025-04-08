package app.mapper;

import app.dto.user.CreateUserDto;
import app.dto.user.UpdateUserDto;
import app.dto.user.UserDto;
import app.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper extends BaseMapper<User, UserDto> {

    @Mapping(source = "name", target = "name")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "financeId", target = "financeId")
    User updateEntity(User userNewData, @MappingTarget User user);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    User toEntity(CreateUserDto userDto);

    List<UserDto> toListDto(Collection<User> users);

    List<User> toList(Collection<User> all);
}
