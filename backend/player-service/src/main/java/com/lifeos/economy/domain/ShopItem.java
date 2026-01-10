package com.lifeos.economy.domain;

import com.lifeos.economy.domain.enums.ShopCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "shop_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID itemId;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private long cost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShopCategory category;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> effectPayload;
}
