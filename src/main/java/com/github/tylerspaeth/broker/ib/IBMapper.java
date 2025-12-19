package com.github.tylerspaeth.broker.ib;

import com.github.tylerspaeth.broker.ib.response.RealtimeBar;
import com.github.tylerspaeth.broker.response.AccountPnL;
import com.github.tylerspaeth.broker.response.PositionPnL;
import com.github.tylerspaeth.common.data.entity.Candlestick;
import com.github.tylerspaeth.common.enums.AssetTypeEnum;
import com.github.tylerspaeth.common.enums.OrderStatusEnum;
import com.ib.client.OrderStatus;
import com.ib.client.Types;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Maps IB specific objects / formats into the standard formats used in this application. Also, can do the same in reverse.
 */
public class IBMapper {

    /**
     * Maps the IB secType to an AssetTypeEnum value
     * @param secType The SecType value IB uses
     * @return AssetTypeEnum
     */
    public static AssetTypeEnum mapSecTypeToAssetType(Types.SecType secType) {
        return switch (secType) {
            case STK -> AssetTypeEnum.EQUITIES;
            case FUT -> AssetTypeEnum.FUTURES;
            case CASH -> AssetTypeEnum.FOREX;
            case OPT -> AssetTypeEnum.OPTIONS;
            case CRYPTO -> AssetTypeEnum.CRYPTOCURRENCY;
            default -> AssetTypeEnum.OTHER;
        };
    }

    /**
     * Maps the AssetTypeEnum value to the corresponding IB secType
     * @param assetType Application asset type format
     * @return SecType
     */
    public static Types.SecType mapAssetTypeToSecType(AssetTypeEnum assetType) {
        return switch (assetType) {
            case EQUITIES -> Types.SecType.STK;
            case FUTURES -> Types.SecType.FUT;
            case FOREX -> Types.SecType.CASH;
            case OPTIONS -> Types.SecType.OPT;
            case CRYPTOCURRENCY -> Types.SecType.CRYPTO;
            default -> null;
        };
    }

    /**
     * Maps the IB AccountPnL to the one used by the rest of the application.
     * @param accountPnL The IB response version of AccountPnL.
     * @return The AccountPnL used by the rest of the application.
     */
    public static AccountPnL mapAccountPnL(com.github.tylerspaeth.broker.ib.response.AccountPnL accountPnL) {
        return new AccountPnL(accountPnL.dailyPnL(), accountPnL.unrealizedPnL(), accountPnL.realizedPnL());
    }

    /**
     * Maps the IB PositionPnL to the one used by the rest of the application.
     * @param positionPnL The IB response version of PositionPnL.
     * @return The PositionPnL used by the rest of the application.
     */
    public static PositionPnL mapPositionPnL(com.github.tylerspaeth.broker.ib.response.PositionPnL positionPnL) {
        return new PositionPnL(positionPnL.position().value().doubleValue(), positionPnL.dailyPnL(), positionPnL.unrealizedPnL(), positionPnL.realizedPnL(), positionPnL.value());
    }

    /**
     * Maps an IB RealTimeBar to a Candlestick object.
     * @param realtimeBar RealTimeBar from IB.
     * @return Candlestick
     */
    public static Candlestick mapRealTimeBarToCandlestick(RealtimeBar realtimeBar) {
        Candlestick candlestick = new Candlestick();
        candlestick.setOpen((float)realtimeBar.open());
        candlestick.setHigh((float)realtimeBar.high());
        candlestick.setLow((float)realtimeBar.low());
        candlestick.setClose((float)realtimeBar.close());
        candlestick.setVolume((float)realtimeBar.volume().longValue());
        candlestick.setTimestamp(Timestamp.from(Instant.ofEpochSecond(realtimeBar.date())));
        return candlestick;
    }

    /**
     * Map IB OrderStatus to OrderStatusEnum value.
     * @param orderStatus IB OrderStatus value.
     * @return Matching OrderStatusEnum value.
     */
    public static OrderStatusEnum mapOrderStatus(OrderStatus orderStatus) {
        return switch(orderStatus) {
            case PendingSubmit, PreSubmitted -> OrderStatusEnum.PENDING_SUBMIT;
            case ApiPending -> OrderStatusEnum.PENDING;
            case Submitted ->  OrderStatusEnum.SUBMITTED;
            case Cancelled, ApiCancelled -> OrderStatusEnum.CANCELLED;
            case PendingCancel -> OrderStatusEnum.PENDING_CANCEL;
            case Inactive -> OrderStatusEnum.INACTIVE;
            case Filled -> OrderStatusEnum.FILLED;
            case null -> null;
            default -> OrderStatusEnum.OTHER;
        };
    }

}
