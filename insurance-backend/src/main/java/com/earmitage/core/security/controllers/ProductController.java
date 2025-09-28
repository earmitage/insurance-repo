package com.earmitage.core.security.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.earmitage.core.security.repository.Product;
import com.earmitage.core.security.repository.ProductRepository;

@RestController
@RequestMapping
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("${app.url}/products/{uuid}/")
    public ResponseEntity<Product> getByUuid(@PathVariable String uuid) {
        return productRepository.findByUuid(uuid).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("${app.url}/products/")
    public List<Product> fetchAll() {
        return productRepository.findAll();
    }

}
