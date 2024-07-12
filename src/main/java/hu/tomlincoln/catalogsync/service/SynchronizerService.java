package hu.tomlincoln.catalogsync.service;

import hu.tomlincoln.catalogsync.domain.Product;
import hu.tomlincoln.catalogsync.dto.InvalidProductDTO;
import hu.tomlincoln.catalogsync.dto.MaxSizedLinkedList;
import hu.tomlincoln.catalogsync.dto.ReportDTO;
import hu.tomlincoln.catalogsync.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class SynchronizerService {

    public static final int MAX_INVALID_REPORTABLE = 12;

    private final ProductRepository productRepository;
    private final ErrorCheckerService errorCheckerService;
    private final IdProviderService idProviderService;
    private final ProductProviderService productProviderService;

    public SynchronizerService(ProductRepository productRepository, ErrorCheckerService errorCheckerService,
                               IdProviderService idProviderService, ProductProviderService productProviderService) {
        this.productRepository = productRepository;
        this.errorCheckerService = errorCheckerService;
        this.idProviderService = idProviderService;
        this.productProviderService = productProviderService;
    }

    public ReportDTO synchronize(String filename) {
        long skipped = 0;
        final List<InvalidProductDTO> invalidProducts = new MaxSizedLinkedList<>(MAX_INVALID_REPORTABLE);
        List<String> lines;
        Path filePath = Paths.get(filename);
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            return ReportDTO.getEmptyReport();
        }

        // Skip 1 is the header
        List<String[]> splitLines = lines.stream().skip(1).map(s -> s.split("\\t")).collect(Collectors.toList());
        HashSet<String[]> validatedProducts = new HashSet<>();
        for (String[] productArray : splitLines) {
            String[] cleanedProductArray = Product.cleanProductArrayFromParenthesis(productArray);
            if (errorCheckerService.hasError(cleanedProductArray, invalidProducts)) {
                skipped++;
            } else {
                validatedProducts.add(cleanedProductArray);
            }
        }

        Set<String> idsToBeUpdated = idProviderService.getIdsWhereExists(validatedProducts);
        Set<Product> productsToBeUpdated = productProviderService.getProductsToBeUpdated(idsToBeUpdated, validatedProducts);
        productRepository.saveAll(productsToBeUpdated);

        Set<String> productIdsToBeDeleted = idProviderService.getIdsWhereNotExists(validatedProducts);
        productRepository.deleteAllById(productIdsToBeDeleted);

        Set<String> allToBeCreatedIds = validatedProducts.stream().map(s -> s[0]).collect(Collectors.toSet());
        allToBeCreatedIds.removeIf(idsToBeUpdated::contains);
        Set<Product> productsToBeCreated = productProviderService.getProductsToBeCreated(allToBeCreatedIds, validatedProducts);
        productRepository.saveAll(productsToBeCreated);

        return new ReportDTO.Builder()
                .withAdded(productsToBeCreated.size())
                .withUpdated(productsToBeUpdated.size())
                .withNotChanged(productRepository.count() - productsToBeUpdated.size() - productsToBeCreated.size())
                .withDeleted(productIdsToBeDeleted.size())
                .withSkipped(skipped)
                .withInvalidProducts(invalidProducts)
                .build();
    }
}
