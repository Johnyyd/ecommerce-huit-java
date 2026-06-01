package com.huitshop.controller;

import com.huitshop.dto.WarrantyDtos.WarrantyDto;
import com.huitshop.service.WarrantyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warranties")
public class WarrantyController {
    private final WarrantyService warrantyService = new WarrantyService();

    @GetMapping("/search")
    public ResponseEntity<?> searchWarranty(@RequestParam String serialNumber) {
        WarrantyDto dto = warrantyService.getWarrantyBySerial(serialNumber);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy thông tin bảo hành cho số serial này");
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<WarrantyDto>> getRecentWarranties() {
        return ResponseEntity.ok(warrantyService.getRecentWarranties());
    }
}
