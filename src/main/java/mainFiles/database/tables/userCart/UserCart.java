package mainFiles.database.tables.userCart;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_carts_data")
public class UserCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @Column(name = "chat_id", columnDefinition = "BIGINT")
    private Long chatId;

    @Column(name = "product_id", columnDefinition = "INTEGER")
    private Integer productId;

    @Column(name = "quantity", columnDefinition = "INTEGER")
    private Integer quantity;

    @Column(name = "selected", columnDefinition = "BOOLEAN")
    private boolean selected;
}
