package dev.fascodes.budgetApp.transaction.exception;

import dev.fascodes.budgetApp.common.ResourceNotFoundException;

public class CategoryNotFoundException extends ResourceNotFoundException {
    public CategoryNotFoundException(Long id) {
        super("Category not found with id: " + id);
    }
}
