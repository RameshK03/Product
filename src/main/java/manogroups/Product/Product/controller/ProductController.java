package manogroups.Product.Product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import manogroups.Product.Product.entity.Product;
import manogroups.Product.Product.entity.ProductHandler;
import manogroups.Product.Product.service.ProductService;

@RestController
@RequestMapping("api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> addRequest(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("product") String product,
            @RequestPart("image") MultipartFile[] images) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Product productHandler = mapper.readValue(product, Product.class);
            String message = productService.addRequest(authHeader.substring(7), productHandler, images);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProducts(@PathVariable Long productId) {
        Product product = productService.getProducts(productId);
        if (product == null) {
            return ResponseEntity.badRequest().body("Product Not Found");
        }
        return ResponseEntity.ok(product);
    }

    @GetMapping("/approved/{storeName}")
    public ResponseEntity<?> getApproved(@PathVariable String storeName) {
        List<Product> products = productService.getStatus("APPROVED",storeName);
        if (products.isEmpty()) {
            return ResponseEntity.badRequest().body("No Products Available in the Database..");
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category")
    public ResponseEntity<?> getCategory(
            @RequestParam String category,
            @RequestParam String storeName) {
        List<Product> products = productService.getCategory(category, storeName);
        if (products.isEmpty()) {
            return ResponseEntity.badRequest().body("No Products Available in the Database..");
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/top/{storeName}")
    public ResponseEntity<?> topSelling(@PathVariable String storeName) {
        List<Product> products = productService.topSelling(storeName);
        if (products.isEmpty()) {
            return ResponseEntity.badRequest().body("No Products Available for Admin Approval");
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/created")
    public ResponseEntity<?> getCreate(@RequestParam String productStatus,@RequestParam String storeName) {
        List<Product> products = productService.getStatus(productStatus,storeName);
        if (products.isEmpty()) {
            return ResponseEntity.badRequest().body("No Products Available for Admin Approval");
        }
        return ResponseEntity.ok(products);
    }

    @PutMapping("/approveCreate")
    public ResponseEntity<?> approveCreate(@RequestBody List<Long> productIds) {
        try {
            List<String> message = productService.approveCreate(productIds);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateRequest(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("product") String product,
            @RequestPart("image") MultipartFile[] images) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ProductHandler productHandle = mapper.readValue(product, ProductHandler.class);
            String message = productService.updateRequest(authHeader.substring(7), productHandle, images);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/updated/{storeName}")
    public ResponseEntity<?> getUpdate(@PathVariable String storeName) {
        List<ProductHandler> products = productService.getUpdate(storeName);
        if (products.isEmpty()) {
            return ResponseEntity.badRequest().body("No Product Available for Admin Approval");
        }
        return ResponseEntity.ok(products);
    }

    @PutMapping("/approveUpdate")
    public ResponseEntity<?> approveUpdate(@RequestBody List<Long> productHandlerIds) {
        try {
            List<String> message = productService.approveUpdate(productHandlerIds);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/delete/{productId}")
    public ResponseEntity<String> deleteRequest(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long productId) {
        try {
            String message = productService.deleteRequest(authHeader.substring(7), productId);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/deleted")
    public ResponseEntity<?> getDelete(@RequestParam String productStatus,@RequestParam String storeName) {
        List<Product> products = productService.getStatus(productStatus,storeName);
        if (products.isEmpty()) {
            return ResponseEntity.badRequest().body("No Product Available for Admin Approval");
        }
        return ResponseEntity.ok(products);
    }

    @DeleteMapping("/approveDelete")
    public ResponseEntity<?> approveDelete(@RequestBody List<Long> productIds) {
        try {
            List<String> message = productService.approveDelete(productIds);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/rejectCreate")
    public ResponseEntity<?> rejectCreate(@RequestBody List<Long> productIds) {
        try {
            List<String> message = productService.rejectCreate(productIds);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/rejectUpdate")
    public ResponseEntity<?> rejectUpdate(@RequestBody List<Long> productHandlerIds) {
        try {
            List<String> message = productService.rejectUpdate(productHandlerIds);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/rejectDelete")
    public ResponseEntity<?> rejectDelete(@RequestBody List<Long> productIds) {
        try {
            List<String> message = productService.rejectDelete(productIds);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/productQuantity/{productId}")
    public void productQuantity(@PathVariable Long productId) {
        System.out.print("Active");
        productService.productQuantity(productId);
    }
}
