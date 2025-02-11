package org.ducanh.apiiam.helpers;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class TimeHelpers {

    public OffsetDateTime currentTime() {
        return OffsetDateTime.now();
    }
}
