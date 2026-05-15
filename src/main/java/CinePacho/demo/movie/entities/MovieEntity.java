package CinePacho.demo.movie.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "movies")
public class MovieEntity {

    @Id
    private Long id;

    private Boolean adult;
    private String backdropPath;
    private String originalLanguage;
    private String originalTitle;
    private String overview;
    private Double rating;
    private String posterPath;
    private String releaseDate;
    private String director;

    //crea una tabla para los ids de los géneros de las peliculas
    @ElementCollection
    @CollectionTable(name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "genre_id")
    private List<Integer> genres = new ArrayList<>();
}