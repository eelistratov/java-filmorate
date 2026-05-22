package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class User {
    private Integer id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private final Set<Integer> friends = new HashSet<>(); // ID друзей

    public void addFriend(Integer friendId) {
        friends.add(friendId);
    }

    public void removeFriend(Integer friendId) {
        friends.remove(friendId);
    }

    public boolean isFriend(Integer friendId) {
        return friends.contains(friendId);
    }

    public Set<Integer> getFriends() {
        return new HashSet<>(friends);
    }
}