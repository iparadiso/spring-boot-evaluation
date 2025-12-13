package com.example.demo.service;

import com.example.demo.model.Product;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {

    private final Map<Long, Product> dataStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public ProductService() {
        dataStore.put(1L, new Product(1L, "Sample Product", "A sample product", 99.99));
        dataStore.put(2L, new Product(2L, "Another Product", "Another sample", 149.99));
        idGenerator.set(3);
    }

    @Cacheable(value = "products", key = "#id")
    public Optional<Product> getProductById(Long id) {
        System.out.println("Fetching product from data store for id: " + id);
        return Optional.ofNullable(dataStore.get(id));
    }

    @CachePut(value = "products", key = "#product.id")
    public Product saveOrUpdateProduct(Product product) {
        if (product.getId() == null) {
            product.setId(idGenerator.getAndIncrement());
        }
        dataStore.put(product.getId(), product);
        System.out.println("Saved/Updated product: " + product);
        return product;
    }

    @CacheEvict(value = "products", key = "#id")
    public boolean deleteProduct(Long id) {
        System.out.println("Deleting product with id: " + id);
        return dataStore.remove(id) != null;
    }
}