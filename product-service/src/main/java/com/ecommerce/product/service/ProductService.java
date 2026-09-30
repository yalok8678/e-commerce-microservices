package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(
            Long vendorId,
            ProductRequest request) {

        Product product = Product.builder()
                .vendorId(vendorId)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .status("ACTIVE")
                .build();

        Product savedProduct =
                productRepository.save(product);

        return toResponse(savedProduct);
    }

    @Cacheable(value = "products", key = "#p0")

    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        return toResponse(product);
    }

    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ProductResponse> getProductsByVendor(
            Long vendorId) {

        return productRepository
                .findByVendorId(vendorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ProductResponse> getProductsByCategory(
            String category) {

        return productRepository
                .findByCategory(category)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @CachePut(value = "products", key = "#p0")
    public ProductResponse updateProduct(
            Long productId,
            Long vendorId,
            ProductRequest request) {

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        if (!product.getVendorId().equals(vendorId)) {

            throw new RuntimeException(
                    "You are not allowed to modify this product"
            );
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());

        Product updatedProduct =
                productRepository.save(product);

        return toResponse(updatedProduct);
    }
    @CacheEvict(value = "products", key = "#p0")
    public void deleteProduct(
            Long productId,
            Long vendorId) {

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        if (!product.getVendorId().equals(vendorId)) {

            throw new RuntimeException(
                    "You are not allowed to delete this product"
            );
        }

        productRepository.delete(product);
    }

    public ProductResponse changeStatus(
            Long productId,
            String status) {

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        product.setStatus(status);

        Product updatedProduct =
                productRepository.save(product);

        return toResponse(updatedProduct);
    }

    private ProductResponse toResponse(
            Product product) {

        return new ProductResponse(
                product.getId(),
                product.getVendorId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getStatus()
        );
    }
}