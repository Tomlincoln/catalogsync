package hu.tomlincoln.catalogsync.service;

import hu.tomlincoln.catalogsync.domain.Product;
import hu.tomlincoln.catalogsync.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductProviderService {

    private final ProductRepository productRepository;

    public ProductProviderService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Set<Product> getProductsToBeCreated(Set<String> allToBeCreatedIds, HashSet<String[]> validatedProducts) {
        return allToBeCreatedIds.stream()
                .map(id -> validatedProducts.stream()
                        .filter(p -> p[0].equals(id))
                        .map(Product::fromStringArray)
                        .findFirst()
                        .orElseGet(Product::new))
                .collect(Collectors.toSet());
    }

    public Set<Product> getProductsToBeUpdated(Set<String> idsToBeUpdated, Set<String[]> validatedProducts) {
        return productRepository.getAllWhereExists(idsToBeUpdated).stream()
                .filter(p -> p.equals(validatedProducts.stream()
                        .filter(vp -> vp[0].equals(p.getId()))
                        .map(Product::fromStringArray)
                        .filter(vp -> !vp.equals(p))
                        .findFirst()
                        .orElseGet(Product::new)))
                .collect(Collectors.toSet());
    }

}
