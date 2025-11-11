package com.codeit.filter.service;

import com.codeit.filter.dto.AuditRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class AuditService {
    private final List<AuditRecord> store = new CopyOnWriteArrayList<>();

    public void save(AuditRecord record) {
        store.add(record);
        log.info("AUDIT {} {} -> {} by {} ({} ms, ip={})",
                record.getMethod(),
                record.getUri(),
                record.getStatusCode(),
                record.getUsername(),
                record.getDuration(),
                record.getIpAddress());
    }

    public List<AuditRecord> findAll() {
        return List.copyOf(store);
    }

    public List<AuditRecord> findByUsername(String username) {
        return store.stream()
                .filter(r -> username.equals(r.getUsername()))
                .toList();
    }

    public long count() {
        return store.size();
    }
}
