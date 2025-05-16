package mainFiles.database.tables.pickupPoint;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "pickup_points_data")
public class PickupPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(name = "delivery_time", columnDefinition = "INTEGER")
    private Integer deliveryTime;

    @Column(name = "registered_at", columnDefinition = "TIMESTAMP")
    private Timestamp registeredAt;
}