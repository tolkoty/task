package com.test.task.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "response_log")
@NoArgsConstructor
public class ResponseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "remote_id")
    private Long remoteId;

    @Column(name = "data")
    private String data;

    @Column(name = "status")
    private int status;

    @Column(name = "message")
    private String message;
}
