package app.controller;

import app.context.UserContext;
import app.controller.advice.annotation.CustomExceptionHandler;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
@CustomExceptionHandler
public class FinanceController {

    private final FinanceService financeService;


    /**
     * Создает новую транзакцию.
     *
     * @param dto Данные для создания транзакции.
     * @return Возвращает созданную транзакцию.
     */
    @Operation(summary = "Создание новой транзакции", description = "Создает новую транзакцию для текущего пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Транзакция успешно создана",
                    content = @Content(schema = @Schema(implementation = TransactionDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDto create(
            @Valid @RequestBody @Parameter(description = "Данные для создания транзакции") CreateTransactionDto dto) {
        return financeService.createTransaction(getFinanceIdCurrentUser(), dto);
    }

    /**
     * Удаляет транзакцию по ID.
     *
     * @param id Идентификатор транзакции для удаления.
     */
    @Operation(summary = "Удаление транзакции", description = "Удаляет транзакцию по предоставленному ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Транзакция успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Транзакция не найдена")
    })
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam(name = "id") @Parameter(description = "ID транзакции для удаления") Long id) {
        financeService.delete(getFinanceIdCurrentUser(), id);
    }

    /**
     * Обновляет существующую транзакцию.
     *
     * @param dto Данные для обновления транзакции.
     * @return Возвращает обновленную транзакцию.
     */
    @Operation(summary = "Обновление транзакции", description = "Обновляет данные существующей транзакции.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Транзакция успешно обновлена",
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = TransactionDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
            @ApiResponse(responseCode = "404", description = "Транзакция не найдена")
    })
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public TransactionDto update(
            @Valid @RequestBody @Parameter(description = "Данные для обновления транзакции") UpdateTransactionDto dto) {
        return financeService.editTransaction(getFinanceIdCurrentUser(), dto);
    }

    /**
     * Фильтрует список транзакций по заданным критериям.
     *
     * @param dto Фильтры для выборки транзакций.
     * @return Возвращает отфильтрованный список транзакций.
     */
    @Operation(summary = "Фильтрация транзакций", description = "Фильтрует транзакции по заданным критериям.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список транзакций успешно отфильтрован",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionDto.class)))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    })
    @PostMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    public List<TransactionDto> listFilterTransaction(
            @Valid @RequestBody @Parameter(description = "Фильтры для транзакций") FilterTransactionDto dto) {
        return financeService.filterTransactions(getFinanceIdCurrentUser(), dto);
    }

    /**
     * Получает список всех транзакций текущего пользователя.
     *
     * @return Возвращает все транзакции текущего пользователя.
     */
    @Operation(summary = "Получение всех транзакций", description = "Возвращает список всех транзакций текущего пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список транзакций успешно получен",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionDto.class)))),
            @ApiResponse(responseCode = "404", description = "Транзакции не найдены")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Set<TransactionDto> listAll() {
        return financeService.list(getFinanceIdCurrentUser());
    }

    private Long getFinanceIdCurrentUser() {
        return UserContext.getCurrentUser().financeId();
    }

}
