package com.github.tylerspaeth.common.data.entity;

import com.github.tylerspaeth.common.enums.AssetTypeEnum;
import jakarta.persistence.*;

/**
 * These are applied per side, per contract/share. If there is no symbol that means it is the default commission applied to the asset type.
 * If there is a symbol then the commission amount for that symbol overrides the default if one exists.
 */
@Entity
@Table(name = "Commissions")
public class Commission {

    @Id
    @Column(name = "CommissionID")
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Integer commissionID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SymbolID", referencedColumnName = "SymbolID")
    private Symbol symbol;

    @Column(name = "AssetType")
    @Enumerated(EnumType.STRING)
    private AssetTypeEnum assetType;

    @Column(name = "CommissionAmount")
    private Float commissionAmount;

    public Integer getCommissionID() {
        return commissionID;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public Float getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(Float commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public AssetTypeEnum getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetTypeEnum assetType) {
        this.assetType = assetType;
    }
}
