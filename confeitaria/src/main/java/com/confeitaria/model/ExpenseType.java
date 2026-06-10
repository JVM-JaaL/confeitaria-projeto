package com.confeitaria.model;

// Classifica um gasto mensal.
// FIXO → entra no cálculo de rateio de custo fixo por unidade de produção (RecipeService).
// EVENTUAL → registrado para controle, mas não afeta o preço sugerido das receitas.
public enum ExpenseType {
    FIXO,
    EVENTUAL
}
