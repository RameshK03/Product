package manogroups.Product.Product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import manogroups.Product.Product.entity.ProductHandler;


@Repository
public interface ProductHandlerRepository extends JpaRepository<ProductHandler , Long>{
    List<ProductHandler> findByStoreName(String storeName);
}
