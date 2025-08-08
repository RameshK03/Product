package manogroups.Product.Product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import manogroups.Product.Product.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{
    boolean existsByProductCodeAndStoreName(String productCode, String storeName);
    List<Product> findByProductCodeAndStoreName(String productCode, String storeName);
    List<Product> findByProductStatusAndCategoryAndStoreName(String string, String category, String storeName);
    List<Product> findByProductStatusAndStoreName(String productStatus, String storeName);
    List<Product> findByStoreNameOrderByNoOfOrdersDesc(String storeName);
}
