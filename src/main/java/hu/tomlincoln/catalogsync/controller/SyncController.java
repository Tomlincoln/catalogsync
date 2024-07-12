package hu.tomlincoln.catalogsync.controller;

import hu.tomlincoln.catalogsync.dto.ReportDTO;
import hu.tomlincoln.catalogsync.service.SynchronizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

@RestController
public class SyncController {

    private static final Logger LOG = LoggerFactory.getLogger(SyncController.class);
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[1-3]$");

    private final SynchronizerService synchronizerService;

    public SyncController(SynchronizerService synchronizerService) {
        this.synchronizerService = synchronizerService;
    }

    @GetMapping("/sync/{fileId}")
    public ResponseEntity<ReportDTO> sync(@PathVariable final String fileId) {
        if (!FILENAME_PATTERN.matcher(fileId).matches()) {
            return ResponseEntity.badRequest().build();
        }
        long startTime = System.currentTimeMillis();
        LOG.debug("Starting synchronizing...");
        ReportDTO reportDTO = synchronizerService.synchronize("file" + fileId + ".txt");
        long endTime = System.currentTimeMillis();
        LOG.debug("Finished synchronizing took " + (endTime - startTime) + "ms");
        return ResponseEntity.ok().body(reportDTO);
    }

}
