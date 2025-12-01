package vv.pms.presentation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.availability.Availability;
import vv.pms.availability.AvailabilityService;
import vv.pms.allocation.AllocationService;
import vv.pms.allocation.ProjectAllocation;
import vv.pms.presentation.internal.PresentationSlotRepository;
import vv.pms.presentation.internal.RoomRepository;
import vv.pms.professor.Professor;
import vv.pms.professor.ProfessorService;
import vv.pms.project.Project;
import vv.pms.project.ProjectService;
import vv.pms.student.Student;
import vv.pms.student.StudentService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PresentationService {

    private final RoomRepository roomRepository;
    private final PresentationSlotRepository slotRepository;
    private final AvailabilityService availabilityService;
    private final AllocationService allocationService;
    private final ProjectService projectService;
    private final ProfessorService professorService;
    private final StudentService studentService;

    // 5 days, 16 bins of 30 minutes (8:00–16:00)
    private static final int DAYS = 5;
    private static final int BINS = 16;
    private static final int DURATION_BINS = 1; // 30 minutes

    private static final String[] DAY_NAMES = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"
    };

    public record SlotOption(int dayIndex, int startBinIndex, String label) {}

    public record PresentationRow(
            Long projectId,
            String projectTitle,
            String professorName,
            String studentNames,
            Long roomId,
            String slotLabel
    ) {}

    public PresentationService(RoomRepository roomRepository,
                               PresentationSlotRepository slotRepository,
                               AvailabilityService availabilityService,
                               AllocationService allocationService,
                               ProjectService projectService,
                               ProfessorService professorService,
                               StudentService studentService) {
        this.roomRepository = roomRepository;
        this.slotRepository = slotRepository;
        this.availabilityService = availabilityService;
        this.allocationService = allocationService;
        this.projectService = projectService;
        this.professorService = professorService;
        this.studentService = studentService;
    }

    // -----------------------------
    // Room helpers (used by controller or RoomService)
    // -----------------------------
    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    // -----------------------------
    // Presentation assignment
    // -----------------------------
    public PresentationSlot assignPresentation(Long projectId,
                                               Long roomId,
                                               int dayIndex,
                                               int startBinIndex) {

        if (dayIndex < 0 || dayIndex >= DAYS) {
            throw new IllegalArgumentException("Invalid day index: " + dayIndex);
        }
        if (startBinIndex < 0 || startBinIndex >= BINS) {
            throw new IllegalArgumentException("Invalid start-bin index: " + startBinIndex);
        }

        Project project = projectService.findProjectById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project " + projectId + " not found"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room " + roomId + " not found"));

        if (hasRoomConflict(roomId, dayIndex, startBinIndex, DURATION_BINS, projectId)) {
            throw new IllegalStateException("Room is already booked at that time.");
        }

        PresentationSlot slot = slotRepository.findByProjectId(projectId)
                .orElseGet(PresentationSlot::new);

        slot.setProjectId(project.getId());
        slot.setRoomId(room.getId());
        slot.setDayIndex(dayIndex);
        slot.setStartBinIndex(startBinIndex);
        slot.setDurationBins(DURATION_BINS);

        return slotRepository.save(slot);
    }

    public void unassignPresentation(Long projectId) {
        slotRepository.findByProjectId(projectId).ifPresent(slotRepository::delete);
    }

    @Transactional(readOnly = true)
    public Optional<PresentationSlot> findByProjectId(Long projectId) {
        return slotRepository.findByProjectId(projectId);
    }

    // -----------------------------
    // Available slots for a project + room
    // -----------------------------
    @Transactional(readOnly = true)
    public List<SlotOption> getAvailableSlots(Long projectId, Long roomId) {
        ProjectAllocation allocation = allocationService.findAllocationByProjectId(projectId).orElse(null);
        if (allocation == null || allocation.getAssignedStudentIds().isEmpty()) {
            return List.of();
        }

        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return List.of();
        }
        Boolean[][] roomAvail = normalize(room.getAvailability());

        Long professorId = allocation.getProfessorId();
        Professor prof = professorService.findProfessorById(professorId).orElse(null);
        if (prof == null) {
            return List.of();
        }
        Availability profAvailEntity = availabilityService.getAvailability(professorId, "PROFESSOR");
        Boolean[][] profAvail = normalize(profAvailEntity.getTimeslots());

        List<Long> studentIds = allocation.getAssignedStudentIds();
        List<Boolean[][]> studentMatrices = new ArrayList<>();
        for (Long sid : studentIds) {
            Student s = studentService.findStudentById(sid).orElse(null);
            if (s == null) continue;
            Availability sAvailEntity = availabilityService.getAvailability(sid, "STUDENT");
            studentMatrices.add(normalize(sAvailEntity.getTimeslots()));
        }

        boolean[][] intersection = new boolean[DAYS][BINS];
        for (int d = 0; d < DAYS; d++) {
            for (int t = 0; t < BINS; t++) {
                boolean ok = bool(roomAvail[d][t]) && bool(profAvail[d][t]);
                for (Boolean[][] sm : studentMatrices) {
                    ok = ok && bool(sm[d][t]);
                    if (!ok) break;
                }
                intersection[d][t] = ok;
            }
        }

        Map<Integer, Set<Integer>> occupied = occupiedBinsForRoom(roomId, projectId);

        List<SlotOption> result = new ArrayList<>();
        for (int d = 0; d < DAYS; d++) {
            for (int t = 0; t <= BINS - DURATION_BINS; t++) {
                boolean blockOk = true;
                for (int k = 0; k < DURATION_BINS; k++) {
                    if (!intersection[d][t + k]) {
                        blockOk = false;
                        break;
                    }
                }
                if (!blockOk) continue;
                if (binRangeOverlaps(occupied, d, t, DURATION_BINS)) {
                    continue;
                }
                String label = formatSlotLabel(d, t, DURATION_BINS);
                result.add(new SlotOption(d, t, label));
            }
        }

        return result.stream()
                .sorted(Comparator.comparingInt(SlotOption::dayIndex)
                        .thenComparingInt(SlotOption::startBinIndex))
                .collect(Collectors.toList());
    }

    private Boolean[][] normalize(Boolean[][] src) {
        Boolean[][] matrix = new Boolean[DAYS][BINS];
        for (int d = 0; d < DAYS; d++) {
            for (int t = 0; t < BINS; t++) {
                Boolean v = (src != null && d < src.length && src[d] != null && t < src[d].length)
                        ? src[d][t]
                        : Boolean.FALSE;
                matrix[d][t] = (v != null ? v : Boolean.FALSE);
            }
        }
        return matrix;
    }

    private boolean bool(Boolean b) {
        return b != null && b;
    }

    private Map<Integer, Set<Integer>> occupiedBinsForRoom(Long roomId, Long projectIdToIgnore) {
        List<PresentationSlot> slots = slotRepository.findByRoomId(roomId);
        Map<Integer, Set<Integer>> map = new HashMap<>();
        for (PresentationSlot s : slots) {
            if (projectIdToIgnore != null && projectIdToIgnore.equals(s.getProjectId())) {
                continue;
            }
            int d = s.getDayIndex();
            int start = s.getStartBinIndex();
            int dur = s.getDurationBins();
            Set<Integer> set = map.computeIfAbsent(d, k -> new HashSet<>());
            for (int t = start; t < start + dur; t++) {
                set.add(t);
            }
        }
        return map;
    }

    private boolean binRangeOverlaps(Map<Integer, Set<Integer>> occupied,
                                     int dayIndex,
                                     int startBin,
                                     int dur) {
        Set<Integer> used = occupied.get(dayIndex);
        if (used == null) return false;
        for (int t = startBin; t < startBin + dur; t++) {
            if (used.contains(t)) return true;
        }
        return false;
    }

    private String formatSlotLabel(int dayIndex, int startBinIndex, int durBins) {
        String day = DAY_NAMES[dayIndex];
        int startMinutes = 8 * 60 + startBinIndex * 30;
        int endMinutes = startMinutes + durBins * 30;

        return String.format(
                "%s %02d:%02d-%02d:%02d",
                day,
                startMinutes / 60, startMinutes % 60,
                endMinutes / 60, endMinutes % 60
        );
    }

    @Transactional(readOnly = true)
    public String describeSlot(PresentationSlot slot) {
        if (slot == null) return null;
        return formatSlotLabel(slot.getDayIndex(), slot.getStartBinIndex(), slot.getDurationBins());
    }

    // -----------------------------
    // Best-effort allocation
    // -----------------------------
    public void runBestEffortAllocation() {
        List<Room> rooms = roomRepository.findAll();
        if (rooms.isEmpty()) return;

        List<ProjectAllocation> allocations = allocationService.findAllAllocations().stream()
                .filter(a -> !a.getAssignedStudentIds().isEmpty())
                .collect(Collectors.toList());

        for (ProjectAllocation allocation : allocations) {
            Long pid = allocation.getProjectId();
            if (!projectService.findProjectById(pid).isPresent()) continue;
            if (slotRepository.findByProjectId(pid).isPresent()) continue;

            for (Room room : rooms) {
                List<SlotOption> options = getAvailableSlots(pid, room.getId());
                if (!options.isEmpty()) {
                    SlotOption first = options.get(0);
                    try {
                        assignPresentation(pid, room.getId(), first.dayIndex(), first.startBinIndex());
                        break;
                    } catch (RuntimeException ignored) {
                    }
                }
            }
        }
    }

    // -----------------------------
    // View model for /presentations
    // -----------------------------
    @Transactional(readOnly = true)
    public List<PresentationRow> buildPresentationRows() {
        List<ProjectAllocation> allocations = allocationService.findAllAllocations().stream()
                .filter(a -> !a.getAssignedStudentIds().isEmpty())
                .collect(Collectors.toList());

        List<PresentationRow> rows = new ArrayList<>();

        for (ProjectAllocation alloc : allocations) {
            Long projectId = alloc.getProjectId();
            Project project = projectService.findProjectById(projectId).orElse(null);
            if (project == null) continue;

            Professor prof = professorService.findProfessorById(alloc.getProfessorId()).orElse(null);
            String profName = prof != null ? prof.getName() : "(unknown)";

            List<Long> studentIds = alloc.getAssignedStudentIds();
            String studentNames = studentIds.stream()
                    .map(id -> studentService.findStudentById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .map(Student::getName)
                    .collect(Collectors.joining(", "));

            PresentationSlot slot = slotRepository.findByProjectId(projectId).orElse(null);
            Long roomId = slot != null ? slot.getRoomId() : null;
            String slotLabel = describeSlot(slot);

            rows.add(new PresentationRow(
                    projectId,
                    project.getTitle(),
                    profName,
                    studentNames,
                    roomId,
                    slotLabel
            ));
        }

        return rows;
    }

    private boolean hasRoomConflict(Long roomId, int dayIndex, int startBinIndex, int durationBins, Long projectIdToIgnore) {
        Map<Integer, Set<Integer>> occupied = occupiedBinsForRoom(roomId, projectIdToIgnore);

        Set<Integer> used = occupied.get(dayIndex);
        if (used == null) return false;

        for (int t = startBinIndex; t < startBinIndex + durationBins; t++) {
            if (used.contains(t)) {
                return true;
            }
        }
        return false;
    }

}