package study.querydsl.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Embeddable
public class Period {
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public Period(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Period() {
    }
}
