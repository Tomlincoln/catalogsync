package hu.tomlincoln.catalogsync.service;

import hu.tomlincoln.catalogsync.domain.Product;
import hu.tomlincoln.catalogsync.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class DumpService {

    private final ProductRepository productRepository;

    public DumpService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> dumpAll() {
        return StreamSupport.stream(productRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

}
