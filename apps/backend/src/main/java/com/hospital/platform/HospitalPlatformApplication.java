package com.hospital.platform;

import java.time.ZoneId;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HospitalPlatformApplication {

    static {
        // PostgreSQL time without time zone must be mapped the same way on every host.
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("America/Lima")));
    }

    public static void main(String[] args) {
        SpringApplication.run(HospitalPlatformApplication.class, args);
    }
}
