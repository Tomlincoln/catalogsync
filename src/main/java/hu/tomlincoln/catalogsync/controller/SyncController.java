package hu.tomlincoln.catalogsync.controller;

import hu.tomlincoln.catalogsync.dto.ReportDTO;
import hu.tomlincoln.catalogsync.service.SynchronizerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

@RestController
public class SyncController {

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
        ReportDTO reportDTO = synchronizerService.synchronize();
        return ResponseEntity.ok().body(reportDTO);
    }

}
