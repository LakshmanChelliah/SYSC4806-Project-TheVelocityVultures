package vv.pms.presentation.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import vv.pms.presentation.PresentationSlot;

import java.util.List;
import java.util.Optional;

public interface PresentationSlotRepository extends JpaRepository<PresentationSlot, Long> {

    Optional<PresentationSlot> findByProjectId(Long projectId);

    List<PresentationSlot> findByRoomId(Long roomId);
}
