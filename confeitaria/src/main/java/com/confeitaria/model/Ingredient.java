package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "ingredients")
public class Ingredient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private BigDecimal pricePerKg; // price per kg in BRL
    private String unit = "kg"; // kg, L, un
}
