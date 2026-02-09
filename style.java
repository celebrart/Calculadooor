package com.eazynotes.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
@Data // Gera getters e setters automaticamente
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDateTime scheduledTime;

    private String category;

    private String hexColor; // Cor personalizada escolhida no front

    private boolean isLocked = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Lógica de negócio: Verifica se falta menos de 10 min
    public boolean isImminent() {
        LocalDateTime now = LocalDateTime.now();
        return scheduledTime.isAfter(now) && 
               scheduledTime.isBefore(now.plusMinutes(10));
    }
}
