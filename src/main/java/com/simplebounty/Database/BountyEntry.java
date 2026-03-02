package com.simplebounty.Database;

public class BountyEntry {
    public final String itemStackJson;
    public final int amount;

    public BountyEntry(String itemStackJson, int amount) {
        this.itemStackJson = itemStackJson;
        this.amount = amount;
    }
}