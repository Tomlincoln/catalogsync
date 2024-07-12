package hu.tomlincoln.catalogsync.controller;

import hu.tomlincoln.catalogsync.domain.Product;
import hu.tomlincoln.catalogsync.service.DumpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DumpController {

    private final DumpService dumpService;

    public DumpController(DumpService dumpService) {
        this.dumpService = dumpService;
    }

    @GetMapping("/dump")
    public ResponseEntity<List<Product>> dump() {
        return ResponseEntity.ok().body(dumpService.dumpAll());
    }

}
