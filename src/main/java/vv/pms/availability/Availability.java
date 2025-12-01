package vv.pms.availability;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import java.io.IOException;

@Entity
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String userType; // "STUDENT" or "PROFESSOR"

    // 5 Days (Mon-Fri) x 32 Time slots (15 mins, 8am-4pm)
    // Stored as JSON string in DB, used as Boolean[][] in Java
    @Convert(converter = MatrixConverter.class)
    @Column(columnDefinition = "TEXT")
    private Boolean[][] timeslots;

    public Availability() {}

    public Availability(Long userId, String userType, Boolean[][] timeslots) {
        this.userId = userId;
        this.userType = userType;
        this.timeslots = timeslots;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public Boolean[][] getTimeslots() { return timeslots; }
    public void setTimeslots(Boolean[][] timeslots) { this.timeslots = timeslots; }

    // --- Converter for Database JSON storage ---
    @Converter
    public static class MatrixConverter implements AttributeConverter<Boolean[][], String> {
        private static final ObjectMapper mapper = new ObjectMapper();

        @Override
        public String convertToDatabaseColumn(Boolean[][] attribute) {
            try {
                return mapper.writeValueAsString(attribute);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Error converting matrix to JSON", e);
            }
        }

        @Override
        public Boolean[][] convertToEntityAttribute(String dbData) {
            try {
                if (dbData == null) return null;
                return mapper.readValue(dbData, Boolean[][].class);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error reading matrix JSON", e);
            }
        }
    }
}