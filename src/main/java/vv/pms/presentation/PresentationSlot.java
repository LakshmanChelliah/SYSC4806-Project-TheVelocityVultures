package vv.pms.presentation;

import jakarta.persistence.*;

@Entity
@Table(name = "presentation_slots",
        uniqueConstraints = @UniqueConstraint(columnNames = "projectId"))
public class PresentationSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One presentation per project
    @Column(nullable = false, unique = true)
    private Long projectId;

    @Column(nullable = false)
    private Long roomId;

    // 0-4 = Mon-Fri
    @Column(nullable = false)
    private int dayIndex;

    // 0-15 = 30-min bins from 08:00
    @Column(nullable = false)
    private int startBinIndex;

    // 1 = 30 minutes
    @Column(nullable = false)
    private int durationBins = 1;

    public PresentationSlot() {}

    public PresentationSlot(Long projectId, Long roomId, int dayIndex, int startBinIndex, int durationBins) {
        this.projectId = projectId;
        this.roomId = roomId;
        this.dayIndex = dayIndex;
        this.startBinIndex = startBinIndex;
        this.durationBins = durationBins;
    }

    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public Long getRoomId() { return roomId; }
    public int getDayIndex() { return dayIndex; }
    public int getStartBinIndex() { return startBinIndex; }
    public int getDurationBins() { return durationBins; }

    public void setId(Long id) { this.id = id; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public void setDayIndex(int dayIndex) { this.dayIndex = dayIndex; }
    public void setStartBinIndex(int startBinIndex) { this.startBinIndex = startBinIndex; }
    public void setDurationBins(int durationBins) { this.durationBins = durationBins; }
}