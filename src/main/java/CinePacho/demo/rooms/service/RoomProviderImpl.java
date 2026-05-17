package CinePacho.demo.rooms.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.rooms.entities.RoomEntity;
import CinePacho.demo.rooms.repository.RoomRepository;
import CinePacho.demo.shared.auxiliaryClass.RoomProvider;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RoomProviderImpl implements RoomProvider {

    private final RoomRepository roomRepository;

    public RoomProviderImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public RoomEntity getRoom(UUID id) {
        return roomRepository.findById(id).orElseThrow(()-> new CinePachoException("Room not found"));
    }
}
