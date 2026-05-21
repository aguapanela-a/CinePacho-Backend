package CinePacho.demo.rooms.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.multiplex.entitites.MultiplexEntity;
import CinePacho.demo.rooms.entities.RoomEntity;
import CinePacho.demo.rooms.repository.RoomRepository;
import CinePacho.demo.shared.auxiliaryClass.MultiplexProvider;
import CinePacho.demo.shared.auxiliaryClass.RoomManager;
import CinePacho.demo.shared.auxiliaryClass.SeatManager;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RoomManagerImpl implements RoomManager {

    private final RoomRepository roomRepository;
    private final MultiplexProvider multiplexProvider;
    private final SeatManager seatManager;


    public RoomManagerImpl(RoomRepository roomRepository, MultiplexProvider multiplexProvider, SeatManager seatManager) {
        this.roomRepository = roomRepository;
        this.multiplexProvider = multiplexProvider;
        this.seatManager = seatManager;
    }

    @Override
    public RoomEntity getRoom(UUID id) {
        return roomRepository.findById(id).orElseThrow(()-> new CinePachoException("Room not found"));
    }

    @Override
    public void createRoom(MultiplexEntity multiplex) {

        int generalCapacity = 40;
        int preferentialCapacity = 20;

        RoomEntity room = RoomEntity.builder()
                .multiplex(multiplex)
                .generalCapacity(generalCapacity)
                .preferentialCapacity(preferentialCapacity)
                .build();

        //pone que la hora de creación sea la actual
        room.prePersist();

        //Guardar entidad sala en BD
        RoomEntity roomSaved = roomRepository.save(room);

        //Crear e insertar sillas físicas asociadas a esa sala
        seatManager.createSeat(generalCapacity, preferentialCapacity, roomSaved);
    }
}
