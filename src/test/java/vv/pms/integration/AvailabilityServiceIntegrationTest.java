package vv.pms.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.availability.Availability;
import vv.pms.availability.AvailabilityService;
import vv.pms.professor.Professor;
import vv.pms.professor.ProfessorService;
import vv.pms.project.Program;
import vv.pms.student.Student;
import vv.pms.student.StudentService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AvailabilityServiceIntegrationTest {

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private ProfessorService professorService;

    @Autowired
    private StudentService studentService;

    private Professor professor;
    private Student student;

    @BeforeEach
    void setUp() {
        professor = professorService.addProfessor("Prof. Avail", "prof.avail@university.edu");
        student = studentService.addStudent("Avail Student", "AVAIL001", "avail.student@university.edu", Program.SOFTWARE_ENGINEERING);
    }

    @Test
    void getAvailability_createsDefaultForNewProfessor() {
        Availability availability = availabilityService.getAvailability(professor.getId(), "PROFESSOR");
        
        assertNotNull(availability);
        assertNotNull(availability.getId());
        assertEquals(professor.getId(), availability.getUserId());
        assertEquals("PROFESSOR", availability.getUserType());
        
        // Default availability should be all false
        Boolean[][] timeslots = availability.getTimeslots();
        assertNotNull(timeslots);
        assertEquals(5, timeslots.length); // 5 days
        assertEquals(32, timeslots[0].length); // 32 time slots (15 min intervals, 8am-4pm)
        
        for (int day = 0; day < 5; day++) {
            for (int slot = 0; slot < 32; slot++) {
                assertFalse(timeslots[day][slot]);
            }
        }
    }

    @Test
    void getAvailability_createsDefaultForNewStudent() {
        Availability availability = availabilityService.getAvailability(student.getId(), "STUDENT");
        
        assertNotNull(availability);
        assertEquals(student.getId(), availability.getUserId());
        assertEquals("STUDENT", availability.getUserType());
    }

    @Test
    void getAvailability_returnsExistingAvailability() {
        // First call creates default
        Availability first = availabilityService.getAvailability(professor.getId(), "PROFESSOR");
        Long firstId = first.getId();
        
        // Second call returns same availability
        Availability second = availabilityService.getAvailability(professor.getId(), "PROFESSOR");
        
        assertEquals(firstId, second.getId());
    }

    @Test
    void updateAvailability_success() {
        Boolean[][] newTimeslots = new Boolean[5][32];
        for (int day = 0; day < 5; day++) {
            for (int slot = 0; slot < 32; slot++) {
                // Set Monday and Wednesday mornings as available
                newTimeslots[day][slot] = (day == 0 || day == 2) && slot < 16;
            }
        }
        
        availabilityService.updateAvailability(professor.getId(), "PROFESSOR", newTimeslots);
        
        Availability updated = availabilityService.getAvailability(professor.getId(), "PROFESSOR");
        
        assertTrue(updated.getTimeslots()[0][0]); // Monday morning
        assertTrue(updated.getTimeslots()[2][0]); // Wednesday morning
        assertFalse(updated.getTimeslots()[1][0]); // Tuesday morning
        assertFalse(updated.getTimeslots()[0][20]); // Monday afternoon
    }

    @Test
    void updateAvailability_fullDayAvailable() {
        Boolean[][] fullDay = new Boolean[5][32];
        for (int day = 0; day < 5; day++) {
            for (int slot = 0; slot < 32; slot++) {
                fullDay[day][slot] = true;
            }
        }
        
        availabilityService.updateAvailability(student.getId(), "STUDENT", fullDay);
        
        Availability updated = availabilityService.getAvailability(student.getId(), "STUDENT");
        
        for (int day = 0; day < 5; day++) {
            for (int slot = 0; slot < 32; slot++) {
                assertTrue(updated.getTimeslots()[day][slot]);
            }
        }
    }

    @Test
    void updateAvailability_partialUpdate() {
        // First set all to true
        Boolean[][] allTrue = new Boolean[5][32];
        for (int day = 0; day < 5; day++) {
            for (int slot = 0; slot < 32; slot++) {
                allTrue[day][slot] = true;
            }
        }
        availabilityService.updateAvailability(professor.getId(), "PROFESSOR", allTrue);
        
        // Then set Friday to unavailable
        Boolean[][] fridayOff = new Boolean[5][32];
        for (int day = 0; day < 5; day++) {
            for (int slot = 0; slot < 32; slot++) {
                fridayOff[day][slot] = day != 4; // Friday is day 4
            }
        }
        availabilityService.updateAvailability(professor.getId(), "PROFESSOR", fridayOff);
        
        Availability updated = availabilityService.getAvailability(professor.getId(), "PROFESSOR");
        
        assertTrue(updated.getTimeslots()[0][0]); // Monday available
        assertFalse(updated.getTimeslots()[4][0]); // Friday unavailable
    }

    @Test
    void getAvailability_differentUserTypes() {
        // Create availability for same user ID but different types
        Long userId = 999L;
        
        Availability professorAvail = availabilityService.getAvailability(userId, "PROFESSOR");
        Availability studentAvail = availabilityService.getAvailability(userId, "STUDENT");
        
        assertNotEquals(professorAvail.getId(), studentAvail.getId());
        assertEquals("PROFESSOR", professorAvail.getUserType());
        assertEquals("STUDENT", studentAvail.getUserType());
    }

    @Test
    void updateAvailability_preservesIdAfterUpdate() {
        Availability initial = availabilityService.getAvailability(professor.getId(), "PROFESSOR");
        Long initialId = initial.getId();
        
        Boolean[][] newTimeslots = new Boolean[5][32];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 32; j++) {
                newTimeslots[i][j] = true;
            }
        }
        
        availabilityService.updateAvailability(professor.getId(), "PROFESSOR", newTimeslots);
        
        Availability updated = availabilityService.getAvailability(professor.getId(), "PROFESSOR");
        assertEquals(initialId, updated.getId());
    }

    @Test
    void getAvailability_matrixDimensions() {
        Availability availability = availabilityService.getAvailability(student.getId(), "STUDENT");
        
        Boolean[][] timeslots = availability.getTimeslots();
        assertEquals(5, timeslots.length); // Mon-Fri
        
        for (int day = 0; day < 5; day++) {
            assertEquals(32, timeslots[day].length); // 32 slots (8am-4pm at 15 min intervals)
        }
    }
}
