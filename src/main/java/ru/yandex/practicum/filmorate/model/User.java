package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class User {
    private Integer id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен содержать символ @")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения должна быть указана")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    private Set<Friendship> friendships = new HashSet<>();

    // Добавляет друга с указанным статусом
    public void addFriend(Integer friendId, FriendshipStatus status) {
        friendships.add(new Friendship(this.id, friendId, status));
    }

    // Добавляет друга со статусом UNCONFIRMED (неподтверждённая дружба)
    public void addFriend(Integer friendId) {
        friendships.add(new Friendship(this.id, friendId, FriendshipStatus.UNCONFIRMED));
    }

    // Удаляет пользователя из друзей
    public void removeFriend(Integer friendId) {
        friendships.removeIf(f -> f.getFriendId().equals(friendId));
    }

    // Проверяет, является ли пользователь другом (независимо от статуса)
    public boolean isFriend(Integer friendId) {
        return friendships.stream().anyMatch(f -> f.getFriendId().equals(friendId));
    }

    // Проверяет, является ли пользователь подтверждённым другом
    public boolean isFriendConfirmed(Integer friendId) {
        return friendships.stream()
                .anyMatch(f -> f.getFriendId().equals(friendId) &&
                        f.getStatus() == FriendshipStatus.CONFIRMED);
    }

    // Возвращает множество ID всех друзей (без учёта статуса)
    public Set<Integer> getFriends() {
        return friendships.stream()
                .map(Friendship::getFriendId)
                .collect(Collectors.toSet());
    }

    // Возвращает копию множества связей дружбы
    public Set<Friendship> getFriendships() {
        return new HashSet<>(friendships);
    }
}