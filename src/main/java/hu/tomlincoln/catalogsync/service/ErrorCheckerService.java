package hu.tomlincoln.catalogsync.service;

import hu.tomlincoln.catalogsync.domain.AgeGroup;
import hu.tomlincoln.catalogsync.domain.Product;
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

    private static final Pattern CATEGORY_PATTERN = Pattern.compile("^\\d+$|^[^\\d]+$");

    public boolean hasError(String[] product, List<InvalidProductDTO> invalidProducts) {
        String[] cleanedProduct = Product.cleanProductArrayFromParenthesis(product);

        // We need to handle enums since they may have space in them.
        IntStream.of(3, 4, 10).forEach(i -> cleanedProduct[i] = cleanedProduct[i].replace(" ", "_"));

        // Also handle default values where needed
        cleanedProduct[4] = !cleanedProduct[4].isEmpty() ? cleanedProduct[4] : "NEW";
        cleanedProduct[10] = !cleanedProduct[10].isEmpty() ? cleanedProduct[10] : "";

        if (cleanedProduct.length != 12) {
            addToInvalidProducts(invalidProducts, cleanedProduct, null);
            return true;
        }
        if (cleanedProduct[0].length() > 50) {
            addToInvalidProducts(invalidProducts, cleanedProduct, "id");
            return true;
        }
        if (cleanedProduct[1].length() > 100) {
            addToInvalidProducts(invalidProducts, cleanedProduct, "title");
            return true;
        }
        if (cleanedProduct[2].length() > 5000) {
            addToInvalidProducts(invalidProducts, cleanedProduct, "description");
            return true;
        }
        if (Arrays.stream(ProductAvailability.values()).map(ProductAvailability::name).noneMatch(s -> s.equalsIgnoreCase(cleanedProduct[3]))) {
            addToInvalidProducts(invalidProducts, cleanedProduct, "availability");
            return true;
        }
        if (Arrays.stream(ProductCondition.values()).map(ProductCondition::name).noneMatch(s -> s.equalsIgnoreCase(cleanedProduct[4]))) {
            addToInvalidProducts(invalidProducts, cleanedProduct, "condition");
            return true;
        }
        if (hasPriceError(5, cleanedProduct)) {
            addToInvalidProducts(invalidProducts, cleanedProduct, "price");
            return true;
        }
        if (hasPriceError(6, cleanedProduct)) {
            addToInvalidProducts(invalidProducts, cleanedProduct, "sale_price");
            return true;
        }
        if (hasLinkError(cleanedProduct, 7)) {
            addToInvalidProducts(invalidProducts, cleanedProduct, "link");
            return true;
        }
        if (cleanedProduct[8].length() > 50) {
            addToInvalidProducts(invalidProducts, cleanedProduct, "brand");
            return true;
        }
        if (hasLinkError(product, 9)) {
            addToInvalidProducts(invalidProducts, cleanedProduct, "image_link");
            return true;
        }
        if (Arrays.stream(AgeGroup.values()).map(Enum::name)
                .noneMatch(s -> s.equalsIgnoreCase("".equals(cleanedProduct[10]) ? "NOT_SPECIFIED" : cleanedProduct[10]))) {
            addToInvalidProducts(invalidProducts, cleanedProduct, "age_group");
            return true;
        }
        if (!CATEGORY_PATTERN.matcher(cleanedProduct[11]).matches()) {
            addToInvalidProducts(invalidProducts, cleanedProduct, "google_product_category");
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
            if (!(product[index].startsWith("http") || product[index].startsWith("https"))) {
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
