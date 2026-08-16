package org.app.movie.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Genre {

    ACTION("Action"),
    ADVENTURE("Adventure"),
    ANIMATION("Animation"),
    COMEDY("Comedy"),
    CRIME("Crime"),
    DOCUMENTARY("Documentary"),
    DRAMA("Drama"),
    FANTASY("Fantasy"),
    HORROR("Horror"),
    MYSTERY("Mystery"),
    ROMANCE("Romance"),
    SCIENCE_FICTION("Science Fiction"),
    THRILLER("Thriller"),
    WAR("War"),
    WESTERN("Western"),
    FAMILY("Family"),
    HISTORY("History"),
    MUSIC("Music"),
    MUSICAL("Musical"),
    SPORT("Sport"),
    BIOGRAPHY("Biography");

    private final String displayName;
}
