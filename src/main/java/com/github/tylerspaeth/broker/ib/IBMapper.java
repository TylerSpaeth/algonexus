package com.github.tylerspaeth.broker.ib;

import com.github.tylerspaeth.broker.ib.response.ContractDetails;
import com.github.tylerspaeth.broker.ib.response.RealtimeBar;
import com.github.tylerspaeth.broker.response.AccountPnL;
import com.github.tylerspaeth.broker.response.PositionPnL;
import com.github.tylerspaeth.common.data.entity.Candlestick;
import com.github.tylerspaeth.common.data.entity.Order;
import com.github.tylerspaeth.common.enums.*;
import com.ib.client.*;

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
            case OTHER -> null;
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

    /**
     * Map SideEnum to IB Action.
     * @param side SideEnum
     * @return Action
     */
    public static Types.Action mapSideEnumToAction(SideEnum side) {
        return switch(side) {
            case BUY -> Types.Action.BUY;
            case SELL -> Types.Action.SELL;
        };
    }

    /**
     * Map OrderTypeEnum to IB OrderType
     * @param orderType OrderTypeEnum
     * @return OrderType
     */
    public static OrderType mapOrderTypeEnumToOrderType(OrderTypeEnum orderType) {
        return switch(orderType) {
            case MKT -> OrderType.MKT;
            case LMT -> OrderType.LMT;
            case STP_LMT -> OrderType.STP_LMT;
            case STP -> OrderType.STP;
            case TRL_LMT -> OrderType.TRAIL_LIMIT;
            case MOC -> OrderType.MOC;
            case LOC -> OrderType.LOC;
        };
    }

    /**
     * Map TimeInForEnum to IB TimeInForce
     * @param timeInForce TimeInForceEnum
     * @return TimeInForce
     */
    public static Types.TimeInForce mapTimeInForceEnumToTimeInForce(TimeInForceEnum timeInForce) {
        return switch(timeInForce) {
            case DAY -> Types.TimeInForce.DAY;
            case GTC -> Types.TimeInForce.GTC;
            case IOC -> Types.TimeInForce.IOC;
        };
    }

    /**
     * Maps a standard domain Order into an IB Order
     * @param order Order containing contents to be mapped.
     * @param ibOrder IB Order object that will be populated from the domain Order.
     */
    public static void mapOrderToIBOrder(Order order, com.ib.client.Order ibOrder) {

        // Map Order to IB Order
        ibOrder.action(mapSideEnumToAction(order.getSide()));
        ibOrder.totalQuantity(Decimal.get(order.getQuantity()));
        if(order.getPrice() != null) {
            ibOrder.lmtPrice(order.getPrice());
        }
        ibOrder.orderType(mapOrderTypeEnumToOrderType(order.getOrderType()));
        ibOrder.tif(mapTimeInForceEnumToTimeInForce(order.getTimeInForce()));
        ibOrder.ocaGroup(order.getOCAGroup());
        ibOrder.transmit(order.isTransmit());
        if(order.getTrailAmount() != null) {
            ibOrder.trailStopPrice(order.getTrailAmount());
        }
        if(order.getTrailPercent() != null) {
            ibOrder.trailingPercent(order.getTrailPercent());
        }
        if(order.getParentOrder() != null) {
            ibOrder.parentId(Integer.parseInt(order.getParentOrder().getExternalOrderID()));
        }
    }

    /**
     * Map IB ContractDetails to ContractDetails.
     * @param ibContractDetails IB ContractDetails
     * @return ContractDetails
     */
    public static ContractDetails mapIBContractDetails(com.ib.client.ContractDetails ibContractDetails) {
        ContractDetails contractDetails = new ContractDetails();
        contractDetails.setContract(mapIBContract(ibContractDetails.contract()));
        return contractDetails;
    }

    /**
     * Map IB Contract to Contract
     * @param ibContract IB Contract
     * @return Contract
     */
    public static com.github.tylerspaeth.broker.ib.response.Contract mapIBContract(Contract ibContract) {
        com.github.tylerspaeth.broker.ib.response.Contract contract = new com.github.tylerspaeth.broker.ib.response.Contract();
        contract.setConid(ibContract.conid());
        contract.setSymbol(ibContract.symbol());
        contract.setSecType(ibContract.secType().name());
        contract.setLastTradeDateOrContractMonth(ibContract.lastTradeDateOrContractMonth());
        contract.setLastTradeDate(ibContract.lastTradeDate());
        contract.setExchange(ibContract.exchange());
        contract.setPrimaryExch(ibContract.primaryExch());
        contract.setCurrency(ibContract.currency());
        return contract;
    }

}
