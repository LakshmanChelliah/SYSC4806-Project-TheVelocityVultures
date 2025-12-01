package vv.pms.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.presentation.Room;
import vv.pms.presentation.RoomService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RoomServiceIntegrationTest {

    @Autowired
    private RoomService roomService;

    @Test
    void createRoom_success() {
        Room room = roomService.createRoom("Test Room 101");
        
        assertNotNull(room);
        assertNotNull(room.getId());
        assertEquals("Test Room 101", room.getName());
        assertNotNull(room.getAvailability());
    }

    @Test
    void createRoom_defaultAvailability() {
        Room room = roomService.createRoom("Availability Test Room");
        
        Boolean[][] availability = room.getAvailability();
        assertEquals(5, availability.length); // 5 days
        assertEquals(16, availability[0].length); // 16 bins (30-min slots)
        
        // Default availability is all true
        for (int d = 0; d < 5; d++) {
            for (int t = 0; t < 16; t++) {
                assertTrue(availability[d][t]);
            }
        }
    }

    @Test
    void createRoom_nullName_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                roomService.createRoom(null)
        );
    }

    @Test
    void createRoom_blankName_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                roomService.createRoom("   ")
        );
    }

    @Test
    void createRoom_duplicateName_throws() {
        roomService.createRoom("Duplicate Room");
        
        assertThrows(IllegalArgumentException.class, () ->
                roomService.createRoom("Duplicate Room")
        );
    }

    @Test
    void createRoom_duplicateNameCaseInsensitive_throws() {
        roomService.createRoom("Case Test Room");
        
        assertThrows(IllegalArgumentException.class, () ->
                roomService.createRoom("CASE TEST ROOM")
        );
    }

    @Test
    void createRoom_trimmedName() {
        Room room = roomService.createRoom("  Trim Test Room  ");
        
        assertEquals("Trim Test Room", room.getName());
    }

    @Test
    void updateRoom_success() {
        Room room = roomService.createRoom("Original Name");
        
        Room updated = roomService.updateRoom(room.getId(), "Updated Name");
        
        assertEquals("Updated Name", updated.getName());
        assertEquals(room.getId(), updated.getId());
    }

    @Test
    void updateRoom_sameNameDifferentCase_success() {
        Room room = roomService.createRoom("Same Name Room");
        
        Room updated = roomService.updateRoom(room.getId(), "SAME NAME ROOM");
        
        assertEquals("SAME NAME ROOM", updated.getName());
    }

    @Test
    void updateRoom_notFound_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                roomService.updateRoom(99999L, "New Name")
        );
    }

    @Test
    void updateRoom_nullName_throws() {
        Room room = roomService.createRoom("Update Null Test");
        
        assertThrows(IllegalArgumentException.class, () ->
                roomService.updateRoom(room.getId(), null)
        );
    }

    @Test
    void updateRoom_blankName_throws() {
        Room room = roomService.createRoom("Update Blank Test");
        
        assertThrows(IllegalArgumentException.class, () ->
                roomService.updateRoom(room.getId(), "   ")
        );
    }

    @Test
    void updateRoom_toExistingName_throws() {
        roomService.createRoom("Existing Room");
        Room room = roomService.createRoom("Another Room");
        
        assertThrows(IllegalArgumentException.class, () ->
                roomService.updateRoom(room.getId(), "Existing Room")
        );
    }

    @Test
    void deleteRoom_success() {
        Room room = roomService.createRoom("Delete Test Room");
        Long id = room.getId();
        
        roomService.deleteRoom(id);
        
        assertThrows(IllegalArgumentException.class, () ->
                roomService.getRoomById(id)
        );
    }

    @Test
    void getAllRooms_success() {
        int initialCount = roomService.getAllRooms().size();
        
        roomService.createRoom("Room A");
        roomService.createRoom("Room B");
        roomService.createRoom("Room C");
        
        List<Room> rooms = roomService.getAllRooms();
        
        assertEquals(initialCount + 3, rooms.size());
    }

    @Test
    void getRoomById_success() {
        Room created = roomService.createRoom("Get By Id Room");
        
        Room found = roomService.getRoomById(created.getId());
        
        assertEquals(created.getId(), found.getId());
        assertEquals(created.getName(), found.getName());
    }

    @Test
    void getRoomById_notFound_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                roomService.getRoomById(99999L)
        );
    }

    @Test
    void multipleRooms_uniqueIds() {
        Room room1 = roomService.createRoom("Unique Id Room 1");
        Room room2 = roomService.createRoom("Unique Id Room 2");
        Room room3 = roomService.createRoom("Unique Id Room 3");
        
        assertNotEquals(room1.getId(), room2.getId());
        assertNotEquals(room2.getId(), room3.getId());
        assertNotEquals(room1.getId(), room3.getId());
    }

    @Test
    void updateRoom_preservesAvailability() {
        Room room = roomService.createRoom("Preserve Availability Room");
        Boolean[][] originalAvailability = room.getAvailability();
        
        Room updated = roomService.updateRoom(room.getId(), "New Name");
        
        // Availability should be preserved after name change
        Boolean[][] updatedAvailability = updated.getAvailability();
        assertEquals(originalAvailability.length, updatedAvailability.length);
        assertEquals(originalAvailability[0].length, updatedAvailability[0].length);
    }

    @Test
    void createRoom_withSpecialCharacters() {
        Room room = roomService.createRoom("Room #1 - Building A (Main)");
        
        assertNotNull(room);
        assertEquals("Room #1 - Building A (Main)", room.getName());
    }

    @Test
    void createRoom_longName() {
        String longName = "A".repeat(200);
        Room room = roomService.createRoom(longName);
        
        assertEquals(longName, room.getName());
    }
}
