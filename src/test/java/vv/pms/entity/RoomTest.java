package vv.pms.entity;

import org.junit.jupiter.api.Test;
import vv.pms.presentation.Room;

import static org.junit.jupiter.api.Assertions.*;

class RoomTest {

    @Test
    void testDefaultConstructor() {
        Room room = new Room();
        
        assertNull(room.getId());
        assertNull(room.getName());
        assertNull(room.getAvailability());
    }

    @Test
    void testConstructorWithName() {
        Room room = new Room("Room 101");
        
        assertNull(room.getId());
        assertEquals("Room 101", room.getName());
        assertNotNull(room.getAvailability());
        
        // Check default full availability (5 days x 16 bins, all true)
        Boolean[][] availability = room.getAvailability();
        assertEquals(5, availability.length);
        assertEquals(16, availability[0].length);
        
        for (int d = 0; d < 5; d++) {
            for (int t = 0; t < 16; t++) {
                assertTrue(availability[d][t], "Default availability should be true");
            }
        }
    }

    @Test
    void testConstructorWithNameAndAvailability() {
        Boolean[][] customAvailability = new Boolean[5][16];
        for (int d = 0; d < 5; d++) {
            for (int t = 0; t < 16; t++) {
                customAvailability[d][t] = (d + t) % 2 == 0;
            }
        }
        
        Room room = new Room("Conference Room A", customAvailability);
        
        assertEquals("Conference Room A", room.getName());
        assertArrayEquals(customAvailability, room.getAvailability());
    }

    @Test
    void testSettersAndGetters() {
        Room room = new Room();
        
        room.setId(1L);
        room.setName("Lab 200");
        Boolean[][] availability = new Boolean[5][16];
        availability[0][0] = true;
        room.setAvailability(availability);
        
        assertEquals(1L, room.getId());
        assertEquals("Lab 200", room.getName());
        assertTrue(room.getAvailability()[0][0]);
    }

    @Test
    void testNameChange() {
        Room room = new Room("Old Name");
        
        room.setName("New Name");
        
        assertEquals("New Name", room.getName());
    }

    @Test
    void testAvailabilityMatrixDimensions() {
        Room room = new Room("Test Room");
        Boolean[][] availability = room.getAvailability();
        
        // 5 days (Mon-Fri)
        assertEquals(5, availability.length);
        
        // 16 bins (30-min bins from 08:00-16:00)
        for (Boolean[] dayAvailability : availability) {
            assertEquals(16, dayAvailability.length);
        }
    }

    @Test
    void testSetPartialAvailability() {
        Room room = new Room("Partially Available Room");
        Boolean[][] availability = room.getAvailability();
        
        // Block Monday morning (bins 0-3 = 8:00-10:00)
        for (int t = 0; t < 4; t++) {
            availability[0][t] = false;
        }
        room.setAvailability(availability);
        
        assertFalse(room.getAvailability()[0][0]);
        assertFalse(room.getAvailability()[0][1]);
        assertFalse(room.getAvailability()[0][2]);
        assertFalse(room.getAvailability()[0][3]);
        assertTrue(room.getAvailability()[0][4]); // Still available from 10:00
    }

    @Test
    void testRoomWithNoAvailability() {
        Boolean[][] noAvailability = new Boolean[5][16];
        for (int d = 0; d < 5; d++) {
            for (int t = 0; t < 16; t++) {
                noAvailability[d][t] = false;
            }
        }
        
        Room room = new Room("Unavailable Room", noAvailability);
        
        for (int d = 0; d < 5; d++) {
            for (int t = 0; t < 16; t++) {
                assertFalse(room.getAvailability()[d][t]);
            }
        }
    }

    @Test
    void testMultipleRoomsIndependentAvailability() {
        Room room1 = new Room("Room 1");
        Room room2 = new Room("Room 2");
        
        // Modify room1's availability
        room1.getAvailability()[0][0] = false;
        
        // Room2 should still have full availability
        assertTrue(room2.getAvailability()[0][0]);
    }

    @Test
    void testIdAssignment() {
        Room room = new Room("Test Room");
        assertNull(room.getId());
        
        room.setId(100L);
        
        assertEquals(100L, room.getId());
    }
}
