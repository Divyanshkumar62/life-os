package com.lifeos.player.dto;

import com.lifeos.player.domain.enums.AttributeType;

public class StatAllocationRequest {
    private AttributeType stat;
    private int amount;
    
    public AttributeType getStat() {
        return stat;
    }
    
    public void setStat(AttributeType stat) {
        this.stat = stat;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public void setAmount(int amount) {
        this.amount = amount;
    }
}
