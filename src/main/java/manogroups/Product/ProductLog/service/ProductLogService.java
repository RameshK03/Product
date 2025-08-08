package manogroups.Product.ProductLog.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import manogroups.Product.ProductLog.entity.ProductLog;
import manogroups.Product.ProductLog.repository.ProductLogRepository;

@Service
public class ProductLogService {

    @Autowired
    ProductLogRepository productLogRepository;

    public List<ProductLog> getLog() {
       return productLogRepository.findAll();
    }
}
