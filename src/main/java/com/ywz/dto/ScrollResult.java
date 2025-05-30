package com.ywz.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ScrollResult {
    private List<?> list;
    private Long minTime;
    private Integer offset;
}
