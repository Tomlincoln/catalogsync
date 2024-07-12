package hu.tomlincoln.catalogsync.service;

import hu.tomlincoln.catalogsync.dto.ReportDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SynchronizerServiceTest {

    @Autowired
    private SynchronizerService underTest;

    @Test
    void synchronizeGivesEmptyReportOnIOException() {
        // GIVEN

        // WHEN
        ReportDTO reportDto = underTest.synchronize("unit-test-55.txt");

        //THEN
        Assertions.assertEquals(ReportDTO.getEmptyReport(), reportDto);
    }

    @Test
    void synchronizeGivesEmptyReportOnNoHeader() {
        // GIVEN

        // WHEN
        ReportDTO reportDto = underTest.synchronize("unit-test-1-without-header.txt");

        //THEN
        Assertions.assertEquals(ReportDTO.getEmptyReport(), reportDto);
    }

    @Test
    void synchronizeGivesOkWithValidRow() {
        // GIVEN

        // WHEN
        ReportDTO reportDto = underTest.synchronize("unit-test-1-valid.txt");

        //THEN
        Assertions.assertEquals(0, reportDto.getInvalidProducts().size());
        Assertions.assertEquals(1, reportDto.getAdded());
    }

    @Test
    void synchronizeAddsValidButNotInvalid() {
        // GIVEN

        // WHEN
        ReportDTO reportDto = underTest.synchronize("unit-test-1-valid-2-invalid.txt");

        //THEN
        Assertions.assertEquals(2, reportDto.getInvalidProducts().size());
        Assertions.assertEquals(2, reportDto.getSkipped());
        Assertions.assertEquals(1, reportDto.getAdded());
    }

    @Test
    void synchronizeKeepsValid() {
        // GIVEN

        // WHEN
        underTest.synchronize("unit-test-1-valid-2-invalid.txt");
        ReportDTO reportDto = underTest.synchronize("unit-test-1-valid-2-invalid.txt");

        //THEN
        Assertions.assertEquals(2, reportDto.getInvalidProducts().size());
        Assertions.assertEquals(2, reportDto.getSkipped());
        Assertions.assertEquals(1, reportDto.getNotChanged());
    }

    @Test
    void synchronizeUpdatesNew() {
        // GIVEN

        // WHEN
        underTest.synchronize("unit-test-1-valid.txt");
        ReportDTO reportDto = underTest.synchronize("unit-test-1-valid-updated.txt");

        //THEN
        Assertions.assertEquals(1, reportDto.getUpdated());
    }

    @Test
    void synchronizeNotChangesSame() {
        // GIVEN

        // WHEN
        underTest.synchronize("unit-test-1-valid.txt");
        ReportDTO reportDto = underTest.synchronize("unit-test-1-valid.txt");

        //THEN
        Assertions.assertEquals(1, reportDto.getNotChanged());
    }

    @Test
    void synchronizeDeletesRemoved() {
        // GIVEN

        // WHEN
        underTest.synchronize("unit-test-2-valid.txt");
        ReportDTO reportDto = underTest.synchronize("unit-test-1-valid.txt");

        //THEN
        Assertions.assertEquals(1, reportDto.getDeleted());
    }

    @Test
    void synchronizeShowsMax12InvalidProducts() {
        // GIVEN

        // WHEN
        ReportDTO reportDto = underTest.synchronize("unit-test-13-invalid.txt");

        //THEN
        Assertions.assertEquals(12, reportDto.getInvalidProducts().size());
    }

    @Test
    void synchronizeShowsSkippedCorrectlyWithMoreThan12InvalidProducts() {
        // GIVEN

        // WHEN
        ReportDTO reportDto = underTest.synchronize("unit-test-13-invalid.txt");

        //THEN
        Assertions.assertEquals(13, reportDto.getSkipped());
    }

}