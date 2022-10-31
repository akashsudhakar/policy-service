# Policy-Service
This spring boot based project deals with all policy operations.

# Features
Available REST APIs
- ##### <div id="create_policy_api"/>Create Policy API
  This is the endpoint invoked by client applications to create policy
    - *Method*        : `POST`
    - *URL*           : `<base_url>/policy/create`
    - *Content-Type*  : `application/json`
    - *Request-Body*  : `Valid JSON compliant with the below model`
    - ###### <div id="create_policy_model"/> Create-Policy-Model
     ```
     {
         "startDate"         : "Future date in the format dd.MM.yyyy, mandatory field",
         "insuredPersons"    : "List of InsuredPerson part of policy, Structure provided below"
     }
        
     InsuredPerson : {
         "firstName"  : "First Name, mandatory field",
         "secondName" : "Second Name, mandatory field",
         "premium"    : "Premium Amount, mandatory field"
     }
     ```
- *Response*      : `Create Policy Response`

    ```
    {
        "policyId"       : "Unique Policy ID",
        "startDate"      : "Start date passed in request in the format dd.MM.yyyy",
        "insuredPersons" : "List of InsuredPerson part of policy, Structure provided below",
        "totalPremium"   : "Sum of all premium amounts"
    }
  
    InsuredPerson : {
         "id"         : "Unique Person Id",
         "firstName"  : "First Name",
         "secondName" : "Second Name",
         "premium"    : "Premium Amount"
     }
    ```

- ##### <div id="modify_policy_api"/>Modify Policy API
  This is the endpoint invoked by client applications to modify policy
    - *Method*        : `POST`
    - *URL*           : `<base_url>/policy/modify`
    - *Content-Type*  : `application/json`
    - *Request-Body*  : `Valid JSON compliant with the below model`
    - ###### <div id="modify_policy_model"/> Modify-Policy-Model
     ```
     {
         "policyId"          : "Policy ID to modify",
         "effectiveDate"     : "Future date in the format dd.MM.yyyy, mandatory field",
         "insuredPersons"    : "List of InsuredPerson part of policy, Structure provided below"
     }
        
     InsuredPerson : {
         "id"         : "Unique Person Id",
         "firstName"  : "First Name, mandatory field",
         "secondName" : "Second Name, mandatory field",
         "premium"    : "Premium Amount, mandatory field"
     }
     ```
- *Response*      : `Modify Policy Response`

    ```
    {
        "policyId"       : "Modified Policy ID",
        "effectiveDate"  : "Effective date passed in request in the format dd.MM.yyyy",
        "insuredPersons" : "List of InsuredPerson part of policy, Structure provided below",
        "totalPremium"   : "Sum of all premium amounts"
    }
  
    InsuredPerson : {
         "id"         : "Unique Person Id"
         "firstName"  : "First Name",
         "secondName" : "Second Name",
         "premium"    : "Premium Amount"
     }
    ```

- ##### <div id="fetch_policy_api"/>Fetch Policy API
  This is the endpoint invoked by client applications to fetch policy
    - *Method*        : `POST`
    - *URL*           : `<base_url>/policy/fetch`
    - *Content-Type*  : `application/json`
    - *Request-Body*  : `Valid JSON compliant with the below model`
    - ###### <div id="fetch_policy_model"/> Fetch-Policy-Model
     ```
     {
         "policyId"          : "Policy ID to fetch",
         "requestDate"       : "Request date in the format dd.MM.yyyy, not mandatory",
     }
     ```
- *Response*      : `Fetch Policy Response`

    ```
    {
        "policyId"       : "Modified Policy ID",
        "requestDate"    : "Request date passed in request in the format dd.MM.yyyy, else current date",
        "insuredPersons" : "List of InsuredPerson part of policy, Structure provided below",
        "totalPremium"   : "Sum of all premium amounts"
    }
  
    InsuredPerson : {
         "id"         : "Unique Person Id"
         "firstName"  : "First Name",
         "secondName" : "Second Name",
         "premium"    : "Premium Amount"
     }
    ```

- ##### <div id="error_response"/>Error Response
  Common Error Response Structure
  
  - ###### <div id="error_response_model"/> Error-Response-Model
     ```
     {
         "timestamp"     : "Exception time",
         "message"       : "Error message",
         "details"       : "More details"
     }
     ```
    For Policy Id not found in system,  
  ```Returned Http Response Code - 404```

## Building the project
```gradlew clean build```

Runnable artifact is generated under $PROJECT_HOME/build/libs

Artifact - ```policy-service-$version.jar```

Current version - ```1.0.0```

## Running the application
### Pre-requisites
Java needs to be installed in the running environment.
To run, please execute ```java -jar policy-service-$version.jar```

## Code Formatting
To apply formatting ```gradlew goJF``` 

To validate whether formatting applied ```gradlew verGJF```

## Code coverage
Code coverage report can be found at ```$PROJECT_HOME/build/reports/jacoco/test/html/index.html```

This will be generated during project build.

## Test report
Unit Test report can be found at ```$PROJECT_HOME/build/reports/tests/test/index.html```

## In-Memory Database
This project uses in-memory H2 database. Authentication details are provided in application property file.

```spring:
        datasource:
            url: jdbc:h2:mem:embeadb
            username: embea
            password: password
```

Console is enabled by default. To view data, please navigate to [H2-console](http://localhost:8080/h2-console) while application is running

## Data Model
This project stores data in 3 different tables.
```
POLICY
    policy_id STRING
    total_premium DOUBLE
```
```
PERSON
    person_id LONG
    first_name STRING
    last_name STRING
```
```
POLICY_MAPPING
    id LONG
    policy_id STRING
    person_id LONG
```

## Documentation
Swagger UI - [Swagger-UI](http://localhost:8080/swagger-ui.html)

API Docs - [API-Docs](http://localhost:8080/v3/api-docs/)