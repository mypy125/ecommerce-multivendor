package com.mygitgor.product_service.service;

import com.mygitgor.product_service.domain.Product;
import com.mygitgor.product_service.dto.CreateProductRequest;
import com.mygitgor.product_service.dto.ProductDto;
import com.mygitgor.product_service.mapper.ProductMapper;
import com.mygitgor.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public Boolean existProductById(UUID productId) {
        return productRepository.existsById(productId);
    }

    @Override
    public ProductDto getProductById(UUID productId) {
        if(productRepository.findById(productId).isEmpty()){
            throw new RuntimeException(String.format(
                    "product with id '%s' not found",productId
            ));
        }
        Product product = productRepository.findById(productId).get();
        return productMapper.toDto(product);
    }

    @Override
    public ProductDto createProduct(CreateProductRequest req, UUID sellerId) {
        return null;
    }

    @Override
    public Boolean deleteProduct(UUID productId) {
        return false;
    }

    @Override
    public ProductDto updateProduct(UUID productId, ProductDto product) {
        return null;
    }

    @Override
    public ProductDto updateProductQuantity(UUID productId, Integer quantity) {
        return null;
    }

    @Override
    public ProductDto findProductById(UUID productId) {
        return null;
    }

    @Override
    public List<ProductDto> searchProduct(String query) {
        return List.of();
    }

    @Override
    public Page<ProductDto> getAllProducts(String category, String brand, String colors, String sizes, Integer minPrice, Integer maxPrice, Integer minDiscount, String sort, String stock, Integer pageNumber) {
        return null;
    }

    @Override
    public List<ProductDto> getProductBySellerId(UUID sellerId) {
        return List.of();
    }


}
