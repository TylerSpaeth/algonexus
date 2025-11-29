package com.github.tylerspaeth.broker.ib;

import com.github.tylerspaeth.common.MultiReaderQueue;
import com.github.tylerspaeth.broker.response.*;
import com.ib.client.*;
import com.ib.client.protobuf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IBWrapper implements EWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(IBWrapper.class);

    private final IBConnection ibConnection;

    public IBWrapper(IBConnection ibConnection) {
        this.ibConnection = ibConnection;
    }

    @Override
    public void tickPrice(int i, int i1, double v, TickAttrib tickAttrib) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickSize(int i, int i1, Decimal decimal) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickOptionComputation(int i, int i1, int i2, double v, double v1, double v2, double v3, double v4, double v5, double v6, double v7) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickGeneric(int i, int i1, double v) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickString(int i, int i1, String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickEFP(int i, int i1, double v, String s, double v1, int i2, String s1, double v2, double v3) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void orderStatus(int i, String s, Decimal decimal, Decimal decimal1, double v, long l, int i1, double v1, int i2, String s1, double v2) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void openOrder(int i, Contract contract, Order order, OrderState orderState) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void openOrderEnd() {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void updateAccountValue(String s, String s1, String s2, String s3) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void updatePortfolio(Contract contract, Decimal decimal, double v, double v1, double v2, double v3, double v4, String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void updateAccountTime(String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void accountDownloadEnd(String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void nextValidId(int i) {
        ibConnection.onNextValidId(i);
    }

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {
        ContractDetailsResponse existingValue = ibConnection.ibRequestRepository.getFutureValue(String.valueOf(reqId));
        if(existingValue == null) {
            existingValue = new ContractDetailsResponse();
        }
        existingValue.contractDetails.add(contractDetails);
        ibConnection.ibRequestRepository.setFutureValue(String.valueOf(reqId), existingValue);
    }

    @Override
    public void bondContractDetails(int reqId, ContractDetails contractDetails) {
        ContractDetailsResponse existingValue = ibConnection.ibRequestRepository.getFutureValue(String.valueOf(reqId));
        if(existingValue == null) {
            existingValue = new ContractDetailsResponse();
        }
        existingValue.contractDetails.add(contractDetails);
        ibConnection.ibRequestRepository.setFutureValue(String.valueOf(reqId), existingValue);
    }

    @Override
    public void contractDetailsEnd(int reqId) {
        ibConnection.ibRequestRepository.removePendingRequest(String.valueOf(reqId));
    }

    @Override
    public void execDetails(int i, Contract contract, Execution execution) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void execDetailsEnd(int i) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void updateMktDepth(int i, int i1, int i2, int i3, double v, Decimal decimal) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void updateMktDepthL2(int i, int i1, String s, int i2, int i3, double v, Decimal decimal, boolean b) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void updateNewsBulletin(int i, int i1, String s, String s1) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void managedAccounts(String accounts) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void receiveFA(int i, String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void historicalData(int i, Bar bar) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void scannerParameters(String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void scannerData(int i, int i1, ContractDetails contractDetails, String s, String s1, String s2, String s3) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void scannerDataEnd(int i) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void realtimeBar(int reqId, long date, double open, double high, double low, double close, Decimal volume, Decimal vwap, int count) {
        MultiReaderQueue<OHLCV> queue = ibConnection.datafeedReqIdMap.get(reqId);
        queue.write(new OHLCV(Timestamp.from(Instant.ofEpochSecond(date)), open, high, low, close, volume.longValue()));
    }

    @Override
    public void currentTime(long l) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void fundamentalData(int i, String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void deltaNeutralValidation(int i, DeltaNeutralContract deltaNeutralContract) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickSnapshotEnd(int i) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void marketDataType(int i, int i1) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void commissionAndFeesReport(CommissionAndFeesReport commissionAndFeesReport) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void position(String accountId, Contract contract, Decimal position, double avgCost) {
        PositionResponse existingValue = ibConnection.ibRequestRepository.getFutureValue(IBRequestRepository.POSITION_REQ_MAP_KEY);
        if(existingValue == null) {
            existingValue = new PositionResponse();
            existingValue.accountId = accountId;
        }
        existingValue.positions.add(new Position(contract, position, avgCost));
        ibConnection.ibRequestRepository.setFutureValue(IBRequestRepository.POSITION_REQ_MAP_KEY, existingValue);
    }

    @Override
    public void positionEnd() {
        ibConnection.ibRequestRepository.removePendingRequest(IBRequestRepository.POSITION_REQ_MAP_KEY);
    }

    @Override
    public void accountSummary(int reqId, String accountId, String tag, String value, String currency) {
        AccountSummaryResponse existingValue = ibConnection.ibRequestRepository.getFutureValue(String.valueOf(reqId));
        if(existingValue == null) {
            existingValue = new AccountSummaryResponse();
        }
        existingValue.accountSummaries.add(new AccountSummary(accountId,tag,value,currency));
        ibConnection.ibRequestRepository.setFutureValue(String.valueOf(reqId), existingValue);
    }

    @Override
    public void accountSummaryEnd(int reqId) {
        ibConnection.ibRequestRepository.removePendingRequest(String.valueOf(reqId));
    }

    @Override
    public void verifyMessageAPI(String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void verifyCompleted(boolean b, String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void verifyAndAuthMessageAPI(String s, String s1) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void verifyAndAuthCompleted(boolean b, String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void displayGroupList(int i, String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void displayGroupUpdated(int i, String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void error(Exception e) {
        LOGGER.error("IB Error", e);
    }

    @Override
    public void error(String s) {
        LOGGER.error(s);
    }

    @Override
    public void error(int i, long l, int i1, String s, String s1) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void connectionClosed() {
        ibConnection.onConnectionClosed();
    }

    @Override
    public void connectAck() {
        ibConnection.onConnectAck();
    }

    @Override
    public void positionMulti(int reqId, String account, String modelCode, Contract contract, Decimal pos, double avgCost) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void positionMultiEnd(int reqId) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void accountUpdateMulti(int i, String s, String s1, String s2, String s3, String s4) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void accountUpdateMultiEnd(int i) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void securityDefinitionOptionalParameter(int i, String s, int i1, String s1, String s2, Set<String> set, Set<Double> set1) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void securityDefinitionOptionalParameterEnd(int i) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void softDollarTiers(int i, SoftDollarTier[] softDollarTiers) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void familyCodes(FamilyCode[] familyCodes) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void symbolSamples(int reqId, ContractDescription[] contractDescriptions) {
        ibConnection.ibRequestRepository.setFutureValue(String.valueOf(reqId), contractDescriptions);
        ibConnection.ibRequestRepository.removePendingRequest(String.valueOf(reqId));
    }

    @Override
    public void historicalDataEnd(int i, String s, String s1) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickNews(int i, long l, String s, String s1, String s2, String s3) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void smartComponents(int i, Map<Integer, Map.Entry<String, Character>> map) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickReqParams(int i, double v, String s, int i1) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void newsProviders(NewsProvider[] newsProviders) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void newsArticle(int i, int i1, String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void historicalNews(int i, String s, String s1, String s2, String s3) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void historicalNewsEnd(int i, boolean b) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void headTimestamp(int i, String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void histogramData(int i, List<HistogramEntry> list) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void historicalDataUpdate(int i, Bar bar) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void rerouteMktDataReq(int i, int i1, String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void rerouteMktDepthReq(int i, int i1, String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void marketRule(int i, PriceIncrement[] priceIncrements) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL) {
        ibConnection.ibRequestRepository.setFutureValue(String.valueOf(reqId), new AccountPnLResponse(dailyPnL, unrealizedPnL, realizedPnL));
        ibConnection.ibRequestRepository.removePendingRequest(String.valueOf(reqId));
    }

    @Override
    public void pnlSingle(int reqId, Decimal position, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {
        ibConnection.ibRequestRepository.setFutureValue(String.valueOf(reqId), new PositionPnLResponse(position, dailyPnL, unrealizedPnL, realizedPnL, value));
        ibConnection.ibRequestRepository.removePendingRequest(String.valueOf(reqId));
    }

    @Override
    public void historicalTicks(int i, List<HistoricalTick> list, boolean b) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void historicalTicksBidAsk(int i, List<HistoricalTickBidAsk> list, boolean b) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void historicalTicksLast(int i, List<HistoricalTickLast> list, boolean b) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickByTickAllLast(int i, int i1, long l, double v, Decimal decimal, TickAttribLast tickAttribLast, String s, String s1) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickByTickBidAsk(int i, long l, double v, double v1, Decimal decimal, Decimal decimal1, TickAttribBidAsk tickAttribBidAsk) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickByTickMidPoint(int i, long l, double v) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void orderBound(long l, int i, int i1) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void completedOrder(Contract contract, Order order, OrderState orderState) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void completedOrdersEnd() {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void replaceFAEnd(int i, String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void wshMetaData(int i, String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void wshEventData(int i, String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void historicalSchedule(int i, String s, String s1, String s2, List<HistoricalSession> list) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void userInfo(int i, String s) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void currentTimeInMillis(long l) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void orderStatusProtoBuf(OrderStatusProto.OrderStatus orderStatus) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void openOrderProtoBuf(OpenOrderProto.OpenOrder openOrder) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void openOrdersEndProtoBuf(OpenOrdersEndProto.OpenOrdersEnd openOrdersEnd) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void errorProtoBuf(ErrorMessageProto.ErrorMessage errorMessage) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void execDetailsProtoBuf(ExecutionDetailsProto.ExecutionDetails executionDetails) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void execDetailsEndProtoBuf(ExecutionDetailsEndProto.ExecutionDetailsEnd executionDetailsEnd) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void completedOrderProtoBuf(CompletedOrderProto.CompletedOrder completedOrder) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void completedOrdersEndProtoBuf(CompletedOrdersEndProto.CompletedOrdersEnd completedOrdersEnd) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void orderBoundProtoBuf(OrderBoundProto.OrderBound orderBound) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void contractDataProtoBuf(ContractDataProto.ContractData contractData) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void bondContractDataProtoBuf(ContractDataProto.ContractData contractData) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void contractDataEndProtoBuf(ContractDataEndProto.ContractDataEnd contractDataEnd) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickPriceProtoBuf(TickPriceProto.TickPrice tickPrice) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickSizeProtoBuf(TickSizeProto.TickSize tickSize) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickOptionComputationProtoBuf(TickOptionComputationProto.TickOptionComputation tickOptionComputation) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickGenericProtoBuf(TickGenericProto.TickGeneric tickGeneric) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickStringProtoBuf(TickStringProto.TickString tickString) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickSnapshotEndProtoBuf(TickSnapshotEndProto.TickSnapshotEnd tickSnapshotEnd) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void updateMarketDepthProtoBuf(MarketDepthProto.MarketDepth marketDepth) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void updateMarketDepthL2ProtoBuf(MarketDepthL2Proto.MarketDepthL2 marketDepthL2) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void marketDataTypeProtoBuf(MarketDataTypeProto.MarketDataType marketDataType) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickReqParamsProtoBuf(TickReqParamsProto.TickReqParams tickReqParams) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void updateAccountValueProtoBuf(AccountValueProto.AccountValue accountValue) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void updatePortfolioProtoBuf(PortfolioValueProto.PortfolioValue portfolioValue) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void updateAccountTimeProtoBuf(AccountUpdateTimeProto.AccountUpdateTime accountUpdateTime) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void accountDataEndProtoBuf(AccountDataEndProto.AccountDataEnd accountDataEnd) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void managedAccountsProtoBuf(ManagedAccountsProto.ManagedAccounts managedAccounts) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void positionProtoBuf(PositionProto.Position position) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void positionEndProtoBuf(PositionEndProto.PositionEnd positionEnd) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void accountSummaryProtoBuf(AccountSummaryProto.AccountSummary accountSummary) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void accountSummaryEndProtoBuf(AccountSummaryEndProto.AccountSummaryEnd accountSummaryEnd) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void positionMultiProtoBuf(PositionMultiProto.PositionMulti positionMulti) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void positionMultiEndProtoBuf(PositionMultiEndProto.PositionMultiEnd positionMultiEnd) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void accountUpdateMultiProtoBuf(AccountUpdateMultiProto.AccountUpdateMulti accountUpdateMulti) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void accountUpdateMultiEndProtoBuf(AccountUpdateMultiEndProto.AccountUpdateMultiEnd accountUpdateMultiEnd) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void historicalDataProtoBuf(HistoricalDataProto.HistoricalData historicalData) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void historicalDataUpdateProtoBuf(HistoricalDataUpdateProto.HistoricalDataUpdate historicalDataUpdate) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void historicalDataEndProtoBuf(HistoricalDataEndProto.HistoricalDataEnd historicalDataEnd) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void realTimeBarTickProtoBuf(RealTimeBarTickProto.RealTimeBarTick realTimeBarTick) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void headTimestampProtoBuf(HeadTimestampProto.HeadTimestamp headTimestamp) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void histogramDataProtoBuf(HistogramDataProto.HistogramData histogramData) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void historicalTicksProtoBuf(HistoricalTicksProto.HistoricalTicks historicalTicks) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void historicalTicksBidAskProtoBuf(HistoricalTicksBidAskProto.HistoricalTicksBidAsk historicalTicksBidAsk) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void historicalTicksLastProtoBuf(HistoricalTicksLastProto.HistoricalTicksLast historicalTicksLast) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickByTickDataProtoBuf(TickByTickDataProto.TickByTickData tickByTickData) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void updateNewsBulletinProtoBuf(NewsBulletinProto.NewsBulletin newsBulletin) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void newsArticleProtoBuf(NewsArticleProto.NewsArticle newsArticle) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void newsProvidersProtoBuf(NewsProvidersProto.NewsProviders newsProviders) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void historicalNewsProtoBuf(HistoricalNewsProto.HistoricalNews historicalNews) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void historicalNewsEndProtoBuf(HistoricalNewsEndProto.HistoricalNewsEnd historicalNewsEnd) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void wshMetaDataProtoBuf(WshMetaDataProto.WshMetaData wshMetaData) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void wshEventDataProtoBuf(WshEventDataProto.WshEventData wshEventData) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void tickNewsProtoBuf(TickNewsProto.TickNews tickNews) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void scannerParametersProtoBuf(ScannerParametersProto.ScannerParameters scannerParameters) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void scannerDataProtoBuf(ScannerDataProto.ScannerData scannerData) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void fundamentalsDataProtoBuf(FundamentalsDataProto.FundamentalsData fundamentalsData) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void pnlProtoBuf(PnLProto.PnL pnL) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void pnlSingleProtoBuf(PnLSingleProto.PnLSingle pnLSingle) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void receiveFAProtoBuf(ReceiveFAProto.ReceiveFA receiveFA) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void replaceFAEndProtoBuf(ReplaceFAEndProto.ReplaceFAEnd replaceFAEnd) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void commissionAndFeesReportProtoBuf(CommissionAndFeesReportProto.CommissionAndFeesReport commissionAndFeesReport) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void historicalScheduleProtoBuf(HistoricalScheduleProto.HistoricalSchedule historicalSchedule) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void rerouteMarketDataRequestProtoBuf(RerouteMarketDataRequestProto.RerouteMarketDataRequest rerouteMarketDataRequest) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void rerouteMarketDepthRequestProtoBuf(RerouteMarketDepthRequestProto.RerouteMarketDepthRequest rerouteMarketDepthRequest) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void secDefOptParameterProtoBuf(SecDefOptParameterProto.SecDefOptParameter secDefOptParameter) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void secDefOptParameterEndProtoBuf(SecDefOptParameterEndProto.SecDefOptParameterEnd secDefOptParameterEnd) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void softDollarTiersProtoBuf(SoftDollarTiersProto.SoftDollarTiers softDollarTiers) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void familyCodesProtoBuf(FamilyCodesProto.FamilyCodes familyCodes) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void symbolSamplesProtoBuf(SymbolSamplesProto.SymbolSamples symbolSamples) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void smartComponentsProtoBuf(SmartComponentsProto.SmartComponents smartComponents) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void marketRuleProtoBuf(MarketRuleProto.MarketRule marketRule) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void userInfoProtoBuf(UserInfoProto.UserInfo userInfo) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void nextValidIdProtoBuf(NextValidIdProto.NextValidId nextValidId) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void currentTimeProtoBuf(CurrentTimeProto.CurrentTime currentTime) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void currentTimeInMillisProtoBuf(CurrentTimeInMillisProto.CurrentTimeInMillis currentTimeInMillis) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void verifyMessageApiProtoBuf(VerifyMessageApiProto.VerifyMessageApi verifyMessageApi) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void verifyCompletedProtoBuf(VerifyCompletedProto.VerifyCompleted verifyCompleted) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void displayGroupListProtoBuf(DisplayGroupListProto.DisplayGroupList displayGroupList) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void displayGroupUpdatedProtoBuf(DisplayGroupUpdatedProto.DisplayGroupUpdated displayGroupUpdated) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void marketDepthExchangesProtoBuf(MarketDepthExchangesProto.MarketDepthExchanges marketDepthExchanges) {
        LOGGER.warn("{} has not been setup.", Thread.currentThread().getStackTrace()[1].getMethodName());
    }
}
