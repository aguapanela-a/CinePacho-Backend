package CinePacho.demo.movie.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.movie.dto.request.GenreDto;
import CinePacho.demo.movie.dto.request.TmdbMovieDTO;
import CinePacho.demo.movie.dto.response.MovieSelectorDTO;
import CinePacho.demo.movie.dto.response.ScreeningInfoDTO;
import CinePacho.demo.movie.entities.MovieEntity;
import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.movie.repository.MovieRepository;
import CinePacho.demo.movie.repository.MovieScreeningRepository;
import CinePacho.demo.shared.auxiliaryClass.RoomManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@AllArgsConstructor
@Service
public class MovieServices {
    private final RoomManager roomManager;
    private final MovieScreeningRepository movieScreeningRepository;
    private final MovieRepository movieRepository;

    // provee la lista de movieScreenings por multiplexId
    private List<MovieScreening> movieScreeningsByMultiplexId(UUID multiplexId){
        //Lista de los ids de las salas del multiplex ingrsado
        List<UUID> roomIdsByMultiplex = roomManager.getRoomIdsByMultiplexId(multiplexId);

        //usar flatMap para "aplanar" la lista de listas de movieScreenings"
       return roomIdsByMultiplex.stream().flatMap(
                roomId->movieScreeningRepository.findMovieScreeningByRoom_Id(roomId).stream())
               .distinct()
               .collect(Collectors.toList());
    }

    //funcion que junta los Screening de una sola película por multiplex
    private MovieSelectorDTO getScreeningByMultiplex (UUID multiplexId, Long movieId){

        MovieEntity movie = movieRepository.findById(movieId).orElseThrow(()-> new CinePachoException("La película no existe en la base de datos"));

        //Filtrar lista de MovieScreening por película de ese multiplex
        List<MovieScreening> movieScreeningsByMovie = movieScreeningsByMultiplexId(multiplexId)
                .stream()
                .filter(movieScreening -> movieScreening.getMovie().getId().equals(movieId))
                .toList();

        if(movieScreeningsByMovie.isEmpty()){
            throw new CinePachoException("No tenemos funciones disponibles en este multiplex para la pelicula seleccionada");
        }

        //Creo el objeto ScreeningInfoDTO qje recibirá MovieSelectorDTO
        List<ScreeningInfoDTO> screeningInfoDTOList = movieScreeningsByMovie.stream().map(
                screeningMovie -> ScreeningInfoDTO.builder()
                        .screeningId(screeningMovie.getId())
                        .roomId(screeningMovie.getRoom().getId())
                        .roomNumber(screeningMovie.getRoom().getRoomNumber())
                        .screeningDate(screeningMovie.getDateTime())
                        .status(screeningMovie.getStatus())
                        .build()
                ).toList();


        //Casteo de genreEmbeddable a GenreDto
        List<GenreDto> genres = movie.getGenres().stream().map(
                genreEmbeddable -> new GenreDto(genreEmbeddable.getId(), genreEmbeddable.getName())
        ).toList();

        //Creo el TmdbMovieDTO con la info de la peli
        TmdbMovieDTO tmdbMovieDTO =
        TmdbMovieDTO.builder()
                .id(movieId)
                .backdropPath(movie.getBackdropPath())
                .genreIds(genres)
                .originalLanguage(movie.getOriginalLanguage())
                .originalTitle(movie.getOriginalTitle())
                .overview(movie.getOverview())
                .posterPath(movie.getPosterPath())
                .releaseDate(movie.getReleaseDate())
                .director(movie.getDirector())
            .build();

        return MovieSelectorDTO.builder()
                .movieInfo(tmdbMovieDTO)
                .rating(movie.getRating())
                .screenings(screeningInfoDTOList)
            .build();
    }

    //TODO: preguntarle a Claudia ahora que tengo todos los Screeningn de uan sola película ajuntados en un MovieSelectorDTO
    //TODO: como hago para que se carguen automáticamente todos los MovieSelectorDTO de cada peli de ese Múltiplex (opara la pantallade inicio)

}
