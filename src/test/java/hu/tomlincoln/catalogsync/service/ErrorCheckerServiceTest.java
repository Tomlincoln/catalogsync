package hu.tomlincoln.catalogsync.service;

import hu.tomlincoln.catalogsync.dto.InvalidProductDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class ErrorCheckerServiceTest {

    private final Random random = new Random();

    @Autowired
    private ErrorCheckerService underTest;

    @Test
    void checkHappyPath() {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);

        // THEN
        Assertions.assertFalse(hasError);
        Assertions.assertEquals(0, invalidProducts.size());
    }

    @Test
    void checkIfArrayIsWrongSize() {
        // GIVEN
        String[] testProduct = new String[11];
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);

        // THEN
        Assertions.assertTrue(hasError);
        Assertions.assertEquals(1, invalidProducts.size());
    }

    @Test
    void checkRemoveParenthesis() {
        // GIVEN
        String[] testProduct = new String[11];
        testProduct[0] = "\"aaa\"";
        testProduct[testProduct.length - 1] = "\"bbb\"";
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        underTest.hasError(testProduct, invalidProducts);
        String[] actualProduct = invalidProducts.get(0).getProductString().split("\t", -1);

        // THEN
        Assertions.assertFalse(actualProduct[0].contains("\""));
        Assertions.assertFalse(actualProduct[actualProduct.length - 1].contains("\""));
    }

    @Test
    void checkRemoveSpacesFrom_3_4_10() {
        // GIVEN
        String[] testProduct = new String[11];
        testProduct[3] = "aa a";
        testProduct[4] = "bb b";
        testProduct[10] = "cc c";
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        underTest.hasError(testProduct, invalidProducts);
        String[] actualProduct = invalidProducts.get(0).getProductString().split("\t", -1);

        // THEN
        Assertions.assertEquals("aa_a", actualProduct[3]);
        Assertions.assertEquals("bb_b", actualProduct[4]);
        Assertions.assertEquals("cc_c", actualProduct[10]);
    }

    @Test
    void checkDefaulting_4_WhenNotEmpty() {
        // GIVEN
        String[] testProduct = new String[11];
        testProduct[4] = "aaa";
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        underTest.hasError(testProduct, invalidProducts);
        String[] actualProduct = invalidProducts.get(0).getProductString().split("\t", -1);

        // THEN
        Assertions.assertEquals("aaa", actualProduct[4]);
    }

    @Test
    void checkDefaulting_4_WhenEmpty() {
        // GIVEN
        String[] testProduct = new String[11];
        testProduct[4] = "";
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        underTest.hasError(testProduct, invalidProducts);
        String[] actualProduct = invalidProducts.get(0).getProductString().split("\t", -1);

        // THEN
        Assertions.assertEquals("NEW", actualProduct[4]);
    }

    @Test
    void checkDefaulting_10_WhenNotEmpty() {
        // GIVEN
        String[] testProduct = new String[11];
        testProduct[10] = "aaa";
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        underTest.hasError(testProduct, invalidProducts);
        String[] actualProduct = invalidProducts.get(0).getProductString().split("\t", -1);

        // THEN
        Assertions.assertEquals("aaa", actualProduct[10]);
    }

    @Test
    void checkDefaulting_10_WhenEmpty() {
        // GIVEN
        String[] testProduct = new String[11];
        testProduct[10] = "";
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        underTest.hasError(testProduct, invalidProducts);
        String[] actualProduct = invalidProducts.get(0).getProductString().split("\t", -1);

        // THEN
        Assertions.assertEquals("", actualProduct[10]);
    }

    @Test
    void checkWhenIdIsLongerThan50() {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[0] = "012345678901234567890123456789012345678901234567890";
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);
        String[] actualProduct = invalidProducts.get(0).getProductString().split("\t", -1);

        // THEN
        Assertions.assertTrue(hasError);
        Assertions.assertEquals(1, invalidProducts.size());
        Assertions.assertArrayEquals(testProduct, actualProduct);
        String errorMessage = invalidProducts.get(0).getErrorMessage();
        Assertions.assertEquals("id", errorMessage.split(" ")[0]);
        Assertions.assertTrue(errorMessage.endsWith("field contains unparsable and/or over-sized data"));
    }

    @Test
    void checkWhenTitleIsLongerThan100() {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[1] = generateBiggerStringThan(100);
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);
        String[] actualProduct = invalidProducts.get(0).getProductString().split("\t", -1);

        // THEN
        Assertions.assertTrue(hasError);
        Assertions.assertEquals(1, invalidProducts.size());
        Assertions.assertArrayEquals(testProduct, actualProduct);
        String errorMessage = invalidProducts.get(0).getErrorMessage();
        Assertions.assertEquals("title", errorMessage.split(" ")[0]);
        Assertions.assertTrue(errorMessage.endsWith("field contains unparsable and/or over-sized data"));
    }

    @Test
    void checkWhenDescriptionIsLongerThan5000() {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[2] = generateBiggerStringThan(5000);
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);
        String[] actualProduct = invalidProducts.get(0).getProductString().split("\t", -1);

        // THEN
        Assertions.assertTrue(hasError);
        Assertions.assertEquals(1, invalidProducts.size());
        Assertions.assertArrayEquals(testProduct, actualProduct);
        String errorMessage = invalidProducts.get(0).getErrorMessage();
        Assertions.assertEquals("description", errorMessage.split(" ")[0]);
        Assertions.assertTrue(errorMessage.endsWith("field contains unparsable and/or over-sized data"));
    }

    @ParameterizedTest(name = "Check if {0} is accepted as availability")
    @ValueSource(strings = {"in stock", "in_stock", "IN STOCK", "IN_STOCK",
            "out of stock", "out_of_stock", "OUT OF STOCK", "OUT_OF_STOCK",
            "backorder", "BACKORDER", "preorder", "PREORDER"})
    void checkAvailabilityValidValues(String value) {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[3] = value;
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);

        // THEN
        Assertions.assertFalse(hasError);
        Assertions.assertEquals(0, invalidProducts.size());
    }

    @ParameterizedTest(name = "Check if {0} is NOT accepted as availability")
    @ValueSource(strings = {"", "asdASD", "124466"})
    void checkAvailabilityInvalidValues(String value) {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[3] = value;
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);
        String[] actualProduct = invalidProducts.get(0).getProductString().split("\t", -1);

        // THEN
        Assertions.assertTrue(hasError);
        Assertions.assertEquals(1, invalidProducts.size());
        Assertions.assertArrayEquals(testProduct, actualProduct);
        String errorMessage = invalidProducts.get(0).getErrorMessage();
        Assertions.assertEquals("availability", errorMessage.split(" ")[0]);
        Assertions.assertTrue(errorMessage.endsWith("field contains unparsable and/or over-sized data"));
    }

    @ParameterizedTest(name = "Check if {0} is accepted as condition")
    @ValueSource(strings = {"new", "NEW", "refurbished", "REFURBISHED", "used", "USED"})
    void checkConditionValidValues(String value) {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[4] = value;
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);

        // THEN
        Assertions.assertFalse(hasError);
        Assertions.assertEquals(0, invalidProducts.size());
    }

    @ParameterizedTest(name = "Check if {0} is NOT accepted as condition")
    @ValueSource(strings = {"asdASD", "124466", "Other"})
    void checkConditionInvalidValues(String value) {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[4] = value;
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);
        String[] actualProduct = invalidProducts.get(0).getProductString().split("\t", -1);

        // THEN
        Assertions.assertTrue(hasError);
        Assertions.assertEquals(1, invalidProducts.size());
        Assertions.assertArrayEquals(testProduct, actualProduct);
        String errorMessage = invalidProducts.get(0).getErrorMessage();
        Assertions.assertEquals("condition", errorMessage.split(" ")[0]);
        Assertions.assertTrue(errorMessage.endsWith("field contains unparsable and/or over-sized data"));
    }

    @ParameterizedTest(name = "Check if {0} is accepted as price")
    @ValueSource(strings = {"100 HUF", "140.52 HUF", "15.44 USD", "16.00 EUR"})
    void checkPriceValidValues(String value) {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[5] = value;
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);

        // THEN
        Assertions.assertFalse(hasError);
        Assertions.assertEquals(0, invalidProducts.size());
    }

    @ParameterizedTest(name = "Check if {0} is NOT accepted as price")
    @ValueSource(strings = {"100,4 HUF", "140,52 HUF", "10 AAA", "25 Other", "25 EUR other"})
    void checkPriceInvalidValues(String value) {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[5] = value;
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);
        String[] actualProduct = invalidProducts.get(0).getProductString().split("\t", -1);

        // THEN
        Assertions.assertTrue(hasError);
        Assertions.assertEquals(1, invalidProducts.size());
        Assertions.assertArrayEquals(testProduct, actualProduct);
        String errorMessage = invalidProducts.get(0).getErrorMessage();
        Assertions.assertEquals("price", errorMessage.split(" ")[0]);
        Assertions.assertTrue(errorMessage.endsWith("field contains unparsable and/or over-sized data"));
    }

    @ParameterizedTest(name = "Check if {0} is accepted as sale_price")
    @ValueSource(strings = {"100 HUF", "140.52 HUF", "15.44 USD", "16.00 EUR"})
    void checkSalePriceValidValues(String value) {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[6] = value;
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);

        // THEN
        Assertions.assertFalse(hasError);
        Assertions.assertEquals(0, invalidProducts.size());
    }

    @ParameterizedTest(name = "Check if {0} is NOT accepted as sale_price")
    @ValueSource(strings = {"100,4 HUF", "140,52 HUF", "10 AAA", "25 Other", "25 EUR other"})
    void checkSalePriceInvalidValues(String value) {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[6] = value;
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);
        String[] actualProduct = invalidProducts.get(0).getProductString().split("\t", -1);

        // THEN
        Assertions.assertTrue(hasError);
        Assertions.assertEquals(1, invalidProducts.size());
        Assertions.assertArrayEquals(testProduct, actualProduct);
        String errorMessage = invalidProducts.get(0).getErrorMessage();
        Assertions.assertEquals("sale_price", errorMessage.split(" ")[0]);
        Assertions.assertTrue(errorMessage.endsWith("field contains unparsable and/or over-sized data"));
    }

    @ParameterizedTest(name = "Check if {0} is accepted as link")
    @ValueSource(strings = {"http://www.other.com/link", "https://www.other.com/link"})
    void checkLinkValidValues(String value) {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[7] = value;
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);

        // THEN
        Assertions.assertFalse(hasError);
        Assertions.assertEquals(0, invalidProducts.size());
    }

    @ParameterizedTest(name = "Check if {0} is NOT accepted as link")
    @ValueSource(strings = {"ftp://doodle.com/pub", "http://not:aacce?:ptable-blah%other",
            "https://not:acce?:ptable-blah%other", "s9d7f6sd7896HHJG"})
    void checkLinkInvalidValues(String value) {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[7] = value;
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);
        String[] actualProduct = invalidProducts.get(0).getProductString().split("\t", -1);

        // THEN
        Assertions.assertTrue(hasError);
        Assertions.assertEquals(1, invalidProducts.size());
        Assertions.assertArrayEquals(testProduct, actualProduct);
        String errorMessage = invalidProducts.get(0).getErrorMessage();
        Assertions.assertEquals("link", errorMessage.split(" ")[0]);
        Assertions.assertTrue(errorMessage.endsWith("field contains unparsable and/or over-sized data"));
    }

    @ParameterizedTest(name = "Check if {0}+1 length string is accepted as brand")
    @ValueSource(ints = {48, 49})
    void checkBrandValidWhenLessThan50(int value) {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[8] = generateBiggerStringThan(value);
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);

        // THEN
        Assertions.assertFalse(hasError);
        Assertions.assertEquals(0, invalidProducts.size());
    }

    @ParameterizedTest(name = "Check if {0}+1 length string is NOT accepted as brand")
    @ValueSource(ints = {50, 51})
    void checkBrandInvalidValues(int value) {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[8] = generateBiggerStringThan(value);
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);
        String[] actualProduct = invalidProducts.get(0).getProductString().split("\t", -1);

        // THEN
        Assertions.assertTrue(hasError);
        Assertions.assertEquals(1, invalidProducts.size());
        Assertions.assertArrayEquals(testProduct, actualProduct);
        String errorMessage = invalidProducts.get(0).getErrorMessage();
        Assertions.assertEquals("brand", errorMessage.split(" ")[0]);
        Assertions.assertTrue(errorMessage.endsWith("field contains unparsable and/or over-sized data"));
    }

    @ParameterizedTest(name = "Check if {0} is accepted as image_link")
    @ValueSource(strings = {"http://www.other.com/link", "https://www.other.com/link"})
    void checkImageLinkValidValues(String value) {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[9] = value;
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);

        // THEN
        Assertions.assertFalse(hasError);
        Assertions.assertEquals(0, invalidProducts.size());
    }

    @ParameterizedTest(name = "Check if {0} is NOT accepted as image_link")
    @ValueSource(strings = {"ftp://doodle.com/pub", "http://not:aacce?:ptable-blah%other",
            "https://not:acce?:ptable-blah%other", "s9d7f6sd7896HHJG"})
    void checkImageLinkInvalidValues(String value) {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[9] = value;
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);
        String[] actualProduct = invalidProducts.get(0).getProductString().split("\t", -1);

        // THEN
        Assertions.assertTrue(hasError);
        Assertions.assertEquals(1, invalidProducts.size());
        Assertions.assertArrayEquals(testProduct, actualProduct);
        String errorMessage = invalidProducts.get(0).getErrorMessage();
        Assertions.assertEquals("image_link", errorMessage.split(" ")[0]);
        Assertions.assertTrue(errorMessage.endsWith("field contains unparsable and/or over-sized data"));
    }

    @ParameterizedTest(name = "Check if {0} is accepted as age_group")
    @ValueSource(strings = {
            "NEWBORN", "newborn",
            "INFANT", "infant",
            "TODDLER", "toddler",
            "KIDS", "kids",
            "ADULT", "adult",
            "NOT_SPECIFIED", "not_specified",
            "NOT SPECIFIED", "not specified"
    })
    void checkAgeGroupValidValues(String value) {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[10] = value;
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);

        // THEN
        Assertions.assertFalse(hasError);
        Assertions.assertEquals(0, invalidProducts.size());
    }

    @ParameterizedTest(name = "Check if {0} is NOT accepted as age_group")
    @ValueSource(strings = {"asdASD", "124466", "Other"})
    void checkAgeGroupInvalidValues(String value) {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[10] = value;
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);
        String[] actualProduct = invalidProducts.get(0).getProductString().split("\t", -1);

        // THEN
        Assertions.assertTrue(hasError);
        Assertions.assertEquals(1, invalidProducts.size());
        Assertions.assertArrayEquals(testProduct, actualProduct);
        String errorMessage = invalidProducts.get(0).getErrorMessage();
        Assertions.assertEquals("age_group", errorMessage.split(" ")[0]);
        Assertions.assertTrue(errorMessage.endsWith("field contains unparsable and/or over-sized data"));
    }

    @ParameterizedTest(name = "Check if {0} is accepted as google_product_category")
    @ValueSource(strings = {"123448", "Category Example > Subcategory Example"})
    void checkGoogleProductCategoryValidValues(String value) {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[11] = value;
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);

        // THEN
        Assertions.assertFalse(hasError);
        Assertions.assertEquals(0, invalidProducts.size());
    }

    @ParameterizedTest(name = "Check if {0} is NOT accepted as google_product_category")
    @ValueSource(strings = {"", "Text123WithNumbers"})
    void checkGoogleProductCategoryInvalidValues(String value) {
        // GIVEN
        String[] testProduct = getValidTestProduct();
        testProduct[11] = value;
        List<InvalidProductDTO> invalidProducts = new LinkedList<>();

        // WHEN
        boolean hasError = underTest.hasError(testProduct, invalidProducts);
        String[] actualProduct = invalidProducts.get(0).getProductString().split("\t", -1);

        // THEN
        Assertions.assertTrue(hasError);
        Assertions.assertEquals(1, invalidProducts.size());
        Assertions.assertArrayEquals(testProduct, actualProduct);
        String errorMessage = invalidProducts.get(0).getErrorMessage();
        Assertions.assertEquals("google_product_category", errorMessage.split(" ")[0]);
        Assertions.assertTrue(errorMessage.endsWith("field contains unparsable and/or over-sized data"));
    }

    private String generateBiggerStringThan(int length) {
        int letterA = 65; // letter 'A'
        int letterZ = 90; // letter 'Z'
        return random.ints(letterA, letterZ + 1)
                .limit(length + 1)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private String[] getValidTestProduct() {
        String[] testProduct = new String[12];
        testProduct[0] = "12345ABCDE";
        testProduct[1] = "Some Title";
        testProduct[2] = "Some description which is longer";
        testProduct[3] = "IN_STOCK";
        testProduct[4] = "NEW";
        testProduct[5] = "100 HUF";
        testProduct[6] = "100 HUF";
        testProduct[7] = "https://www.asdasd.hu/valami.html";
        testProduct[8] = "Some Brand";
        testProduct[9] = "https://www.asdasd.hu/valami2.jpg";
        testProduct[10] = "ADULT";
        testProduct[11] = "12345";
        return testProduct;
    }
}