package com.competition.codesprint_2025_backend.controllers;

import com.competition.codesprint_2025_backend.controllers.requests.CreateSavingGoalRequest;
import com.competition.codesprint_2025_backend.controllers.requests.UpdateSavingGoalRequest;
import com.competition.codesprint_2025_backend.controllers.responses.SavingGoalResponse;
import com.competition.codesprint_2025_backend.persistence.models.SavingGoalModel;
import com.competition.codesprint_2025_backend.services.SavingGoalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v0/savings-goals")
public class SavingsGoalsController {

    private SavingGoalService savingGoalService;

    public SavingsGoalsController(SavingGoalService savingGoalService) {
        this.savingGoalService = savingGoalService;
    }

    /**
     * Gets all saving goals
     * GET /api/savings-goals
     */
    @GetMapping
    public ResponseEntity<List<SavingGoalResponse>> getAllSavingGoals() {
        List<SavingGoalModel> savingGoals = savingGoalService.getAllSavingGoals();

        List<SavingGoalResponse> responses = savingGoals.stream()
                .map(SavingGoalResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Creates a new saving goal
     * POST /api/savings-goals
     */
    @PostMapping
    public ResponseEntity<Void> createSavingGoal(@Valid @RequestBody CreateSavingGoalRequest request) {
        savingGoalService.addSavingGoal(request.getName(), request.getTarget(), request.getSaved());
        return ResponseEntity.noContent().build(); // can be created() but i decided to reload all savings since i do not expect too many
    }

    /**
     * Updates the saved amount for an existing saving goal
     * PATCH /api/savings-goals/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateSavingGoal(@PathVariable String id,
                                                 @Valid @RequestBody UpdateSavingGoalRequest request) {
        savingGoalService.updateSavingGoal(id, request.getSaved());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSavingGoal(@PathVariable String id){
        savingGoalService.deleteSavingGoal(id);
        return ResponseEntity.noContent().build();
    }

}
