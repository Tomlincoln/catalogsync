package hu.tomlincoln.catalogsync.service;

import hu.tomlincoln.catalogsync.domain.AgeGroup;
import hu.tomlincoln.catalogsync.domain.ProductAvailability;
import hu.tomlincoln.catalogsync.domain.ProductCondition;
import hu.tomlincoln.catalogsync.dto.InvalidProductDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Service
public class ErrorCheckerService {

    private static final Pattern CATEGORY_PATTERN = Pattern.compile("^([0-9]*|[A-Za-z])$");

    public boolean hasError(String[] product, List<InvalidProductDTO> invalidProducts) {
        IntStream.range(0, product.length).forEach(i -> {
            if (product[i].startsWith("\"")) {
                product[i] = product[i].substring(1, product[i].length() - 1);
            }
            if (product[i].endsWith("\"")) {
                product[i] = product[i].substring(0, product[i].length() - 2);
            }
        });
        // We need to handle enums since they may have space in them.
        IntStream.of(3, 4, 10).forEach(i -> product[i] = product[i].replace(" ", "_"));

        // Also handle default values where needed
        product[4] = !product[4].isEmpty() ? product[4] : "NEW";
        product[10] = !product[10].isEmpty() ? product[10] : null;

        if (product.length != 12) {
            addToInvalidProducts(invalidProducts, product, null);
            return true;
        }
        if (product[0].length() > 50) {
            addToInvalidProducts(invalidProducts, product, "id");
            return true;
        }
        if (product[1].length() > 100) {
            addToInvalidProducts(invalidProducts, product, "title");
            return true;
        }
        if (product[2].length() > 5000) {
            addToInvalidProducts(invalidProducts, product, "description");
            return true;
        }
        if (Arrays.stream(ProductAvailability.values()).map(ProductAvailability::name).noneMatch(s -> s.equalsIgnoreCase(product[3]))) {
            addToInvalidProducts(invalidProducts, product, "availability");
            return true;
        }
        if (Arrays.stream(ProductCondition.values()).map(ProductCondition::name).noneMatch(s -> s.equalsIgnoreCase(product[4]))) {
            addToInvalidProducts(invalidProducts, product, "condition");
            return true;
        }
        if (hasPriceError(5, product)) {
            addToInvalidProducts(invalidProducts, product, "price");
            return true;
        }
        if (hasPriceError(6, product)) {
            addToInvalidProducts(invalidProducts, product, "sale_price");
            return true;
        }
        if (hasLinkError(product, 7)) {
            addToInvalidProducts(invalidProducts, product, "link");
            return true;
        }
        if (product[8].length() > 50) {
            addToInvalidProducts(invalidProducts, product, "brand");
            return true;
        }
        if (hasLinkError(product, 9)) {
            addToInvalidProducts(invalidProducts, product, "image_link");
            return true;
        }
        if (product[10] != null && (Arrays.stream(AgeGroup.values()).map(Enum::name).noneMatch(s -> s.equalsIgnoreCase(product[10])))) {
            addToInvalidProducts(invalidProducts, product, "age group");
            return true;
        }
        if (!CATEGORY_PATTERN.matcher(product[11]).matches()) {
            addToInvalidProducts(invalidProducts, product, "google product category");
            return true;
        }
        return false;
    }

    private boolean hasPriceError(int index, String[] product) {
        try {
            String[] splitPrice = product[index].split(" ");
            if (splitPrice.length != 2) {
                throw new IllegalArgumentException(index == 5 ? "price" : "sale_price" + " split length is not valid");
            }
            // Syntax check
            new BigDecimal(splitPrice[0]);
            // Syntax check
            Currency.getInstance(splitPrice[1]);
        } catch (IllegalArgumentException e) {
            return true;
        }
        return false;
    }

    private boolean hasLinkError(String[] product, int index) {
        try {
            if (!product[index].startsWith("http") || !product[index].startsWith("https")) {
                throw new IllegalArgumentException("not a (http or https) link");
            }
            // Syntax check
            new URL(product[index]);
        } catch (MalformedURLException | IllegalArgumentException e) {
            return true;
        }
        return false;
    }

    private InvalidProductDTO createInvalidProductInfo(String[] splitProduct, String fieldName) {
        String errorMsg = "did not find all parameters (should be 12)";
        if (fieldName != null) {
            errorMsg = fieldName + " field contains unparsable and/or over-sized data";
        }
        return new InvalidProductDTO.Builder()
                .withProductString(splitProduct)
                .withErrorMessage(errorMsg)
                .build();
    }

    private void addToInvalidProducts(List<InvalidProductDTO> invalidProducts, String[] splitProduct, String fieldName) {
        if (invalidProducts.size() < SynchronizerService.MAX_INVALID_REPORTABLE) {
            invalidProducts.add(createInvalidProductInfo(splitProduct, fieldName));
        }
    }

}
