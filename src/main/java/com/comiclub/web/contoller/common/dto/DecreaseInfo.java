package com.comiclub.web.contoller.common.dto;


import lombok.Data;

@Data
public class DecreaseInfo {

    private Long motherId;
    private Long totalReply;

    public DecreaseInfo(Long motherId, Long totalReply) {
        this.motherId = motherId;
        this.totalReply = totalReply;
    }
}
