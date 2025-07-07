# Codesprint 2025 Backend

This is the backend service for the Codesprint 2025 competition, providing various functionalities including AI-powered financial recommendations, transaction analysis, savings goal management, and real-time alerts.

## Getting Started

To run this backend, you'll need:
* Java 21 or higher
* Maven 3.6+
* MongoDB installed and running (default port 27017)

### Setup

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Gabbu98/codesprint-2025-backend && git clone https://github.com/Gabbu98/codesprint-2025-frontend
    cd codesprint-2025-backend
    ```
2.  **Configure `application.yml`:**
    Create `src/main/resources/application.yml` and configure your MongoDB connection and WhatsApp API details.

    ```yaml
    spring:
      data:
        mongodb:
          host: localhost
          port: 27017
          database: codesprint_2025_db # Or your preferred database name

    whatsapp:
      token: YOUR_WHATSAPP_BEARER_TOKEN # Ensure this is a valid, non-expired token
      to:  # Recipient phone number (e.g., your test number)
      accountId:  # Your WhatsApp Business Account ID

    # Important: For local development with Angular, enable CORS on relevant controllers
    # @CrossOrigin(origins = "http://localhost:4200")
    ```
3.  **Build the project:**
    ```bash
    mvn clean install
    ```
4.  **Run the application:**
    ```bash
    mvn spring-boot:run
    ```
    The application will start on `http://localhost:8080` by default.

## API Features

All endpoints are prefixed with `/v0`.

---

### AI Recommendations Controller (`/v0/ai`)

Provides AI-powered financial assistance, recommendations, and conversation capabilities.

| Method | Endpoint Path                 | Description                                                        | Request Body                                  | Response Body                                  |
| :----- | :---------------------------- | :----------------------------------------------------------------- | :-------------------------------------------- | :--------------------------------------------- |
| `GET`  | `/v0/ai/recommendations`      | Get AI-powered spending recommendations.                           | `None`                                        | `SpendingRecommendation`                       |
| `POST` | `/v0/ai/chat`                 | Chat with the financial assistant.                                 | `ChatRequest` (sessionId, message)            | `AIService.ChatbotResponse`                    |
| `GET`  | `/v0/ai/quick-responses`      | Get quick response suggestions for common questions.               | `None`                                        | `List<String>`                                 |
| `GET`  | `/v0/ai/chat/history/{sessionId}` | Get conversation history for a specific session.                   | `None` (Path Variable: `sessionId`)           | `List<AIService.ChatMessage>`                  |
| `DELETE`| `/v0/ai/chat/history/{sessionId}` | Clear conversation history for a specific session.                 | `None` (Path Variable: `sessionId`)           | `Void`                                         |
| `GET`  | `/v0/ai/analyze`              | Get AI analysis for a specific financial topic.                    | `None` (Query Param: `topic` - e.g., `spending`, `savings`, `budget`, `trends`, `categories`, `goals`, or `default`) | `AIService.ChatbotResponse`                    |
| `GET`  | `/v0/ai/advice`               | Get personalized financial advice based on spending patterns.      | `None` (Query Param: `category` - optional, e.g., `groceries`, `rent`, `transport`) | `AIService.ChatbotResponse`                    |

---

### Alerts Controller (`/v0/alerts`)

Manages and retrieves system alerts.

| Method | Endpoint Path | Description                               | Request Body | Response Body |
| :----- | :------------ | :---------------------------------------- | :----------- | :------------ |
| `GET`  | `/v0/alerts`  | Get the single latest alert by timestamp. | `None`       | `AlertModel`  |

---

### Savings Goals Controller (`/v0/savings-goals`)

Manages personal savings goals.

| Method | Endpoint Path                 | Description                             | Request Body                     | Response Body                    |
| :----- | :---------------------------- | :-------------------------------------- | :------------------------------- | :------------------------------- |
| `GET`  | `/v0/savings-goals`           | Gets all saving goals.                  | `None`                           | `List<SavingGoalResponse>`       |
| `POST` | `/v0/savings-goals`           | Creates a new saving goal.              | `CreateSavingGoalRequest`        | `Void` (204 No Content)          |
| `PATCH`| `/v0/savings-goals/{id}`      | Updates the saved amount for an existing saving goal. | `UpdateSavingGoalRequest` (Path Variable: `id`) | `Void` (204 No Content)          |
| `DELETE`| `/v0/savings-goals/{id}`      | Deletes a saving goal.                  | `None` (Path Variable: `id`)     | `Void` (204 No Content)          |

---

### Transactions Controller (`/v0/transactions`)

Provides insights and analysis on user transactions.

| Method | Endpoint Path                  | Description                               | Request Body | Response Body                |
| :----- | :----------------------------- | :---------------------------------------- | :----------- | :--------------------------- |
| `GET`  | `/v0/transactions/spending-percentages` | Get spending percentages by category for a pie chart. | `None`       | `List<TransactionCategoryResponse>` |
| `GET`  | `/v0/transactions/trends`      | Get monthly spending totals (trends).     | `None`       | `List<TransactionCategoryResponse>` |
