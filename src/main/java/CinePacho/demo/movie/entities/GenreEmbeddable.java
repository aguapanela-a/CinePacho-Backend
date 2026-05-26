package CinePacho.demo.movie.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenreEmbeddable {

    @Column(name = "genre_id")
    private Integer id;

    @Column(name = "genre_name")
    private String name;
}
