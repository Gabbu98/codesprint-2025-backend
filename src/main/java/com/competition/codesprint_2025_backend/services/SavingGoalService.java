package com.competition.codesprint_2025_backend.services;

import com.competition.codesprint_2025_backend.exceptions.NotFoundException;
import com.competition.codesprint_2025_backend.persistence.models.SavingGoalModel;
import com.competition.codesprint_2025_backend.persistence.repositories.SavingGoalRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SavingGoalService {

    private SavingGoalRepository savingGoalRepository;

    public SavingGoalService(SavingGoalRepository savingGoalRepository) {
        this.savingGoalRepository = savingGoalRepository;
    }

    public List<SavingGoalModel> getAllSavingGoals() {
        return this.savingGoalRepository.findAll();
    }

    public SavingGoalModel addSavingGoal(final String name, final BigDecimal target, final BigDecimal saved) {
        final BigDecimal remainder = target.subtract(saved);

        if(remainder.doubleValue() < 0 ) {
            throw new IllegalArgumentException("Remainder calculated is below zero, invalid arguments.");
        }

        return savingGoalRepository.insert(new SavingGoalModel(name, target, saved,remainder));
    }

    public SavingGoalModel updateSavingGoal(final String id, final BigDecimal saved){
        SavingGoalModel savingGoalModel = this.savingGoalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Saving Goal by ID [%s] not found".formatted(id)));

        // Validate saved amount is not negative
        if (saved.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saved amount cannot be negative");
        }

        // Calculate new remainder
        final BigDecimal remainder = savingGoalModel.getTarget().subtract(saved);

        // Validate that saved doesn't exceed target
        if (remainder.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saved amount cannot exceed target amount");
        }

        // Update the model
        savingGoalModel.setSaved(saved);
        savingGoalModel.setRemainder(remainder);

        // Save to repository
        return savingGoalRepository.save(savingGoalModel);
    }

    public void deleteSavingGoal(final String id) {
        this.savingGoalRepository.deleteById(id);
    }
}
