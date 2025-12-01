package vv.pms.presentation.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import vv.pms.presentation.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByNameIgnoreCase(String name);
}
