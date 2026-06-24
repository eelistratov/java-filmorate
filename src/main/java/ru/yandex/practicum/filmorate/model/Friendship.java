package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {
    private Integer userId;
    private Integer friendId;
    private FriendshipStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Конструктор для создания новой дружбы с указанным статусом
    public Friendship(Integer userId, Integer friendId, FriendshipStatus status) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Конструктор для создания новой дружбы со статусом UNCONFIRMED по умолчанию
    public Friendship(Integer userId, Integer friendId) {
        this(userId, friendId, FriendshipStatus.UNCONFIRMED);
    }
}