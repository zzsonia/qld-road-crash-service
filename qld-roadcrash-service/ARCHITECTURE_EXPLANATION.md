# QLD Road Crash Service - Architecture Explanation

## 🎯 What is This Project?

**QLD Road Crash Service** is a **Spring Boot REST API microservice** that fetches and serves road crash data from Queensland's public dataset. It acts as a middle layer between clients and Queensland's Data API.

---

## 🏢 High-Level Architecture

```
┌─────────────────────────────────────────────────────┐
│                   CLIENT (Browser/App)              │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP Requests
                       ▼
┌─────────────────────────────────────────────────────┐
│         PRESENTATION LAYER (Controller)             │
│  ✓ Handles HTTP requests                            │
│  ✓ Validates input parameters                       │
│  ✓ Returns JSON responses                           │
└──────────────────────┬──────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│         BUSINESS LOGIC LAYER (Service)              │
│  ✓ Processes crash data                             │
│  ✓ Applies filtering & sorting                      │
│  ✓ Manages caching                                  │
└──────────────────────┬──────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│        INTEGRATION LAYER (API Client)               │
│  ✓ Communicates with QLD's API                      │
│  ✓ Handles HTTP calls (WebFlux)                     │
│  ✓ Error handling                                   │
└──────────────────────┬──────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│    EXTERNAL DATA SOURCE (QLD Public API)            │
│  https://www.data.qld.gov.au/api/3/action/...      │
└─────────────────────────────────────────────────────┘
```

---

## 📚 Architectural Layers Explained

### **1. Presentation Layer (Controller)**
**File:** `CrashController.java`

- **Purpose:** Handle incoming HTTP requests from clients
- **Responsibilities:**
  - Map URL endpoints to methods
  - Validate request parameters
  - Call service layer
  - Return responses in a consistent format

**Endpoints Provided:**
```
GET /crashes?page=0&size=100
├─ Fetch paginated crash data
│
GET /crashes/all
├─ Get all cached crash summaries
│
GET /crashes/locations?location=Brisbane
├─ Filter crashes by suburb
│
GET /crashes/sorting?sortBy=severity
├─ Sort crashes by severity or location
```

---

### **2. Service Layer (Business Logic)**
**File:** `CrashService.java`

- **Purpose:** Core business logic and data processing
- **Responsibilities:**
  - Fetch crash data from API Client
  - Transform raw data into usable objects
  - Filter crashes by location
  - Sort crashes by various fields
  - **Manage caching** to improve performance

**Key Methods:**
1. **`fetchAllCrashSummaries()`**
   - Fetches all crash records from QLD API
   - Batches: 100 records per request, up to 5000 total
   - **Cached** - only fetched once, then reused
   - Converts raw data to `CrashSummary` objects

2. **`fetchByfilter(location)`**
   - Filters crashes by suburb name
   - Uses Java Streams for efficient filtering

3. **`fetchBySortedOrder(sortBy)`**
   - Sorts crashes by severity or location
   - Uses Java Streams with custom comparators

---

### **3. API Client Layer (External Integration)**
**File:** `CrashApiClient.java`

- **Purpose:** Communicate with Queensland's public API
- **Responsibilities:**
  - Build API requests with correct parameters
  - Execute HTTP calls
  - Parse responses
  - Handle errors

**Key Details:**
- **Base URL:** `https://www.data.qld.gov.au`
- **Endpoint:** `/api/3/action/datastore_search`
- **Technology:** Spring WebFlux (non-blocking, reactive)
- **Timeout:** 10 seconds per request
- **Query Parameters:** resource_id, limit, offset

**Example API Call:**
```
https://www.data.qld.gov.au/api/3/action/datastore_search?
  resource_id=e88943c0-5968-4972-a15f-38e120d72ec0&
  limit=100&
  offset=0
```

---

### **4. Configuration Layer**
**File:** `WebClientConfig.java`

- **Purpose:** Set up and configure Spring components
- **Provides:** `WebClient` bean for HTTP communication
- Uses: `@Configuration` and `@Bean` annotations

---

### **5. Model Layer (Data Objects)**
**Files:** Model classes using Java Records

| Record | Properties | Purpose |
|--------|-----------|---------|
| `CrashSummary` | type, crashMonth, suburb, severity | Represents a single crash |
| `ApiResponse<T>` | status, message, data | Standard response wrapper |
| `QldResponse` | success, result | Maps QLD API response |
| `Result` | records[], total | Contains crash records |

**Why Records?**
- Immutable data structures
- Less boilerplate code
- Type-safe data transfer

---

### **6. Exception Handling**
**File:** `GlobalExceptionHandler.java`

- **Purpose:** Centralized error handling
- Catches exceptions and returns consistent error responses
- Converts to proper HTTP status codes

**Error Types:**
```
404 Not Found      ← NoSuchElementException (crashes not found)
400 Bad Request    ← IllegalArgumentException (invalid params)
500 Server Error   ← Any other exception
```

---

## 🔄 How a Request Flows Through the System

### **Example: Client requests crashes for Brisbane**

```
1️⃣  CLIENT makes request:
    GET /crashes/locations?location=Brisbane
    │
2️⃣  CONTROLLER receives request
    ├─ Validates: Is "Brisbane" valid? ✓ Yes
    ├─ Calls: crashService.fetchAllCrashSummaries()
    │
3️⃣  SERVICE checks CACHE
    ├─ Is data cached? 
    │  ├─ YES → Return cached data (fast! ⚡)
    │  └─ NO → Call API client
    │
4️⃣  API CLIENT (if cache miss)
    ├─ Builds: HTTP request to QLD API
    ├─ Sends: HTTP GET request
    ├─ Receives: JSON with 100+ crash records
    ├─ Parses: Converts to CrashSummary objects
    ├─ Returns: To Service
    │
5️⃣  SERVICE processes data
    ├─ Filters: Keeps only Brisbane crashes
    ├─ Stores: Result in cache
    ├─ Returns: List to Controller
    │
6️⃣  CONTROLLER wraps response
    ├─ Status: true
    ├─ Message: "Data fetched successfully"
    ├─ Data: Brisbane crashes
    │
7️⃣  Response sent back as JSON
    └─ CLIENT receives data
```

---

## 💡 Key Design Decisions & Why

### **✅ Layered Architecture**
- **Why:** Separation of concerns, easier to test, easier to modify
- **Result:** Each layer has a specific responsibility

### **✅ Caching**
- **Why:** QLD API has rate limits and can be slow
- **Technology:** Spring Cache with `@Cacheable` annotation
- **Result:** Subsequent requests are 100x faster!

### **✅ Non-Blocking HTTP (WebFlux)**
- **Why:** Better performance under high load
- **Result:** Can handle many concurrent users efficiently

### **✅ Exception Handling**
- **Why:** Clients need consistent, clear error messages
- **Result:** All errors return in same JSON format

### **✅ Java Records**
- **Why:** Less code, type-safe, immutable
- **Result:** Clean, modern Java code

### **✅ Dependency Injection**
- **Why:** Loose coupling, easier to test
- **Result:** Components can be easily mocked in tests

---

## 📊 Data Flow Diagram

```
External Data Source
        │
        │ API Response (JSON)
        │ {success: true, result: {records: [...]}}
        ▼
CrashApiClient
        │
        │ QldResponse object
        ▼
CrashService
        │
        ├─ Transform → List<CrashSummary>
        ├─ Cache result
        │
        ├─ Filter (if needed)
        ├─ Sort (if needed)
        │
        ▼
CrashController
        │
        │ Wrap in ApiResponse
        ▼
Client Response
        │
        │ {status: true, message: "...", data: {...}}
        ▼
Client receives JSON
```

---

## 🚀 Technical Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Framework | Spring Boot 4.0.3 | Web framework |
| Language | Java 21 | Latest Java features |
| HTTP Client | Spring WebFlux | Non-blocking requests |
| Caching | Spring Cache | Performance optimization |
| Logging | Log4j2 | Application logging |
| Deployment | Docker | Containerization |

---

## 📝 Configuration Example

```yaml
qld:
  api:
    base-url: https://www.data.qld.gov.au
    path: /api/3/action/datastore_search
    resource-id: e88943c0-5968-4972-a15f-38e120d72ec0  # QLD Crash Dataset ID
```

---

## ✨ Key Features

| Feature | Benefit |
|---------|---------|
| **Pagination** | Handle large datasets efficiently |
| **Filtering** | Find crashes by location |
| **Sorting** | Organize data by severity or location |
| **Caching** | Reduce API calls & improve response time |
| **Error Handling** | Clear, consistent error messages |
| **Validation** | Prevent invalid requests |
| **Non-blocking I/O** | Handle concurrent users efficiently |

---

## 🎓 Design Patterns Used

1. **Layered Architecture** - Separation of concerns
2. **Dependency Injection** - Loose coupling
3. **Factory Pattern** - Bean creation in config
4. **Strategy Pattern** - Different sorting/filtering options
5. **Caching Pattern** - `@Cacheable` annotation

---

## 🔍 Code Structure Summary

```
src/main/java/com/example/qld_roadcrash_service/
├── QldRoadcrashServiceApplication.java    [Entry Point]
├── controller/
│   └── CrashController.java               [HTTP Endpoints]
├── service/
│   └── CrashService.java                  [Business Logic]
├── client/
│   └── CrashApiClient.java                [External API Integration]
├── config/
│   └── WebClientConfig.java               [Spring Configuration]
├── model/
│   ├── ApiResponse.java                   [Response Wrapper]
│   ├── CrashSummary.java                  [Single Crash DTO]
│   ├── QldResponse.java                   [QLD API Response]
│   └── Result.java                        [Result Container]
└── exception/
    └── GlobalExceptionHandler.java        [Error Handling]
```

---

## 🎯 Summary for Interview/Discussion

**"This is a Spring Boot REST microservice that:**
- **Aggregates** road crash data from Queensland's public API
- **Provides** multiple endpoints for querying, filtering, and sorting
- **Optimizes** performance through caching
- **Follows** clean architecture principles with layered design
- **Implements** proper error handling and validation
- **Uses** modern Java features and Spring best practices"

---

## 🤔 Common Questions & Answers

### **Q: Why use layers?**
A: It separates concerns - each layer has one responsibility. Makes code easier to test, modify, and understand.

### **Q: Why caching?**
A: External APIs can be slow and have rate limits. Caching reduces calls and improves response time dramatically.

### **Q: Why WebFlux instead of RestTemplate?**
A: WebFlux is non-blocking and more efficient under high load. It can handle many concurrent requests without creating threads for each.

### **Q: How does error handling work?**
A: All exceptions are caught by `GlobalExceptionHandler`, converted to consistent JSON format with appropriate HTTP status codes.

### **Q: Can this scale?**
A: Yes - caching reduces load, non-blocking I/O handles concurrency, and it's containerized with Docker.

---

## 📚 Further Reading

- **Spring Boot Docs:** https://spring.io/projects/spring-boot
- **Spring WebFlux:** https://spring.io/projects/spring-webflux
- **Java Records:** https://docs.oracle.com/en/java/javase/21/

