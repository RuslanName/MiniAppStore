package mainFiles.database.tables.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "products_data")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(name = "quantity", columnDefinition = "INTEGER")
    private Integer quantity;

    @Column(name = "price", columnDefinition = "INTEGER")
    private Integer price;

    @Column(name = "icon_path", columnDefinition = "VARCHAR(255)")
    private String iconPath;

    @Column(name = "visibility", columnDefinition = "BOOLEAN")
    private boolean visibility;

    @Column(name = "registered_at", columnDefinition = "TIMESTAMP")
    private Timestamp registeredAt;
}






