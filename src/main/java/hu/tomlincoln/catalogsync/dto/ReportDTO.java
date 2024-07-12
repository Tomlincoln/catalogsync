package hu.tomlincoln.catalogsync.dto;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ReportDTO {

    private final long added;
    private final long updated;
    private final long notChanged;
    private final long deleted;
    private final long skipped;
    private final List<InvalidProductDTO> invalidProducts;

    private ReportDTO(Builder builder) {
        this.added = builder.added;
        this.updated = builder.updated;
        this.notChanged = builder.notChanged;
        this.deleted = builder.deleted;
        this.skipped = builder.skipped;
        this.invalidProducts = Collections.unmodifiableList(builder.invalidProducts);
    }

    public long getAdded() {
        return added;
    }

    public long getUpdated() {
        return updated;
    }

    public long getNotChanged() {
        return notChanged;
    }

    public long getDeleted() {
        return deleted;
    }

    public long getSkipped() {
        return skipped;
    }

    public List<InvalidProductDTO> getInvalidProducts() {
        return invalidProducts;
    }

    public static ReportDTO getEmptyReport() {
        return new ReportDTO.Builder()
                .withAdded(0)
                .withUpdated(0)
                .withNotChanged(0)
                .withDeleted(0)
                .withSkipped(0)
                .withInvalidProducts(Collections.emptyList())
                .build();
    }

    public static class Builder {
        private long added;
        private long updated;
        private long notChanged;
        private long deleted;
        private long skipped;
        private final MaxSizedLinkedList<InvalidProductDTO> invalidProducts = new MaxSizedLinkedList<>(12);

        public Builder withAdded(long added) {
            this.added = added;
            return this;
        }

        public Builder withUpdated(long updated) {
            this.updated = updated;
            return this;
        }

        public Builder withNotChanged(long notChanged) {
            this.notChanged = notChanged;
            return this;
        }

        public Builder withDeleted(long deleted) {
            this.deleted = deleted;
            return this;
        }

        public Builder withSkipped(long skipped) {
            this.skipped = skipped;
            return this;
        }

        public Builder withInvalidProducts(Collection<InvalidProductDTO> invalidProducts) {
            this.invalidProducts.addAll(invalidProducts);
            return this;
        }

        public ReportDTO build() {
            return new ReportDTO(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReportDTO)) return false;
        ReportDTO reportDTO = (ReportDTO) o;
        return added == reportDTO.added && updated == reportDTO.updated
                && notChanged == reportDTO.notChanged && deleted == reportDTO.deleted
                && skipped == reportDTO.skipped && Objects.equals(invalidProducts, reportDTO.invalidProducts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(added, updated, notChanged, deleted, skipped, invalidProducts);
    }
}
