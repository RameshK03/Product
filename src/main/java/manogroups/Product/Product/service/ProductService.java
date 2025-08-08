package manogroups.Product.Product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import manogroups.Product.GoogleDriveService.GoogleDriveService;
import manogroups.Product.Jwt.JwtUtil;
import manogroups.Product.Product.entity.Product;
import manogroups.Product.Product.entity.ProductHandler;
import manogroups.Product.Product.repository.ProductHandlerRepository;
import manogroups.Product.Product.repository.ProductRepository;
import manogroups.Product.ProductLog.entity.ProductLog;
import manogroups.Product.ProductLog.repository.ProductLogRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductHandlerRepository productHandlerRepository;

    @Autowired
    private ProductLogRepository productLogRepository;

    @Autowired
    private GoogleDriveService googleDriveService;

    @Autowired
    private JwtUtil jwtUtil;

    public String addRequest(String authHeader, Product product, MultipartFile[] images) throws Exception {
        if (productRepository.existsByProductCodeAndStoreName(product.getProductCode(), product.getStoreName())) {
            return "Product Code Already Exists";
        }

        product.setProductImageId(create(images));
        product.setProductUploadBy(jwtUtil.extractName(authHeader));

        ProductLog productLog = new ProductLog();
        productLog.setProductCode(product.getProductCode());
        productLog.setStoreName(product.getStoreName());
        productLog.setStaffName(jwtUtil.extractName(authHeader));
        productLog.setDate(LocalDateTime.now());
        productLog.setStoreName(product.getStoreName());

        product.setNoOfOrders(0);

        String role = getRole();

        if (role.equals("ROLE_ADMIN")) {
            product.setProductStatus("APPROVED");
            productLog.setMessage("Product Upload By Admin");
            productRepository.save(product);
            productLog.setProductId(product.getProductId());
            productLogRepository.save(productLog);
            return "Product Added Successfully";
        }else if(role.equals("ROLE_STAFF")){

        product.setProductStatus("CREATE");
        productRepository.save(product);
        productLog.setProductId(product.getProductId());
        productLog.setMessage("Product Upload Request Sent to Admin");
        productLogRepository.save(productLog);
        }
        return "Product Added Request Sent to Admin.";
    }

    public Product getProducts(Long productId) {
        return productRepository.findById(productId).orElse(null);
    }

    public List<Product> getCategory(String category, String storeName) {
         return productRepository.findByProductStatusAndCategoryAndStoreName("APPROVED", category, storeName);
        
    }

    public List<Product> topSelling(String storeName) {
       return productRepository.findByStoreNameOrderByNoOfOrdersDesc(storeName);
        
    }

    public List<Product> getStatus(String productStatus,String storeName) {
        return get(productStatus, storeName);
    }

    public List<String> approveCreate(List<Long> productIds) {
        List<String> results = new ArrayList<>();

        for (Long productId : productIds) {
            Optional<Product> optionalProduct = productRepository.findById(productId);

            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                product.setProductStatus("APPROVED");

                ProductLog productLog = new ProductLog();
                productLog.setProductCode(product.getProductCode());
                productLog.setProductId(productId);
                productLog.setStoreName(product.getStoreName());
                productLog.setStaffName("Admin");
                productLog.setDate(LocalDateTime.now());
                productLog.setMessage("Product Upload Approved by Admin");
                productLog.setStoreName(product.getStoreName());
                productLogRepository.save(productLog);

                productRepository.save(product);

                results.add("Product ID " + productId + ": Approved by Admin");
            } else {
                results.add("Product ID " + productId + ": Not Found");
            }
        }

        return results;
    }

    public String updateRequest(String authHeader, ProductHandler productHandler, MultipartFile[] images) throws Exception {
        if (!productRepository.existsById(productHandler.getProductOriginalId())) {
            return "Product Not Found";
        }

        productHandler.setProductImageId(create(images));
        String role = getRole();

        ProductLog productLog = new ProductLog();
        productLog.setProductCode(productHandler.getProductCode());
        productLog.setProductId(productHandler.getProductOriginalId());
        productLog.setStoreName(productHandler.getStoreName());
        productLog.setStaffName(jwtUtil.extractName(authHeader));
        productLog.setDate(LocalDateTime.now());
        productLog.setStoreName(productHandler.getStoreName());

        if (role.equals("ROLE_ADMIN")) {
            productLog.setMessage("Product Updated By Admin");
            productLogRepository.save(productLog);
            update(productHandler);
            return "Product Updated Successfully";
        } else if (role.equals("ROLE_STAFF")){
            productLog.setMessage("Product Update Request Sent to Admin");
            productLogRepository.save(productLog);

            Product product = productRepository.findById(productHandler.getProductOriginalId()).orElse(null);
            product.setProductStatus("UPDATE");
            productRepository.save(product);
        }

        productHandlerRepository.save(productHandler);
        return "Product Updated Request Sent to Admin.";
    }

    public List<ProductHandler> getUpdate(String storeName) {
        return productHandlerRepository.findByStoreName(storeName);
        
    }

    public List<String> approveUpdate(List<Long> productHandlerIds) {
        List<String> results = new ArrayList<>();

        for (Long productHandlerId : productHandlerIds) {
            Optional<ProductHandler> optionalHandler = productHandlerRepository.findById(productHandlerId);

            if (optionalHandler.isPresent()) {
                ProductHandler handler = optionalHandler.get();

                ProductLog productLog = new ProductLog();
                productLog.setProductCode(handler.getProductCode());
                productLog.setProductId(handler.getProductOriginalId());
                productLog.setStoreName(handler.getStoreName());
                productLog.setStaffName("Admin");
                productLog.setDate(LocalDateTime.now());
                productLog.setStoreName(handler.getStoreName());
                productLog.setMessage("Product Updated Approved by Admin");
                productLogRepository.save(productLog);

                update(handler);
                productHandlerRepository.deleteById(productHandlerId);

                results.add("Handler ID " + productHandlerId + ": Product Updated Approved by Admin");
            } else {
                results.add("Handler ID " + productHandlerId + ": Not Found");
            }
        }

        return results;
    }

    public String deleteRequest(String authHeader, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product Not Found"));

        String role = getRole();

        ProductLog productLog = new ProductLog();
        productLog.setProductCode(product.getProductCode());
        productLog.setProductId(productId);
        productLog.setStaffName(jwtUtil.extractName(authHeader));
        productLog.setStoreName(product.getStoreName());
        productLog.setDate(LocalDateTime.now());

        if (role.equals("ROLE_ADMIN")) {
            productLog.setMessage("Product Deleted By Admin");
            productLogRepository.save(productLog);
            approveDelete(Collections.singletonList(productId));
            return "Product Deleted By Admin.";
        }else if(role.equals("ROLE_STAFF"))

        productLog.setMessage("Product Delete Request sent to Admin");
        productLogRepository.save(productLog);
        product.setProductStatus("DELETE");
        productRepository.save(product);

        return "Product Delete Request Sent to Admin.";
    }

    public List<String> approveDelete(List<Long> productIds) {
        List<String> results = new ArrayList<>();

        for (Long productId : productIds) {
            Optional<Product> optionalProduct = productRepository.findById(productId);

            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                delete(product.getProductImageId());

                ProductLog productLog = new ProductLog();
                productLog.setProductCode(product.getProductCode());
                productLog.setProductId(productId);
                productLog.setStoreName(product.getStoreName());
                productLog.setStaffName("Admin");
                productLog.setDate(LocalDateTime.now());
                productLog.setMessage("Product Deleted by Admin");
                productLogRepository.save(productLog);

                productRepository.deleteById(productId);
                results.add("Product ID " + productId + ": Deleted by Admin");
            } else {
                results.add("Product ID " + productId + ": Not Found");
            }
        }

        return results;
    }

    public List<String> create(MultipartFile[] images) throws Exception {
        List<String> imageIds = new ArrayList<>();
        for (MultipartFile image : images) {
            String fileId = googleDriveService.uploadFile(image);
            imageIds.add(fileId);
        }
        return imageIds;
    }

    public List<Product> get(String productStatus, String storeName) {
        return productRepository.findByProductStatusAndStoreName(productStatus, storeName);
        
    }

    public void update(ProductHandler productHandler) {
        Product product = productRepository.findById(productHandler.getProductOriginalId()).orElse(null);

        if(product!=null){
            product.setProductName(productHandler.getProductName());
            product.setProductDescription(productHandler.getProductDescription());
            product.setStoreName(productHandler.getStoreName());
            product.setCategory(productHandler.getCategory());
            product.setRecommend(productHandler.getRecommend());
            product.setTrending(productHandler.isTrending());
            product.setBrandName(productHandler.getBrandName());
            product.setProductQuantity(productHandler.getProductQuantity());
            product.setProductOriginalPrice(productHandler.getProductOriginalPrice());
            product.setProductSellingPrice(productHandler.getProductSellingPrice());
            product.setProductStatus("APPROVED");
            delete(product.getProductImageId());
            product.setProductImageId(productHandler.getProductImageId());

        productRepository.save(product);
        }
    }

    public void delete(List<String> imageIds) {
        if (imageIds != null) {
            for (String imageId : imageIds) {
                googleDriveService.deleteFileIfExists(imageId);
            }
        }
    }

    public List<String> rejectCreate(List<Long> productIds) {
        List<String> results = new ArrayList<>();

        for (Long productId : productIds) {
            Optional<Product> optionalProduct = productRepository.findById(productId);

            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();

                ProductLog productLog = new ProductLog();
                productLog.setProductCode(product.getProductCode());
                productLog.setProductId(productId);
                productLog.setStoreName(product.getStoreName());
                productLog.setStaffName("Admin");
                productLog.setDate(LocalDateTime.now());
                productLog.setMessage("Product upload request was rejected by Admin.");
                productLogRepository.save(productLog);

                productRepository.deleteById(productId);
                results.add("Product ID " + productId + ": Upload request rejected and deleted");
            } else {
                results.add("Product ID " + productId + ": Not found");
            }
        }

        return results;
    }

    public List<String> rejectUpdate(List<Long> productHandlerIds) {
        List<String> results = new ArrayList<>();

        for (Long handlerId : productHandlerIds) {
            Optional<ProductHandler> optionalHandler = productHandlerRepository.findById(handlerId);

            if (optionalHandler.isPresent()) {
                ProductHandler handler = optionalHandler.get();

                ProductLog productLog = new ProductLog();
                productLog.setProductCode(handler.getProductCode());
                productLog.setProductId(handler.getProductOriginalId());
                productLog.setStoreName(handler.getStoreName());
                productLog.setStaffName("Admin");
                productLog.setDate(LocalDateTime.now());
                productLog.setMessage("Product Update Request was Rejected by Admin");
                productLogRepository.save(productLog);

                delete(handler.getProductImageId());
                productHandlerRepository.deleteById(handlerId);

                Product product = productRepository.findById(handler.getProductOriginalId()).orElse(null);
                if (product != null) {
                    product.setProductStatus("APPROVED");
                    productRepository.save(product);
                }

                results.add("Handler ID " + handlerId + ": Update Request Rejected and Cleaned Up");
            } else {
                results.add("Handler ID " + handlerId + ": Not Found");
            }
        }

        return results;
    }

    public List<String> rejectDelete(List<Long> productIds) {
        List<String> results = new ArrayList<>();

        for (Long productId : productIds) {
            Optional<Product> optionalProduct = productRepository.findById(productId);

            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();

                ProductLog productLog = new ProductLog();
                productLog.setProductCode(product.getProductCode());
                productLog.setProductId(productId);
                productLog.setStoreName(product.getStoreName());
                productLog.setStaffName("Admin");
                productLog.setDate(LocalDateTime.now());
                productLog.setMessage("Product Delete Request was Rejected by Admin");
                productLogRepository.save(productLog);

                product.setProductStatus("APPROVED");
                productRepository.save(product);

                results.add("Product ID " + productId + ": Delete Request Rejected and Status Restored");
            } else {
                results.add("Product ID " + productId + ": Not Found");
            }
        }

        return results;
    }

    private void reject(Long productId, String message) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            ProductLog productLog = new ProductLog();
            productLog.setProductCode(product.getProductCode());
            productLog.setProductId(productId);
            productLog.setStoreName(product.getStoreName());
            productLog.setStaffName("Admin");
            productLog.setDate(LocalDateTime.now());
            productLog.setMessage(message);
            productLogRepository.save(productLog);
        }
    }

    private String getRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
    }

    public void productQuantity(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            product.setNoOfOrders(product.getNoOfOrders()+1);
            if (product.getProductQuantity() > 0) {
                product.setProductQuantity(product.getProductQuantity() - 1);
                productRepository.save(product);
            }
        }
    }
}
