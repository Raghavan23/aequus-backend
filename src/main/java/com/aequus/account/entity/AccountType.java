package com.aequus.account.entity;

public enum AccountType {
    SAVINGS,
    CURRENT,
    SALARY;

    /**
     * Determines whether this account type is a liability (debt) or asset.
     * All current supported accounts (Savings, Current, Salary) are asset accounts.
     */
    public boolean isLiability() {
        return false;
    }

    public boolean isAsset() {
        return true;
    }
}

