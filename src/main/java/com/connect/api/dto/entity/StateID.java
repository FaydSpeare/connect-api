package com.connect.api.dto.entity;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class StateID implements Serializable {
    public Long gameId;
    public Long moveNumber;
}
