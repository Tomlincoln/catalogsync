package hu.tomlincoln.catalogsync.dto;

public class InvalidProductDTO {

    private final String productString;
    private final String errorMessage;

    private InvalidProductDTO(Builder builder) {
        this.productString = builder.productString;
        this.errorMessage = builder.errorMessage;
    }

    public String getProductString() {
        return productString;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static class Builder {

        private String productString;
        private String errorMessage;

        public Builder withProductString(String[] product) {
            this.productString = String.join("\t", product);
            return this;
        }

        public Builder withErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public InvalidProductDTO build() {
            return new InvalidProductDTO(this);
        }
    }
}
