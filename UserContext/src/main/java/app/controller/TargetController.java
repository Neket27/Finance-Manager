package app.controller;

import app.controller.advice.annotation.CustomExceptionHandler;
import app.entity.User;
import app.service.TargetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import neket27.context.UserContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/target")
@RequiredArgsConstructor
@CustomExceptionHandler
public class TargetController {

    private final TargetService targetService;

    /**
     * Генерирует финансовый отчет.
     *
     * @return Возвращает строку, представляющую финансовый отчет.
     */
    @Operation(summary = "Генерация финансового отчета", description = "Генерирует финансовый отчет для текущего пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Финансовый отчет успешно сгенерирован")
    })
    @GetMapping("/report")
    @ResponseStatus(HttpStatus.OK)
    public String getFinanceReport() {
        return targetService.generateFinancialReport();
    }

    /**
     * Устанавливает месячный бюджет.
     *
     * @param budget Месячный бюджет для текущего пользователя.
     */
    @Operation(summary = "Установка месячного бюджета", description = "Устанавливает месячный бюджет для текущего пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Месячный бюджет успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    })
    @PostMapping("/mount/budget")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setMountBudget(
            @RequestParam(name = "budget") @Parameter(description = "Месячный бюджет") BigDecimal budget) {
        targetService.updateMonthlyBudget(getFinanceIdCurrentUser(), budget);
    }

    /**
     * Получает прогресс достижения цели.
     *
     * @return Возвращает процент достижения цели.
     */
    @Operation(summary = "Получение прогресса достижения цели", description = "Возвращает текущий прогресс в достижении финансовой цели для текущего пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Прогресс в достижении цели успешно получен")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Double getProgressTowardsGoal() {
        return targetService.getProgressTowardsGoal(getFinanceIdCurrentUser());
    }

    /**
     * Устанавливает финансовую цель.
     *
     * @param goal Финансовая цель для текущего пользователя.
     */
    @Operation(summary = "Установка финансовой цели", description = "Устанавливает финансовую цель для текущего пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Финансовая цель успешно обновлена"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    })
    @PostMapping("/goal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setGoalGoal(
            @RequestParam(name = "goal") @Parameter(description = "Финансовая цель") BigDecimal goal) {
        targetService.updateGoalSavings(goal);
    }

    private Long getFinanceIdCurrentUser() {
        User user = (User) UserContext.getCurrentUser();
        return user.getFinanceId();
    }
}
