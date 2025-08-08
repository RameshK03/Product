package manogroups.Product.ProductLog.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import manogroups.Product.ProductLog.entity.ProductLog;
import manogroups.Product.ProductLog.service.ProductLogService;

@RestController
@RequestMapping("/api/log")
public class ProductLogController {

    @Autowired
    private ProductLogService productLogService;

    @GetMapping("/product")
    public ResponseEntity<?> getLog(){
        List<ProductLog> productLogs = productLogService.getLog();
        if(productLogs.isEmpty()){
            return ResponseEntity.badRequest().body("No Product Log in the Database");
        }
        return ResponseEntity.ok(productLogs);
    }
}
