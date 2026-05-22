package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class Film {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private final Set<Integer> likes = new HashSet<>(); // ID пользователей, кто лайкнул

    public void addLike(Integer userId) {
        likes.add(userId);
    }

    public void removeLike(Integer userId) {
        likes.remove(userId);
    }

    public boolean isLikedByUser(Integer userId) {
        return likes.contains(userId);
    }

    public int getLikesCount() {
        return likes.size();
    }

    public Set<Integer> getLikes() {
        return new HashSet<>(likes);
    }
}