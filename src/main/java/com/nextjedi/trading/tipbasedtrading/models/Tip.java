package com.nextjedi.trading.tipbasedtrading.models;

import com.zerodhatech.models.Instrument;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.List;

@Entity
public class Tip {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    String name;
//    Instrument ins;
    long trigger;
//    List<long> target;
    long stoploss;
    String type;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date")
    private Date createDate;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modify_date")
    private Date modifyDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
