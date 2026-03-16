# QLD Road Crash Service API

A Spring Boot application that fetches and processes Queensland road crash data with features like pagination, filtering, sorting, validation, and structured API responses.

**#Features**

- REST API built with Spring Boot  
- Pagination, filtering, and sorting  
- Input validation  
- Standard API response format  
- Exception handling with global error handler  
- Integration with external crash data API  
- Modern Java features (records, streams)  
- Clean layered architecture (Controller → Service → Client)

  **#Tech Stack**
- Java 17
- Spring Boot
- Maven


 ##API Endpoints

**GET /crashes?page=0&size=100**
Fetch crash data with pagination.

*Query Params:*
- `page` (default: 0)
- `size` (default: 100)

*Success Response:*
```json
{
  "status": true,
  "message": "Data fetched successfully",
  "data": { ... }
}

**`GET /crashes/all`**

Fetch crash data and cache selected fields

**Success Response:**
```json
{"status":true,
"message":"Data fetched successfully",
"data":[{"type":"Multi-Vehicle","crashMonth":"May","suburb":"Woolloongabba","severity":"Hospitalisation"}]}

** GET /crashes/locations?location=Redwook **

{"status":true,
"message":"Data fetched successfully",
"data":[{"type":"Multi-Vehicle","crashMonth":"February","suburb":"Morayfield","severity":"Hospitalisation"}]}
