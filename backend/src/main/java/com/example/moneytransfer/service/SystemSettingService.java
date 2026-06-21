package com.example.moneytransfer.service;

import com.example.moneytransfer.domain.entity.SystemSetting;
import com.example.moneytransfer.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class SystemSettingService {

    private final SystemSettingRepository repository;

    public SystemSettingService(SystemSettingRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public String getSetting(String key, String defaultValue) {
        return repository.findById(key)
                .map(SystemSetting::getSettingValue)
                .orElseGet(() -> {
                    SystemSetting setting = new SystemSetting(key, defaultValue);
                    repository.save(setting);
                    return defaultValue;
                });
    }

    @Transactional
    public void setSetting(String key, String value) {
        SystemSetting setting = repository.findById(key)
                .orElse(new SystemSetting(key, value));
        setting.setSettingValue(value);
        repository.save(setting);
    }

    public BigDecimal getMinTransferAmount() {
        try {
            return new BigDecimal(getSetting("min_transfer_amount", "1.00"));
        } catch (Exception e) {
            return BigDecimal.ONE;
        }
    }

    public BigDecimal getMaxTransferAmount() {
        try {
            return new BigDecimal(getSetting("max_transfer_amount", "10000.00"));
        } catch (Exception e) {
            return new BigDecimal("10000.00");
        }
    }

    public boolean isTransfersEnabled() {
        return Boolean.parseBoolean(getSetting("transfers_enabled", "true"));
    }

    @Transactional
    public Map<String, Object> getAllSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("minTransferAmount", getMinTransferAmount());
        settings.put("maxTransferAmount", getMaxTransferAmount());
        settings.put("transfersEnabled", isTransfersEnabled());
        return settings;
    }

    @Transactional
    public void updateSettings(Map<String, Object> newSettings) {
        if (newSettings.containsKey("minTransferAmount")) {
            setSetting("min_transfer_amount", newSettings.get("minTransferAmount").toString());
        }
        if (newSettings.containsKey("maxTransferAmount")) {
            setSetting("max_transfer_amount", newSettings.get("maxTransferAmount").toString());
        }
        if (newSettings.containsKey("transfersEnabled")) {
            setSetting("transfers_enabled", newSettings.get("transfersEnabled").toString());
        }
    }
}
