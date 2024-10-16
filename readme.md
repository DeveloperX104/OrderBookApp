# Order Book API

This project implements an in-memory order book for placing limit orders with order matching capabilities. It provides APIs to view the order book, submit limit orders, and view recent trades.

## Features

- **Get Order Book**: View the current state of the order book with aggregated bids and asks.
- **Submit Limit Order**: Place buy or sell limit orders with automatic matching.
- **Recent Trades**: Retrieve a list of recent trades executed in the order book.

## Tech
- **Kotlin**
- **Vert.x**
- **Jackson**
- **JUnit 5** 
- **Maven**


## API Endpoints

1. **Get Order Book**
   - **Endpoint**: `/orderbook`
   - **Method**: GET
   - **Headers**: Authorization: Your api key

Response:
```json
{
  "asks": [],
  "bids": []
}
```

2. **Submit Limit Order**
   - **Endpoint**: `/api/orders/limit`
   - **Method**: POST
   - **Headers**: Authorization: Your api key
   - **Request Body**: 
     ```json
     {
       "side": "BUY or SELL",
       "price": "Order price",
       "quantity": "Order quantity",
       "currencyPair": "BTCZAR"
     }
     ```
Response:
```json
{
  "message": "Order placed successfully"
}
```


3. **Recent Trades**
   - **Endpoint**: `/trades/recent`
   - **Headers**: Authorization: Your api key
   - **Method**: GET
 
Response:
```json
[
  {
    "price": "1200000",
    "quantity": "0.05",
    "currencyPair": "BTCZAR",
    "tradedAt": "2024-10-15T17:52:11.342Z",
    "takerSide": "sell",
    "sequenceId": 1295806444729278500,
    "id": "343c6e02-8b1e-11ef-b601-d1eca0747ffd",
    "quoteVolume": "60000.00"
  }
]

 ```


## Authentication

- The API uses a simple API key for authentication. run a uuidgen and update secret to the application.conf


## Running the Application

1. **Build the Project**: 
   ```bash
   ./gradlew build
   ```

2. **Run the Application**: 
   ```bash
   ./gradlew run
   ```

3. **Run Tests**:
   ```bash
   ./gradlew test
   ```

## Author: 
Anjolaiya Oladapo
