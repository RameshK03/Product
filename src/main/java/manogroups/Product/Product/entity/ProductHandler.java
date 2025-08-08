package manogroups.Product.Product.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "producthandlers")
@Entity
public class ProductHandler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productOriginalId;
    private String productCode;
    private String productName;
    private String productDescription;
    private String storeName;
    private String category;
    private String recommend;
    private boolean trending;
    private String brandName;
    private int productQuantity;
    private Double productOriginalPrice;
    private Double productSellingPrice;
    private List<String> productImageId;
}
