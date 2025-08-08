package manogroups.Product.ProductLog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import manogroups.Product.ProductLog.entity.ProductLog;

@Repository
public interface ProductLogRepository extends JpaRepository<ProductLog, Long>{

}
