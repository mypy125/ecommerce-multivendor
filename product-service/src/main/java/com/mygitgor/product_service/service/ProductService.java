package com.mygitgor.product_service.service;

import com.mygitgor.product_service.dto.CreateProductRequest;
import com.mygitgor.product_service.dto.ProductDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    Boolean existProductById(UUID productId);
    ProductDto getProductById(UUID productId);
    ProductDto createProduct(CreateProductRequest req, UUID sellerId);
    Boolean deleteProduct(UUID productId);
    ProductDto updateProduct(UUID productId, ProductDto product);
    ProductDto updateProductQuantity(UUID productId, Integer quantity);
    ProductDto findProductById(UUID productId);
    List<ProductDto> searchProduct(String query);
    Page<ProductDto> getAllProducts(String category,
                                    String brand,
                                    String colors,
                                    String sizes,
                                    Integer minPrice,
                                    Integer maxPrice,
                                    Integer minDiscount,
                                    String sort,
                                    String stock,
                                    Integer pageNumber
    );
    List<ProductDto> getProductBySellerId(UUID sellerId);
}
