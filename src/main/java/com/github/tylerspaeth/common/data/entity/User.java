package com.github.tylerspaeth.common.data.entity;

import com.github.tylerspaeth.common.enums.AccountTypeEnum;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "UserID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userID;

    @Column(name = "ExternalAccountID")
    private String externalAccountID;

    @Column(name = "AccountType")
    @Enumerated(EnumType.STRING)
    private AccountTypeEnum accountType;

    public Integer getUserID() {
        return userID;
    }

    public String getExternalAccountID() {
        return externalAccountID;
    }

    public void setExternalAccountID(String externalAccountID) {
        this.externalAccountID = externalAccountID;
    }

    public AccountTypeEnum getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountTypeEnum accountType) {
        this.accountType = accountType;
    }
}
