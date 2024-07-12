package hu.tomlincoln.catalogsync.service;

import hu.tomlincoln.catalogsync.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IdProviderService {

    private final ProductRepository productRepository;

    public IdProviderService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Set<String> getIdsWhereExists(Set<String[]> validatedProducts) {
        return productRepository.getIdsWhereExists(validatedProducts.stream().map(s -> s[0]).collect(Collectors.toSet()));
    }

    public Set<String> getIdsWhereNotExists(HashSet<String[]> validatedProducts) {
        return productRepository.getIdsWhereNotExists(validatedProducts.stream().map(vp -> vp[0]).collect(Collectors.toSet()));
    }

}
