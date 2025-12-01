package vv.pms.presentation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.presentation.internal.RoomRepository;

import java.util.List;

@Service
@Transactional
public class RoomService {

    private final RoomRepository repository;

    public RoomService(RoomRepository repository) {
        this.repository = repository;
    }

    public Room createRoom(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Room name is required");
        }
        if (repository.existsByNameIgnoreCase(name.trim())) {
            throw new IllegalArgumentException("Room with that name already exists");
        }
        Room room = new Room(name.trim());
        return repository.save(room);
    }

    public Room updateRoom(Long id, String name) {
        Room room = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + id));
        String cleaned = name == null ? "" : name.trim();
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("Room name is required");
        }
        if (!cleaned.equalsIgnoreCase(room.getName())
                && repository.existsByNameIgnoreCase(cleaned)) {
            throw new IllegalArgumentException("Room with that name already exists");
        }
        room.setName(cleaned);
        return repository.save(room);
    }

    public void deleteRoom(Long id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Room getRoomById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + id));
    }
}
