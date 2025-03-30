package app.controller;

import app.context.UserContext;
import app.controller.advice.annotation.CustomExceptionHandler;
import app.service.TargetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/target")
@RequiredArgsConstructor
@CustomExceptionHandler
public class TargetController {

    private final TargetService targetService;

    @GetMapping("/report")
    @ResponseStatus(HttpStatus.OK)
    public String getFinanceReport() {
        return targetService.generateFinancialReport();
    }

    @PostMapping("/mount/budget")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setMountBudget(@RequestParam(name = "budget") BigDecimal budget) {
        targetService.updateMonthlyBudget(getFinanceIdCurrentUser(), budget);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Double getProgressTowardsGoal(){
        return targetService.getProgressTowardsGoal(getFinanceIdCurrentUser());
    }

    @PostMapping("/goal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setGoalGoal(@RequestParam(name = "goal") BigDecimal goal) {
        targetService.updateGoalSavings(goal);
    }

    private Long getFinanceIdCurrentUser() {
        return UserContext.getCurrentUser().financeId();
    }

}
