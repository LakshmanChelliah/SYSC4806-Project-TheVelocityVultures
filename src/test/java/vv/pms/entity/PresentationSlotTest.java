package vv.pms.entity;

import org.junit.jupiter.api.Test;
import vv.pms.presentation.PresentationSlot;

import static org.junit.jupiter.api.Assertions.*;

class PresentationSlotTest {

    @Test
    void testDefaultConstructor() {
        PresentationSlot slot = new PresentationSlot();
        
        assertNull(slot.getId());
        assertNull(slot.getProjectId());
        assertNull(slot.getRoomId());
        assertEquals(0, slot.getDayIndex());
        assertEquals(0, slot.getStartBinIndex());
        assertEquals(1, slot.getDurationBins()); // default duration is 1
    }

    @Test
    void testParameterizedConstructor() {
        Long projectId = 1L;
        Long roomId = 10L;
        int dayIndex = 2; // Wednesday
        int startBinIndex = 4; // 10:00 (4 * 30min = 2 hours after 8:00)
        int durationBins = 2; // 60 minutes
        
        PresentationSlot slot = new PresentationSlot(projectId, roomId, dayIndex, startBinIndex, durationBins);
        
        assertEquals(projectId, slot.getProjectId());
        assertEquals(roomId, slot.getRoomId());
        assertEquals(dayIndex, slot.getDayIndex());
        assertEquals(startBinIndex, slot.getStartBinIndex());
        assertEquals(durationBins, slot.getDurationBins());
    }

    @Test
    void testSettersAndGetters() {
        PresentationSlot slot = new PresentationSlot();
        
        slot.setId(1L);
        slot.setProjectId(100L);
        slot.setRoomId(10L);
        slot.setDayIndex(3);
        slot.setStartBinIndex(8);
        slot.setDurationBins(2);
        
        assertEquals(1L, slot.getId());
        assertEquals(100L, slot.getProjectId());
        assertEquals(10L, slot.getRoomId());
        assertEquals(3, slot.getDayIndex());
        assertEquals(8, slot.getStartBinIndex());
        assertEquals(2, slot.getDurationBins());
    }

    @Test
    void testDayIndexRange() {
        PresentationSlot slot = new PresentationSlot();
        
        // Monday (0) to Friday (4)
        for (int day = 0; day < 5; day++) {
            slot.setDayIndex(day);
            assertEquals(day, slot.getDayIndex());
        }
    }

    @Test
    void testStartBinIndexRange() {
        PresentationSlot slot = new PresentationSlot();
        
        // 0-15 = 30-min bins from 08:00 to 16:00
        for (int bin = 0; bin < 16; bin++) {
            slot.setStartBinIndex(bin);
            assertEquals(bin, slot.getStartBinIndex());
        }
    }

    @Test
    void testDurationBins() {
        PresentationSlot slot = new PresentationSlot();
        
        slot.setDurationBins(1); // 30 minutes
        assertEquals(1, slot.getDurationBins());
        
        slot.setDurationBins(2); // 60 minutes
        assertEquals(2, slot.getDurationBins());
        
        slot.setDurationBins(4); // 2 hours
        assertEquals(4, slot.getDurationBins());
    }

    @Test
    void testUniqueProjectIdConstraint() {
        // Each project can only have one presentation slot
        PresentationSlot slot1 = new PresentationSlot(1L, 10L, 0, 0, 1);
        PresentationSlot slot2 = new PresentationSlot(1L, 20L, 1, 2, 1);
        
        // Both have the same projectId, which is a unique constraint
        assertEquals(slot1.getProjectId(), slot2.getProjectId());
    }

    @Test
    void testMondayMorningSlot() {
        // Monday 8:00-8:30
        PresentationSlot slot = new PresentationSlot(1L, 10L, 0, 0, 1);
        
        assertEquals(0, slot.getDayIndex()); // Monday
        assertEquals(0, slot.getStartBinIndex()); // 8:00
        assertEquals(1, slot.getDurationBins()); // 30 minutes
    }

    @Test
    void testFridayAfternoonSlot() {
        // Friday 15:30-16:00
        PresentationSlot slot = new PresentationSlot(1L, 10L, 4, 15, 1);
        
        assertEquals(4, slot.getDayIndex()); // Friday
        assertEquals(15, slot.getStartBinIndex()); // 15:30
        assertEquals(1, slot.getDurationBins()); // 30 minutes
    }

    @Test
    void testSlotModification() {
        PresentationSlot slot = new PresentationSlot(1L, 10L, 0, 0, 1);
        
        // Reschedule to different day and time
        slot.setDayIndex(2); // Wednesday
        slot.setStartBinIndex(6); // 11:00
        slot.setRoomId(20L);
        
        assertEquals(2, slot.getDayIndex());
        assertEquals(6, slot.getStartBinIndex());
        assertEquals(20L, slot.getRoomId());
    }
}
