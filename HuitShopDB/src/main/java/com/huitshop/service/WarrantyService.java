package com.huitshop.service;

import com.huitshop.dao.InventoryDao;
import com.huitshop.dto.WarrantyDtos.WarrantyDto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class WarrantyService {
    private final InventoryDao inventoryDao = new InventoryDao();

    public WarrantyDto getWarrantyBySerial(String serialNumber) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            return null;
        }
        WarrantyDto dto = inventoryDao.findProductSerialByNumber(serialNumber.trim());
        if (dto != null) {
            calculateWarrantyStatus(dto);
        }
        return dto;
    }

    public List<WarrantyDto> getRecentWarranties() {
        List<WarrantyDto> list = inventoryDao.findRecentProductSerials();
        for (WarrantyDto dto : list) {
            calculateWarrantyStatus(dto);
        }
        return list;
    }

    private void calculateWarrantyStatus(WarrantyDto dto) {
        if (!"SOLD".equalsIgnoreCase(dto.getStatus())) {
            dto.setStatus("NOT_SOLD");
            dto.setDaysRemaining(0);
        } else if (dto.getExpireDate() != null) {
            LocalDate today = LocalDate.now();
            if (today.isAfter(dto.getExpireDate())) {
                dto.setStatus("EXPIRED");
                dto.setDaysRemaining(0);
            } else {
                dto.setStatus("ACTIVE");
                long days = ChronoUnit.DAYS.between(today, dto.getExpireDate());
                dto.setDaysRemaining((int) days);
            }
        } else {
            dto.setStatus("UNKNOWN");
            dto.setDaysRemaining(0);
        }
    }
}
