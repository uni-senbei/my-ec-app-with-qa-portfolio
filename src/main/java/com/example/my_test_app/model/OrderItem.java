package com.example.my_test_app.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference("order-items")  // Order クラスの @JsonManagedReference に対応
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "item_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal itemPrice;

    @Column(nullable = false)
    private int quantity;

    // コンストラクタは維持
    public OrderItem(Long productId, String productName, BigDecimal itemPrice, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.itemPrice = itemPrice;
        this.quantity = quantity;
    }

    // equals と hashCode メソッドを id フィールドに基づいて実装
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem orderItem)) return false;
        return id != null && id.equals(orderItem.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // toString メソッドは循環参照を避けるため、重要なフィールドのみを含める
    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", itemPrice=" + itemPrice +
                ", quantity=" + quantity +
                '}';
    }
}