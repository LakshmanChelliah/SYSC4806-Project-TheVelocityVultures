package vv.pms.entity;

import org.junit.jupiter.api.Test;
import vv.pms.availability.Availability;

import static org.junit.jupiter.api.Assertions.*;

class AvailabilityTest {

    @Test
    void testDefaultConstructor() {
        Availability availability = new Availability();
        
        assertNull(availability.getId());
        assertNull(availability.getUserId());
        assertNull(availability.getUserType());
        assertNull(availability.getTimeslots());
    }

    @Test
    void testParameterizedConstructor() {
        Long userId = 1L;
        String userType = "STUDENT";
        Boolean[][] timeslots = new Boolean[5][32];
        
        Availability availability = new Availability(userId, userType, timeslots);
        
        assertEquals(userId, availability.getUserId());
        assertEquals(userType, availability.getUserType());
        assertNotNull(availability.getTimeslots());
    }

    @Test
    void testSettersAndGetters() {
        Availability availability = new Availability();
        
        availability.setUserId(1L);
        availability.setUserType("PROFESSOR");
        Boolean[][] timeslots = new Boolean[5][32];
        timeslots[0][0] = true;
        availability.setTimeslots(timeslots);
        
        assertEquals(1L, availability.getUserId());
        assertEquals("PROFESSOR", availability.getUserType());
        assertTrue(availability.getTimeslots()[0][0]);
    }

    @Test
    void testTimeslotsMatrix() {
        // 5 Days (Mon-Fri) x 32 Time slots (15 mins, 8am-4pm)
        Boolean[][] timeslots = new Boolean[5][32];
        for (int day = 0; day < 5; day++) {
            for (int slot = 0; slot < 32; slot++) {
                timeslots[day][slot] = (day + slot) % 2 == 0;
            }
        }
        
        Availability availability = new Availability(1L, "STUDENT", timeslots);
        
        // Verify matrix structure
        assertEquals(5, availability.getTimeslots().length);
        assertEquals(32, availability.getTimeslots()[0].length);
        
        // Verify pattern
        assertTrue(availability.getTimeslots()[0][0]);   // 0+0 = 0 (even)
        assertFalse(availability.getTimeslots()[0][1]);  // 0+1 = 1 (odd)
        assertFalse(availability.getTimeslots()[1][0]);  // 1+0 = 1 (odd)
        assertTrue(availability.getTimeslots()[1][1]);   // 1+1 = 2 (even)
    }

    @Test
    void testUserTypeValues() {
        Availability studentAvail = new Availability(1L, "STUDENT", new Boolean[5][32]);
        Availability professorAvail = new Availability(2L, "PROFESSOR", new Boolean[5][32]);
        
        assertEquals("STUDENT", studentAvail.getUserType());
        assertEquals("PROFESSOR", professorAvail.getUserType());
    }

    @Test
    void testMatrixConverterToDatabaseColumn() {
        Availability.MatrixConverter converter = new Availability.MatrixConverter();
        Boolean[][] matrix = new Boolean[2][2];
        matrix[0][0] = true;
        matrix[0][1] = false;
        matrix[1][0] = false;
        matrix[1][1] = true;
        
        String json = converter.convertToDatabaseColumn(matrix);
        
        assertNotNull(json);
        assertTrue(json.contains("true"));
        assertTrue(json.contains("false"));
    }

    @Test
    void testMatrixConverterToEntityAttribute() {
        Availability.MatrixConverter converter = new Availability.MatrixConverter();
        String json = "[[true,false],[false,true]]";
        
        Boolean[][] matrix = converter.convertToEntityAttribute(json);
        
        assertNotNull(matrix);
        assertEquals(2, matrix.length);
        assertTrue(matrix[0][0]);
        assertFalse(matrix[0][1]);
        assertFalse(matrix[1][0]);
        assertTrue(matrix[1][1]);
    }

    @Test
    void testMatrixConverterWithNullInput() {
        Availability.MatrixConverter converter = new Availability.MatrixConverter();
        
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void testMatrixConverterRoundTrip() {
        Availability.MatrixConverter converter = new Availability.MatrixConverter();
        Boolean[][] original = new Boolean[5][32];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 32; j++) {
                original[i][j] = (i * j) % 3 == 0;
            }
        }
        
        String json = converter.convertToDatabaseColumn(original);
        Boolean[][] restored = converter.convertToEntityAttribute(json);
        
        assertEquals(original.length, restored.length);
        assertEquals(original[0].length, restored[0].length);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 32; j++) {
                assertEquals(original[i][j], restored[i][j]);
            }
        }
    }
}
