package vv.pms.presentation;

import jakarta.persistence.*;
import vv.pms.availability.Availability;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // 5 x 16 matrix (Mon–Fri, 30-min bins 08:00–16:00)
    @Convert(converter = Availability.MatrixConverter.class)
    @Column(columnDefinition = "TEXT")
    private Boolean[][] availability;

    public Room() {
    }

    public Room(String name) {
        this.name = name;
        this.availability = defaultFullAvailability();
    }

    public Room(String name, Boolean[][] availability) {
        this.name = name;
        this.availability = availability;
    }

    private Boolean[][] defaultFullAvailability() {
        Boolean[][] matrix = new Boolean[5][16];
        for (int d = 0; d < 5; d++) {
            for (int t = 0; t < 16; t++) {
                matrix[d][t] = Boolean.TRUE; // by default, rooms are available everywhere
            }
        }
        return matrix;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Boolean[][] getAvailability() { return availability; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAvailability(Boolean[][] availability) { this.availability = availability; }
}