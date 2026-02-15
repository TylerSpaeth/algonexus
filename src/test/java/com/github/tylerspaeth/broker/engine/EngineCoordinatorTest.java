package com.github.tylerspaeth.broker.engine;

import com.github.tylerspaeth.broker.backtester.BacktesterDataFeedService;
import com.github.tylerspaeth.broker.backtester.BacktesterOrderService;
import com.github.tylerspaeth.broker.ib.service.IBAccountService;
import com.github.tylerspaeth.broker.ib.service.IBDataFeedService;
import com.github.tylerspaeth.broker.ib.service.IBOrderService;
import com.github.tylerspaeth.common.data.entity.Symbol;
import com.github.tylerspaeth.engine.EngineCoordinator;
import com.github.tylerspaeth.engine.request.IBDisconnectRequest;
import com.github.tylerspaeth.engine.request.datafeed.SubscribeToDataFeedRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

@ExtendWith({MockitoExtension.class})
public class EngineCoordinatorTest {

    @Mock
    private ExecutorService executorService;

    @Mock
    private IBAccountService ibAccountService;
    @Mock
    private IBDataFeedService ibDataFeedService;
    @Mock
    private IBOrderService ibOrderService;

    @Mock
    private BacktesterDataFeedService backtesterDataFeedService;
    @Mock
    private BacktesterOrderService backtesterOrderService;

    private EngineCoordinator engineCoordinator;

    @BeforeEach
    public void setup() {
        engineCoordinator = new EngineCoordinator(executorService, ibAccountService, ibDataFeedService, ibOrderService, backtesterDataFeedService, backtesterOrderService);
    }

    @Test
    public void testSubmittingNullRequestDoesNothingAndReturnsNothing() throws ExecutionException, InterruptedException {
        Assertions.assertNull(engineCoordinator.submitRequest(null));
    }

    @Test
    public void testSubmittingRequestWithoutStartingEngineWillNotRunRequest() throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                engineCoordinator.submitRequest(new IBDisconnectRequest());
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        Thread.sleep(500);
        thread.interrupt();

        Mockito.verify(executorService, Mockito.times(0)).submit(Mockito.any(Runnable.class));
    }

    @Test
    public void testSubmittingRequestAndThenStartingEngineWillRunRequest() throws InterruptedException {

        Mockito.when(executorService.submit(Mockito.any(Runnable.class))).thenAnswer(invocationOnMock -> {
            IBDisconnectRequest request = invocationOnMock.getArgument(0);
            request.run();
            return request.get();
        });

        Thread requestThread = new Thread(() -> {
            try {
                engineCoordinator.submitRequest(new IBDisconnectRequest());
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        Thread runThread = new Thread(engineCoordinator::run);
        requestThread.start();
        Thread.sleep(100);
        runThread.start();
        Thread.sleep(400);

        Assertions.assertTrue(runThread.isAlive());
        Assertions.assertFalse(requestThread.isAlive());
        Mockito.verify(executorService, Mockito.times(1)).submit(Mockito.any(Runnable.class));
    }

    @Test
    public void testStartingEngineAndThenSubmittingRequestWillRunRequest() throws InterruptedException {

        Mockito.when(executorService.submit(Mockito.any(Runnable.class))).thenAnswer(invocationOnMock -> {
            IBDisconnectRequest request = invocationOnMock.getArgument(0);
            request.run();
            return request.get();
        });

        Thread requestThread = new Thread(() -> {
            try {
                engineCoordinator.submitRequest(new IBDisconnectRequest());
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        Thread runThread = new Thread(engineCoordinator::run);
        runThread.start();
        Thread.sleep(100);
        requestThread.start();
        Thread.sleep(400);

        Assertions.assertTrue(runThread.isAlive());
        Assertions.assertFalse(requestThread.isAlive());
        Mockito.verify(executorService, Mockito.times(1)).submit(Mockito.any(Runnable.class));
    }

    @Test
    public void testSubmittingRequestAfterShuttingDownWillNotRunRequest() throws InterruptedException {

        Thread requestThread = new Thread(() -> {
            try {
                engineCoordinator.submitRequest(new IBDisconnectRequest());
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        Thread runThread = new Thread(engineCoordinator::run);
        Thread stopThread = new Thread(engineCoordinator::stop);
        runThread.start();
        Thread.sleep(100);
        stopThread.start();
        Thread.sleep(300);
        requestThread.start();
        Thread.sleep(100);

        Assertions.assertFalse(runThread.isAlive());
        Assertions.assertFalse(stopThread.isAlive());
        Assertions.assertTrue(requestThread.isAlive());
        Mockito.verify(executorService, Mockito.times(0)).submit(Mockito.any(Runnable.class));
    }

    @Test
    public void testUseBacktesterWillUseBacktestServices() throws InterruptedException {
        engineCoordinator.useBacktester();

        Thread requestThread = new Thread(() -> {
            try {
                engineCoordinator.submitRequest(new SubscribeToDataFeedRequest(new Symbol()));
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        Thread runThread = new Thread(engineCoordinator::run);
        runThread.start();
        Thread.sleep(100);
        requestThread.start();
        Thread.sleep(400);

        Assertions.assertTrue(runThread.isAlive());
        Assertions.assertFalse(requestThread.isAlive());
        Mockito.verify(executorService, Mockito.times(0)).submit(Mockito.any(Runnable.class));
        Mockito.verify(backtesterDataFeedService, Mockito.times(1)).subscribeToDataFeed(Mockito.any(long.class), Mockito.any(Symbol.class));
    }

    @Test
    public void testUseIBWillUseIBServices() throws InterruptedException {
        engineCoordinator.useIB();

        Mockito.when(executorService.submit(Mockito.any(Runnable.class))).thenAnswer(invocationOnMock -> {
            SubscribeToDataFeedRequest request = invocationOnMock.getArgument(0);
            request.run();
            return request.get();
        });

        Thread requestThread = new Thread(() -> {
            try {
                engineCoordinator.submitRequest(new SubscribeToDataFeedRequest(new Symbol()));
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        Thread runThread = new Thread(engineCoordinator::run);
        runThread.start();
        Thread.sleep(100);
        requestThread.start();
        Thread.sleep(400);

        Assertions.assertTrue(runThread.isAlive());
        Assertions.assertFalse(requestThread.isAlive());
        Mockito.verify(executorService, Mockito.times(1)).submit(Mockito.any(Runnable.class));
        Mockito.verify(ibDataFeedService, Mockito.times(1)).subscribeToDataFeed(Mockito.any(long.class), Mockito.any(Symbol.class));
    }
}
