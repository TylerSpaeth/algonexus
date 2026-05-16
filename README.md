# AlgoNexus: Algorithmic Trading Platform

## 📜 Description

AlgoNexus is a high-performance platform for quantitative finance professionals to build, backtest, and execute sophisticated automated trading strategies. It provides an end-to-end solution designed for low latency and extreme concurrency, allowing developers to focus purely on the algorithmic logic of their trades.

## ✨ Key Features

**📈 Strategy & Execution:**
*   Develop complex, customizable algorithms (e.g., arbitrage, mean reversion).
*   Validate strategies using comprehensive backtesting capabilities.
*   Execute validated strategies in a production, live environment.

**🌐 Data Management:**
*   Integrates seamlessly with multiple exchange APIs for real-time data feeds.
*   Features concurrent and thread-safe data queuing for robust data handling.
*   Standardizes diverse incoming market data formats (Data Normalization).

**🛡️ Risk & Order Management:**
*   Comprehensive Order Lifecycle Management (OMS) tracking every order state.
*   Configurable risk controls, including maximum loss limits and position sizing constraints.
*   Maintains a precise, real-time view of current portfolio positions.

**⚙️ Technology:**
*   **Language:** Java 25
*   **Build Tool:** Gradle
*   **Database:** MySql
*   **Market Connectivity:** IB Gateway version 10.41 (Using another version may require updating the TWS jar)

## 🚀 Getting Started

Follow these steps to set up and run the AlgoNexus platform locally:

1.  Set up the database in MySql using the provided script (`src/main/resources/database/algonexus.sql`).
2.  Update the username and password in `persistence.xml` if needed for database connection.
3.  Execute the main application using the Gradle run task:
    ```bash
    ./gradlew run
    ```
4.  At this point all functionality of the application is available except for live trading.
5.  To enable live trading, you will need to launch IB Gateway and connect via a live or simulated account. Then select the "IB Account" option in AlgoNexus to connect for live trading. 

### UI Navigation
AlgoNexus includes a terminal user interface. It can be navigated using the arrow keys to traverse menus and enter to select options. To move between screens in a split screen, use ctrl + arrow keys.

### Managing Strategy Parameters

Before implementing any trading strategy, you must define its operational parameters using the Strategy Manager component. This ensures that all trade decisions adhere to defined risk and liquidity thresholds. Key parameters include:

*   **Risk Tolerance:** Sets the maximum percentage of capital allocated per trade.
*   **Liquidity Filter:** Determines the minimum required trading volume for an asset to be considered viable.
*   **Time Horizon:** Defines the expected duration or cycle length of the strategy's performance measurement (e.g., daily, weekly).

### Data Management Pipeline Usage

The Data Management Pipeline allows you to load, process, and manage historical financial datasets from external sources (e.g., CSV files) into the application's internal dataset structure. This process ensures that all data is properly validated and linked to corresponding ticker symbols.

#### **1. Overview of the Process**

To successfully create a new historical dataset, you must provide several pieces of information:

*   **Dataset Identification:** A unique name for the dataset and its source/origin.
*   **Symbol Mapping:** The required stock or asset ticker symbol (`TICKER`). This links the data to the correct security within our system.
*   **Time Configuration:** Defining the time interval (e.g., 1 minute, 1 hour) and the unit of that interval (e.g., `MINUTE`, `HOUR`).
*   **Data Source Specification:** The location (`File Path`) of the raw data file and metadata about its structure.

#### **2. Step-by-Step Guide to Dataset Creation**

Follow these steps when initiating a new dataset load:

1.  **Locate Data File:** Ensure your external historical data is saved in a compatible format (e.g., CSV).
2.  **Gather Metadata:** Before running the process, you must gather the file's specific metadata, including:
    *   `Source File Location`: The absolute path to the raw data file on the system.
    *   `Ticker Symbol`: The ticker symbol corresponding to the data (e.g., `AAPL`).
    *   `Time Interval & Unit`: How often candles are recorded and what unit of time is used (e.g., 60 seconds).
    *   **Column Ordering:** Specify the exact order of columns in your CSV file (`sourceFileColumnOrder`).
    *   **Metadata Rows Count:** Determine how many initial rows contain dataset metadata rather than actual data points (`sourceFileMetadataRows`).
    *   **Date Format:** Provide a standard date format string (e.g., `yyyy-MM-dd`) used in the file for date parsing.

3.  **Execute Data Loading:** Utilize the data manager UI to create and manage datasets.

**Troubleshooting Tip:** If the system fails during dataset creation, verify that all parameters, especially the file path, date format, and column order, match the actual structure of your source file exactly.

## 🚀 Developing New Strategies

The core trading logic of AlgoNexus resides within custom strategies. To implement a new strategy, you must follow these steps:

1.  **Create the Class:** Create a new Java class that implements your strategy logic. This class must extend `AbstractStrategy`.
2.  **Define the Strategy:** Use the `@Strategy(version = N)` annotation at the top of your file. The version number helps manage compatibility and updates for your trading logic.
3.  **Parameterization (Optional):** If your strategy requires configurable inputs (e.g., lookback period, percentage thresholds), use the `@StrategyParameter` annotation on private fields within your class. These parameters will be automatically injected when the strategy is initialized.
4.  **Implement `onRun()`:** This method contains the main execution loop of your trading logic. In this method:
    *   You retrieve necessary market data (e.g., via `SymbolDAO` or data feed requests).
    *   You process the data according to your defined rules.
    *   You place orders using the provided engine methods (`PlaceOrderRequest`, etc.).
5.  **Implement `onStop()`:** Use this method for cleanup tasks, such as canceling open orders when the strategy is disabled or shut down.

### Example Workflow (Conceptual)

A typical flow inside `onRun()` involves:

1.  Subscribing to a data feed (`submitEngineRequest(subscribeToDataFeedRequest)`).
2.  Reading recent candlesticks (`readFromDataFeedRequest`).
3.  Iterating through the read data and applying logic (e.g., checking for breakout conditions or moving averages).
4.  If conditions are met, constructing an `Order` object and submitting it to the trading engine via `PlaceOrderRequest`.

By adhering to this structure, you ensure that your custom strategies integrate seamlessly with the AlgoNexus execution engine.

## ⚙️ Managing Strategy Parameters in the UI

When developing a new strategy and using the `@StrategyParameter` annotation (as shown in the development guide), these required parameters must be configured through the user interface before running a backtest or live trade.

1.  **Select Strategy:** Navigate to the trading view and select your desired custom strategy from the available list.
2.  **View Parameters Panel:** A dedicated 'Strategy Parameters' panel will appear, populated automatically based on the fields marked with `@StrategyParameter` in your Java code.
3.  **Configure Values:** Each parameter (e.g., `breakoutRange`, `trailPercentage`) will have an editable input field. Input the required values for your specific trading scenario. The system provides type hints and basic validation to assist you.
4.  **Execute:** Once all parameters are correctly set, initiate the backtest or live connection. The engine will automatically inject these user-defined parameter values into your strategy's initialization process (`StrategyParameterSet`), allowing your `onRun()` logic to utilize them.
