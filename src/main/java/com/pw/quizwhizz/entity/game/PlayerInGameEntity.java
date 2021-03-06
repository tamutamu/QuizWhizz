package com.pw.quizwhizz.entity.game;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Encja PlayerInGame zawierajaca informacje na temat gracza w kontekscie konkretnej gry.
 * Okresla, czy gracz jest wlascicielem-zalozycielem danej gry.
 *
 * @author Karolina Prusaczyk
 */
@Entity
@Getter @Setter
@Table(name = "player_in_game")
public class PlayerInGameEntity {
    @EmbeddedId
    private PlayerInGameKey id;

    @Column(columnDefinition = "TINYINT(1)", name = "owner")
    private boolean isOwner;
}
