package test.controller;

import app.context.UserContext;
import app.controller.TargetController;
import app.dto.user.UserDto;
import app.entity.Role;
import app.service.TargetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TargetControllerTest {

    private MockMvc mockMvc;
    private UserContext userContext;
    private ObjectMapper objectMapper;
    private TargetController targetController;

    @Mock
    private TargetService targetService;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.targetController = new TargetController(targetService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(targetController).build();
        this.userContext = new UserContext();

        this.userContext.setUser(new UserDto(1L, "name", "email@mail.ru", "password", true, Role.USER, 1L));
    }

    @Test
    public void testGetFinanceReport() throws Exception {
        when(targetService.generateFinancialReport()).thenReturn("Report");

        mockMvc.perform(get("/api/v1/target/report"))
                .andExpect(status().isOk());

        verify(targetService).generateFinancialReport();
    }

    @Test
    public void testSetMountBudget() throws Exception {
        BigDecimal budget = new BigDecimal("1000");

        mockMvc.perform(post("/api/v1/target/mount/budget")
                        .param("budget", budget.toString()))
                .andExpect(status().isNoContent());

        verify(targetService).updateMonthlyBudget(any(), any());
    }

    @Test
    public void testGetProgressTowardsGoal() throws Exception {
        when(targetService.getProgressTowardsGoal(any())).thenReturn(50.0);

        mockMvc.perform(post("/api/v1/target"))
                .andExpect(status().isOk());

        verify(targetService).getProgressTowardsGoal(any());
    }

    @Test
    public void testSetGoalGoal() throws Exception {
        BigDecimal goal = new BigDecimal("5000");

        mockMvc.perform(post("/api/v1/target/goal")
                        .param("goal", goal.toString()))
                .andExpect(status().isNoContent());

        verify(targetService).updateGoalSavings(any());
    }

//    @Test
//    public void testGetFinanceReportWhenServiceFails() throws Exception {
//        when(targetService.generateFinancialReport()).thenThrow(new RuntimeException("Service error"));
//
//        mockMvc.perform(get("/api/v1/target/report"))
//                .andExpect(status().isInternalServerError());
//    }
//
//    @Test
//    public void testSetMountBudgetWhenServiceFails() throws Exception {
//        BigDecimal budget = new BigDecimal("1000");
//        doThrow(new RuntimeException("Service error")).when(targetService).updateMonthlyBudget(any(), any());
//
//        mockMvc.perform(post("/api/v1/target/mount/budget")
//                        .param("budget", budget.toString()))
//                .andExpect(status().isInternalServerError());
//    }
//
//    @Test
//    public void testGetProgressTowardsGoalWhenServiceFails() throws Exception {
//        when(targetService.getProgressTowardsGoal(any())).thenThrow(new RuntimeException("Service error"));
//
//        mockMvc.perform(post("/api/v1/target"))
//                .andExpect(status().isInternalServerError());
//    }
//
//    @Test
//    public void testSetGoalGoalWhenServiceFails() throws Exception {
//        BigDecimal goal = new BigDecimal("5000");
//        doThrow(new RuntimeException("Service error")).when(targetService).updateGoalSavings(any());
//
//        mockMvc.perform(post("/api/v1/target/goal")
//                        .param("goal", goal.toString()))
//                .andExpect(status().isInternalServerError());
//    }

    @Test
    public void testSetMountBudgetWithInvalidParam() throws Exception {
        mockMvc.perform(post("/api/v1/target/mount/budget")
                        .param("budget", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSetGoalGoalWithInvalidParam() throws Exception {
        mockMvc.perform(post("/api/v1/target/goal")
                        .param("goal", "invalid"))
                .andExpect(status().isBadRequest());
    }

//    @Test
//    public void testGetFinanceReportWhenServiceReturnsEmpty() throws Exception {
//        when(targetService.generateFinancialReport()).thenReturn("");
//
//        mockMvc.perform(get("/api/v1/target/report"))
//                .andExpect(status().isNoContent());
//    }

    @Test
    public void testGetProgressTowardsGoalWhenServiceReturnsNull() throws Exception {
        when(targetService.getProgressTowardsGoal(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/target"))
                .andExpect(status().isOk());
    }
}