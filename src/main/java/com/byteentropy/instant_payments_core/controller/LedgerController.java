package com.byteentropy.instant_payments_core.controller;
import com.byteentropy.instant_payments_core.model.LedgerEntry;
import com.byteentropy.instant_payments_core.repository.LedgerRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ledger")
public class LedgerController {
    private final LedgerRepository repository;
    public LedgerController(LedgerRepository repository) { this.repository = repository; }
    @GetMapping("/history") public List<LedgerEntry> getAll() { return repository.findAll(); }
}