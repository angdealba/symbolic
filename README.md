# symbolic

## Table of Contents

## Client
### What the Client Does
Our client is a medical history Background Checker application designed to provide a simple method for institutions to verify key medical data about prospective customers or workers.  The client provides an input to submit an individual's unique patient ID number along with fields for a Required Vaccination, Potential Allergy, and Potential Diagnosis.  This allows for the user of the client to submit up to three simultaneous checks of a patient's health history, allowing for them to filter on each of these categories.  Each separate query is then returned to the client UI as either a POSITIVE or NEGATIVE response value, indicating whether the patient's relevant medical history did in fact match the query or if it did not (for example, a POSITIVE vaccination result means that the patient has received the vaccine that was searched, while a NEGATIVE diagnosis result indicates that they have not been diagnosed with the specified condition).

### How it Uses the Service
On clicking Submit, the client app will send a query to our service which will process the values that the user provided for each of the three possible inputs and will check whether the Patient stored in the database with the specified ID value has received the specified vaccination, has been recorded to have the specified allergy, and whether they have ever been diagnosed with the specified diagnosis condition.  The vaccination and allergy checks occur entirely within the domain of the Patient entity type from the service, while the diagnosis check interacts with the Diagnosis entity as well and leverages the Patient-Diagnosis join table functionality to retrieve any matching Diagnosis objects that have been linked to the specified Patient.

### Target Audience
The simple structure and flexibility of our client allow for it to serve a wide variety of possible target audiences and in many different areas of industry.  One example use of the client is that it could be hosted by a restaurant as a means for customers to check in and confirm that they do not have any critical allergies that the restaurant staff need to be aware of when preparing the customer's food.  The client could also be used by a wide range of businesses to verify their patrons' vaccination status (for example, checking for the COVID-19 vaccine) before allowing the individual entry into the business (similarly, this system could also be used by business to verify the medical background of potential candidates that they are considering hiring).

However, the most critical target use case for our client and the one that provides several key target audience is the ability for this client to be used by healthcare workers in emergency room or urgent care facilities to quickly verify key details about a patient's health history, particularly in the event where a patient is unconscious or unable to communicate effectively with the care providers.  Our intended design of this client within the Symbolic service would be for all patients whose data is registered within Symbolic to be provided with a physical copy of their Patient ID value, for example by having the Patient ID listed on their health insurance card.  This design offers incredible accessibility by accommodating the needs of users who are impaired or are elderly and may be unable to remember the full details of their medical history, as they could simply display the Patient ID value on the card to a healthcare provider, who can then enter this into our Background Checker client to allow them to quickly review the user's medical history before beginning treatment.

### What Can Users Do Better with This Client
As noted above, our client provides useful functionality to a range of possible target users and greatly streamlines the process of checking an individual's medical history for vaccination history and critical allergies or diagnoses by allowing the user to directly query the Symbolic service database for this historical data rather than forcing the patient to manually request medical records from their care provider and transfer them to the business or healthcare facility.  In addition, our approach is privacy-preserving for the patient's sensitive medical data since every user's medical history data is only accessible with knowledge of their unique Patient ID, which is a 36-character string that ultimately encodes a 128-bit universally unique identifier (UUID).  Since there are 2^128 = 3.40 * 10^38 total possible UUIDs, this gives over 10^28 possible UUIDs every person on the planet which is far larger than could be enumerated with a computer in any feasible time frame and therefore the randomized identifier does provide protection against a user's medical history from being exposed.  Additionally, the Background Checker client only retrieves Positive or Negative responses for each of the user inputs, and therefore does not expose any other information from the patient's medical history other than the specifically requested values.

The other main benefit of this client compared to past work in the domain is the ease with which it enables impaired patients to share their critical health data with a healthcare provider if they are unable to verbally communicate it.  The expectation of only sharing a single Patient ID string can be easily performed with a physical data transfer (as in our suggestion of having an insurance card include the Symbolic Patient ID printed on it), and the healthcare provider is able to complete the rest of the tasks on their own by querying any relevant health checks through the client that they need to be aware of before administering care.

### How to Build, Run, and Test the Client
It is important to note that the client requires the Symbolic service to also be running in order to function appropriately.  As a result, the service should be started first in a terminal window by following the instructions [here](#how-to-build-run-and-test-the-service).  The Client can then be started in a separate terminal window with the following steps:
1. Clone the repo to a local machine with git clone https://github.com/angdealba/symbolic.git and **cd** to the project directory
2. Run the command **./mvnw spring-boot:run -Dstart-class=com.symbolic.symbolic.ClientApplication** to build and run the client, which will launch in a new GUI window.

### How a Third Party can Develop Their Own Client
A third party can develop their own client simply by agreeing to the Symbolic service's authentication scheme which requires a client to register with the service by sending a POST request to the **/api/client/register** endpoint with a client ID and password value.  Once the registration has been completed successfully the client can then authenticate with the service by sending a POST request to **/api/client/authenticate** with the same name and password in a JSON object, which will return a valid JSON Web Token on success that grants the client authorization to make requests to the Symbolic service's endpoints.  This JWT token must then be included as a Bearer token in the Authorization header of all future requests sent from the client to the Symbolic service.  The registration process grants the client a standard Spring Security role that grants it access to the read-only GET endpoints in the database along with the additional functionality endpoints such as **/api/bgcheck**.  The endpoints that allow for the creation or deletion of data directly from the database are restricted to accounts with the Spring Security ADMIN_ROLE, which the third party developer would need to request for our team to manually elevate their role level in the production database in order to ensure that their use case does in fact require these permissions and that we can verify their identity to prevent a malicious actor from gaining access to overwrite or delete all medical data in the database.

### Multiple Simultaneous Client Instances
Due to our usage of the Spring Boot library, our service was already designed to accommodate multiple simultaneous client connections and the parallel processing of requests since all of the networking logic for the service is handled automatically by Spring Boot.  Spring Boot also automatically tracks which requests came from each client by maintaining separate HTTP connections to each of them, and the usage of the Spring Security framework for authorization ensures that each individual client must authenticate with the service and will send a JWT token in the Authorization header of all request  We performed a range of manual tests of the simultaneous client instance functionality as described below in the [End-to-End Testing section](#end-to-end-testing), and we also performed additional tests within our System Test suite to ensure that this feature was adequately tested.  The first of these test collections is located [here](https://github.com/angdealba/symbolic/blob/main/postman/manual-collections/Simultaneous%20Test.postman_collection.json) in the **/manual-collections** folder and can be imported into Postman to run a series of parallel tests on the service.  To simulate a real-world scenario of multiple service instances, three client instances were initiated, each automatically selecting an available port. These ports were then assigned to the Postman environment variables port1, port2, and port3, enabling dynamic referencing in Postman's test scripts for requests to different service instances.
Using Postman's 'run manually with 0 delay' feature, tests were executed with zero milliseconds delay, simulating a high-concurrency scenario and testing the service's capability to handle concurrent requests. The 'simultaneous test' suite included a variety of add, modify, and delete operations targeted at different ports, ensuring coverage of various operational scenarios for each service instance.
The successful completion of all test scenarios demonstrated the stability and reliability of the service, both on individual instances and in parallel operation. This robust testing approach was essential to confirm the service's ability to handle concurrent requests in production environments, providing key insights for future development and optimization.


### End-to-End Testing
For our end-to-end testing process we opted to go with a manual testing approach centered on exercising all of the possible combinations of inputs that we could make to the client.  Our client was designed to fail gracefully so that invalid inputs would not crash or cause the connection to the service to be reset, and we enumerated a variety of different scenarios to create an end-to-end testing checklist for ensuring that our client and service are communicating correctly and are returning the correct results.  All of these end-to-end tests require the use of Postman along with manually entering values into the Client GUI and sending requests to the service, with Postman being used to generate new Patient and Diagnosis data objects and provide the Patient ID values that can be passed into the client.  A checklist of tests that we performed is included below, with each numbered step in the list representing a single test flow:
1. 1. Generate a new patient in Postman with a POST request to {{baseUrl}}/api/patient and the JSON body {"vaccinations": "HPV", "allergies": "Pollen", "accommodations": ""}.  Denote the ID of this Patient as **patientID1**.
   2. Generate a new diagnosis in Postman with a POST request to {{baseUrl}}/api/diagnosis and the JSON body {"condition": "Measles", "treatmentInfo": "Antiviral", "date": "2023-10-3"}.  Denote the ID of this Diagnosis as **diagnosisID1**.
   3. Join the Patient and Diagnosis objects together to represent the Patient receiving this Diagnosis by sending a POST request to {{baseUrl}}/api/patient/diagnosis with the JSON body {"patientId": "{{patientId1}}", "diagnosisId": "{{diagnosisId1}}"}
   4. In the Client GUI, enter the following data:  Subject ID: {{patientId1}}, Required Vaccination: "HPV", Potential Allergy: "Dogs", and Potential Diagnoses of Concern: "Measles".
   5. Verify that the values in the Background Check Results section are Vaccination: POSITIVE, Allergy: NEGATIVE, Diagnosis: POSITIVE
2. 1. Update the Client GUI inputs to Required Vaccination: "MMR", Potential Allergy: "Dogs", and Potential Diagnoses of Concern: "Covid-19".
   2. Verify that the values in the Background Check Results section are Vaccination: NEGATIVE, Allergy: NEGATIVE, Diagnosis: NEGATIVE
3. 1. Update the Client GUI inputs to Required Vaccination: "Flu", Potential Allergy: "Dogs", and Potential Diagnoses of Concern: "Measles".
   2. Verify that the values in the Background Check Results section are Vaccination: NEGATIVE, Allergy: NEGATIVE, Diagnosis: POSITIVE
4. 1. Update the Client GUI inputs to Required Vaccination: "Covid-19", Potential Allergy: "Tree Nuts", and Potential Diagnoses of Concern: "Measles".
   2. Verify that the values in the Background Check Results section are Vaccination: NEGATIVE, Allergy: NEGATIVE, Diagnosis: POSITIVE
5. 1. Update the Client GUI inputs to Required Vaccination: "Hepatitis B", Potential Allergy: "Dairy", and Potential Diagnoses of Concern: "Measles".
   2. Verify that the values in the Background Check Results section are Vaccination: NEGATIVE, Allergy: POSITIVE, Diagnosis: POSITIVE
6. 1. Generate a new patient in Postman with a POST request to {{baseUrl}}/api/patient and the JSON body {"vaccinations": "Covid-19", "allergies": "Sunlight", "accommodations": ""}.  Denote the ID of this Patient as **patientID2**.
   2. Generate a new diagnosis in Postman with a POST request to {{baseUrl}}/api/diagnosis and the JSON body {"condition": "Influenza", "treatmentInfo": "In-patient Treatment", "date": "2023-11-12"}.  Denote the ID of this Diagnosis as **diagnosisID2**.
   3. Join the Patient and Diagnosis objects together to represent the Patient receiving this Diagnosis by sending a POST request to {{baseUrl}}/api/patient/diagnosis with the JSON body {"patientId": "{{patientId2}}", "diagnosisId": "{{diagnosisId2}}"}
   4. In the Client GUI, enter the following data:  Subject ID: {{patientId2}}, Required Vaccination: "Covid-19", Potential Allergy: "Sunlight", and Potential Diagnoses of Concern: "Covid-19".
   5. Verify that the values in the Background Check Results section are Vaccination: POSITIVE, Allergy: POSITIVE, Diagnosis: NEGATIVE
7. 1. Update the Client GUI inputs to Required Vaccination: "MMR", Potential Allergy: "Sunlight", and Potential Diagnoses of Concern: "Cancer".
    2. Verify that the values in the Background Check Results section are Vaccination: NEGATIVE, Allergy: POSITIVE, Diagnosis: NEGATIVE
8. 1. Update the Client GUI inputs to Required Vaccination: "HPV", Potential Allergy: "Cats", and Potential Diagnoses of Concern: "Cancer".
   2. Verify that the values in the Background Check Results section are Vaccination: NEGATIVE, Allergy: NEGATIVE, Diagnosis: NEGATIVE
9. 1. Update the Client GUI inputs to Required Vaccination: "Rotavirus", Potential Allergy: "Sunlight", and Potential Diagnoses of Concern: "Influenza".
   2. Verify that the values in the Background Check Results section are Vaccination: NEGATIVE, Allergy: POSITIVE, Diagnosis: POSITIVE
10. 1. Update the Client GUI inputs to enter {{patientID1}} as the Subject ID again, then pass in Required Vaccination: "HPV", Potential Allergy: "Pollen", and Potential Diagnoses of Concern: "Measles".
   2. Verify that the values in the Background Check Results section are Vaccination: POSITIVE, Allergy: POSITIVE, Diagnosis: POSITIVE

We also used our end-to-end testing setup to perform a set of multi-client testing operations to complement our API tests discussed [above](#multiple-simultaneous-client-instances).  We did this by launching two instances of the client in separate terminal windows and inputting separate queries into each before submitting and ensuring that each received the correct results.  This helped to ensure that our service did in fact support multiple instances of our full client implementation running concurrently.  Assume for the purposes of the checklist below that the patient1/patient2 and diagnosis1/diagnosis2 objects created in the above checklist are still present in the database, and let the clients be denoted Client A and Client B.

1. 1. In Client A, provide the GUI inputs Subject ID: {{patientID1}}, Required Vaccination: "HPV", Potential Allergy: "Pollen", and Potential Diagnoses of Concern: "Measles" but do not hit submit.
   2. In Client B, provide the GUI inputs Subject ID: {{patientID2}}, Required Vaccination: "Covid-19", Potential Allergy: "Sunlight", and Potential Diagnoses of Concern: "Covid-19".
   3. Click submit on Client A then immediately after click submit on Client B.
   4. Verify that the values in the Background Check Results section for Client A are Vaccination: POSITIVE, Allergy: POSITIVE, Diagnosis: POSITIVE.
   5. Verify that the values in the Background Check Results section for Client B are Vaccination: POSITIVE, Allergy: POSITIVE, Diagnosis: NEGATIVE.
   6. Repeat by clicking submit first on Client B and then immediately after clicking submit on Client B.  Verify that the values described in steps iv-v. have not changed.
2. 1. In Client A, provide the GUI inputs Subject ID: {{patientID1}}, Required Vaccination: "MMR", Potential Allergy: "Pollen", and Potential Diagnoses of Concern: "Measles" but do not hit submit.
   2. In Client B, provide the GUI inputs Subject ID: {{patientID2}}, Required Vaccination: "Covid-19", Potential Allergy: "Tree Nuts", and Potential Diagnoses of Concern: "Mononucleosis".
   3. Click submit on Client A then immediately after click submit on Client B.
   4. Verify that the values in the Background Check Results section for Client A are Vaccination: NEGATIVE, Allergy: POSITIVE, Diagnosis: POSITIVE.
   5. Verify that the values in the Background Check Results section for Client B are Vaccination: POSITIVE, Allergy: NEGATIVE, Diagnosis: NEGATIVE.
   6. Repeat by clicking submit first on Client B and then immediately after clicking submit on Client B.  Verify that the values described in steps iv-v. have not changed.
31. In Client A, provide the GUI inputs Subject ID: {{patientID1}}, Required Vaccination: "Flu", Potential Allergy: "Dairy", and Potential Diagnoses of Concern: "Mononucleosis" but do not hit submit.
2. In Client B, provide the GUI inputs Subject ID: {{patientID2}}, Required Vaccination: "HPV", Potential Allergy: "Stone Fruits", and Potential Diagnoses of Concern: "Influenza".
3. Click submit on Client A then immediately after click submit on Client B.
4. Verify that the values in the Background Check Results section for Client A are Vaccination: NEGATIVE, Allergy: NEGATIVE, Diagnosis: NEGATIVE.
5. Verify that the values in the Background Check Results section for Client B are Vaccination: NEGATIVE, Allergy: NEGATIVE, Diagnosis: POSITIVE.
6. Repeat by clicking submit first on Client B and then immediately after clicking submit on Client B.  Verify that the values described in steps iv-v. have not changed.


## Continuous Integration
We defined a custom Github Actions continuous integration workflow that builds the service, runs all of the service's unit tests and integration tests, performs a Jacoco coverage analysis of these tests, performs a SpotBugs static analysis of the codebase, and performs a Checkstyle analysis of the code, before launching the service and performing a wide range of scripted System API tests with the Postman command line runner tool Newman.  The Jacoco coverage analysis, Spotbugs static analysis, and Checkstyle analysis reports are all pushed to the workflow run as job reports for viewing and can be accessed by clicking on any of the workflow runs at https://github.com/angdealba/symbolic/actions and viewing the Jobs in the left panel.

The custom workflow configuration script for our CI pipeline can be viewed at https://github.com/angdealba/symbolic/blob/main/.github/workflows/maven.yml.

### Testing
In order to test our service and client, we have written a range of Unit Tests targeting all of the core methods to ensure that all of the data type classes behave as expected and that all of the functionality for setting and modifying values in the database work correctly.  Our tests rely upon the JUnit testing framework and aim to test every function within the service Entity, Controller, and Service classes to ensure that they produce the expected outputs for various input scenarios.  We also aimed to test boundary conditions and exceptional scenarios within the input types for these classes to ensure that all possible inputs are handled appropriately and that our service does not reach an error state under unusual inputs.  Finally, we used the Mockito mocking framework to create mocks the repository lookup methods in our API service methods to simulate the database returning data objects matching a search query so that we are able to unit test the functionality of the Service class methods.  We also introduced a range of internal and external integration tests covering all of the core components of the service and testing the interactions between the Entity types, Controller classes, Repository interfaces, along with the external database interface that handles all of the data stored by the service.

In order to test that our API entry points work correctly, we also performed API System testing using the Postman tool.  By passing a range of different Postman inputs to each of our different API endpoints, we were able to produce an exhaustive set of code-calls-code-over-network system-level tests that exhaustively check every endpoint and force them to produce every possible status code defined within our API.  We relied upon the Postman testing framework for adding scripts to check that the response status codes, error messages, and body values all matched expectations for each of the endpoints in our API and aimed to force every possible error code to ensure that the service was fully covered by API tests.

We also tested the persistent data aspect of the service by terminating the application after saving the data to the database with POST requests and then restarting the service and performing GET requests to ensure that the data has persisted in the database after restart.  The Postman Collections feature also allows for a set of requests to be run multiple times by setting the Iterations field in the Runner > Functional > Run Configuration field to a value greater than 1, so by varying this Iterations value we were able to simulate having multiple distinct clients connect to our service and use the data.

### Unit Tests
All of our unit tests can be run within IntelliJ Idea by setting up a default JUnit configuration under the Run > Edit Configurations menu and running the project in this configuration, or by executing the following command from the root directory:
**./mvnw -B package --file pom.xml -Dstart-class=com.symbolic.symbolic.SymbolicApplication -Dgroups="UnitTest"**
The unit test files can be found in the directory **src/test/java/com.symbolic.symbolic** and are organized into subdirectories matching the layout of the **src/main/java/com.symbolic.symbolic** code directory.  The naming convention for our test files is that the test code for a class **A** in subdirectory **dir** of the **src/main** root will be located in **dir/ATest.java**.  The unit tests for the endpoint Service classes that rely upon Mockito mocks for simulating the database functionality are located in the **src/test/java/com.symbolic.symbolic/service**.

The unit tests are all run automatically in the CI workflow and their output can be viewed in the **build** Job under the **Build and run Unit Tests with Maven** step.  We also implemented a set of separate unit tests for the Client code that run during the **run_live_tests** Job under the **Build and run Client Tests with Maven** step.

### System Tests Corresponding to API
Postman offers an export tool that exports all of the call settings and metadata needed to reproduce a set of calls, and we took advantage of this to produce a complete set of requests for each of our 7 data types, along with all of the joint endpoints that control the creation and deletion of join table entries connecting elements in the database.  The .json file outputs from each of these collections are stored in the **postman/** folder, and each of these .json files can be imported into an instance of Postman on the same machine as the service and run to replicate our test functionality.  Our CI workflow automatically runs all of the tests stored in the **postman/collections/** folder, using the environment variables defined in **postman/environments/**, while the **postman/manual-collections** folder contains an additional set of tests that are set up to run manually by importing the **postman_collection.json** file in Postman.  The results of these runs can be viewed in the **run_live_tests** Job in any workflow action and are located under the **Postman API Tests with Newman** step.

As described [above](#multiple-simultaneous-client-instances) we used Postman as our primary test suite for performing the tests to verify the multiple client instances aspect of our service.  This was done manually using the Newman command line interface to simultaneously run multiple instances of the Postman client connecting to the service.

We also used Postman system tests to manually exercise the persistent data aspects of the service by running a selection of CREATE and PUT requests to populate the database with values, shutting down the service by terminating the Maven run with ^C, and then restarting the service with a new Maven run and performing all of the GET requests to ensure that the data still exists and is unmodified since the shutdown.  We were unable to simulate this process of repeatedly starting and stopping the service between Postman calls within our CI pipeline, and as a result chose to just run it as manual tests.


### Integration Testing - Internal and External
We performed integration testing covering all of the core components of the service and testing the interactions between the Entity types, Controller classes, Repository interfaces, along with the external database interface that handles all of the data stored by the service.  All of our integration tests are denoted with the **@Tag("IntegrationTest")** JUnit flag and can be run by executing the following command from the root directory:
**./mvnw -B package --file pom.xml -Dstart-class=com.symbolic.symbolic.SymbolicApplication -Dgroups="IntegrationTest"**
The integration tests are all run automatically in the CI workflow and their output can be viewed in the **build** Job under the **Build and run Integration Tests with Maven** step.

All of the integration test files can be found in the directory **src/test/java/com.symbolic.symbolic/integration**.  Each integration test file defines all of the integration tests based around a single Entity data type class and its associated Controller class.  Using the example of the Patient Entity and PatientController class, the integration test implementations provide internal integration tests of the interface between the Patient and PatientController by testing all of the locations in which the controller calls Patient's methods or transmits data to it.  In addition, the test suite also provides internal integration tests of the connection between the PatientController and the PatientRepository interface that connects to the service database, differing from the controller unit tests where all of the Repository method calls were mocked.  This setup also provides external integration tests by interacting with the underlying database through an @Autowired JdbcTemplate connection to ensure that values are properly saved and loaded to the database when the appropriate HTTP requests are made to the methods set up in the PatientController.  This process is repeated for all 7 of our core data types, and we also provided additional internal integration tests for the connection between the Controllers and any additional Service classes that they interact with.  For example, the [Medical Practitioner Integration Test](https://github.com/angdealba/symbolic/blob/main/src/test/java/com/symbolic/symbolic/integration/MedicalPractitionerIntegrationTest.java) defines the **testSearch()** method which provides an internal integration test of the interface between the MedicalPractitionerController and MedicalPractitionerService.

### Branch Coverage Tool - Jacoco
For branch coverage, we chose to use the Jacoco coverage library to track the line, method, and branch testing coverage of the entire service.  The Jacoco coverage tool was run automatically in the CI workflow and its output was published directly to the run report using the **jacoco-reporter** Github Action.  The Jacoco reporter results can be viewed at the bottom of the list of Jobs on any CI workflow action run in the Job labeled **COVERAGE_RESULTS_{TIMESTAMP}**.  Note that none of the Jacoco Github Actions integrations were capable of publishing a branch coverage percentage and only publish the line coverage, so we also ran Jacoco locally during testing and reached a final branch coverage percentage of 89% as seen [here](https://github.com/angdealba/symbolic/blob/main/BranchCoverage.png).

### Static Analysis Bug Finder - SpotBugs
We also ran the SpotBugs static analysis bug finder within our CI workflow in order to identify any potentially problematic areas of our source code for us to fix.  SpotBugs was run using Github's spotbugs-maven-plugin which allows for a spotbugs report to be generated automatically within a Maven run using the command **mvn -B package --file pom.xml spotbugs:spotbugs**.  We also relied upon the **spotbugs-github-action** to publish our generated spotbugsXml.xml file to the CI workflow action run for viewing on each pull request or main branch commit.  These static analysis bug finder results can be found in the **spotbugs** Job in any of the CI workflow action runs, and we made an effort to address as many of the warnings raised by SpotBugs as possible in our development.

### Style Checker - Checkstyle
The style checking for the project was using the maven-checkstyle plugin, which allows for Checkstyle to be run on every compilation by specifying the checkstyle option in the command **mvn -B package --file pom.xml checkstyle:checkstyle**.  For the project we chose to use the Google Checks checkstyle guide for the style of all of our code.  We ran Checkstyle on every pull request and main branch commit within our CI workflow and the **checkstyle-github-action** to publish our generated checkstyle-result.xml file to the CI workflow action run.  These reports can be found in the **Checkstyle** Job in any of the CI workflow action runs, and we made an effort to address as many of the style warnings raised by Checkstyle as possible in our development.

### Third Party Code
For our inclusion of third-party code in this project, we relied on the Maven package manager to automatically integrate all of the external packages and also manage our build and testing workflows for the application.  Our Maven configuration is defined at https://github.com/angdealba/symbolic/blob/main/pom.xml and contains every external package and dependency that we included within the service or client.  Our key external dependencies were the Spring/Spring Boot library along with the Spring Security library for the structure of our service, the implementation of its networking logic, and integration with the underlying database.  We also relied on the MySQL database and the Hibernate library for the definition of our data entity types and communication with the database, and we used the JJWT library to manage the JSON Web Tokens that we use to authorize client requests.

Finally, our client was implemented using the JavaFX library to define the GUI, the Apache HTTPComponents library to handle the HTTP requests sent from the client to the service, and Google's GSON parsing library for handling the JSON values returned by the service.

## Service
### How to Build, Run, and Test the Service
Our service depends upon a MySQL database to provide persistent
storage for all of the medical data supplied by client software.
The database should be configured using the following guide:
1. Install MySQL Community Server from this link if it is not already installed: https://dev.mysql.com/downloads/mysql/
2. If on Unix/MacOS, open a Terminal and run **mysql -u root** and input the root account password set up during installation.
   If on Windows, open a Command Prompt, run **cd C:\Program Files\MySQL\My SQL Server 8.0\bin** then **mysql.exe -u root -p** and enter the root password.
3. In the MySQL shell, create a new user that will control the test database with the command
   **CREATE USER 'testuser'@'localhost' IDENTIFIED BY 'password';**
4. Create the database for the project with the command **CREATE DATABASE medical_db;**
5. Give the new user permissions to access and modify this database with the command **GRANT ALL ON medical_db.\* TO 'testuser'@'localhost';**

Now that the database has been configured, our service can either by
cloning the repository and opening it in the IntelliJ Idea IDE or from
the command line using the Maven build tool.
1. Clone the repo to a local machine with **git clone https://github.com/angdealba/symbolic.git**
2. If using IntelliJ, open the project in the IDE and select **Build > Build Project** to complete the build process
3. If using IntelliJ, run the project by selecting **Run > Run 'SymbolicApplication'**.
   If using the command line, **cd**ing into the project directory and running **./mvnw spring-boot:run -Dstart-class=com.symbolic.symbolic.SymbolicApplication** will execute the build and run processes.
4. The service can then be accessed at **https://localhost:8080/api**.
5. Our full unit testing suite can be run with the command **./mvnw test**.


### API Documentation

#### CRUD Endpoints Documentation
Our API provides a large number of endpoints for managing the
Create, Read, Update, Delete (CRUD) lifecycle for all the
primary data types in our service (Patient, Practitioner, Facility,
Insurance, Appointment, Prescription, Diagnosis). These endpoints are
listed below along with information on the request methods (GET, POST,
PUT, DELETE) and the inputs and outputs for each endpoint.

#### Patient Data Endpoints
- /api/patient
    - GET
        - Description: receives a single Patient object from the database
        - Returns a single Patient object and a 200 OK on success, and a 404 Not Found if the Patient with the specified id is not found in the database.  Patient objects contain an integer identifier "id", a string value "vaccinations" listing any vaccinations the patient has received, a string value "allergies" listing any allergies for which the patient has been diagnosed, and a string value "accommodations" representing any accommodations the patient has been approved for.
        - Body fields: a single integer param "id" specifying the UUID number of the Patient to remove
        - Sample body: {"id": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
    - POST
        - Description: adds an entry for a single Patient object to the database
        - Returns the newly created Patient object and a 201 CREATED on success.  Patient objects contain an integer identifier "id", a string value "vaccinations" listing any vaccinations the patient has received, a string value "allergies" listing any allergies for which the patient has been diagnosed, and a string value "accommodations" representing any accommodations the patient has been approved for.
        - Body fields: a JSON object containing the following values
            - vaccinations (type: String)
            - allergies (type: String)
            - accommodations (type: String)
        - Sample body: {"vaccinations": "COVID-19, Flu", "allergies": "Dairy", "accommodations": "Wheelchair Access"}
    - PUT
        - Description: updates an entry for a single Patient object in the database
        - Returns the newly updated Patient object and a 200 OK on success, and a 404 Not Found on error if the Patient with the specified id is not found in the database.  Patient objects contain an integer identifier "id", a string value "vaccinations" listing any vaccinations the patient has received, a string value "allergies" listing any allergies for which the patient has been diagnosed, and a string value "accommodations" representing any accommodations the patient has been approved for.
        - Body fields: an integer param "id" specifying the id number of the Patient to update and a JSON object containing the following values
            - vaccinations (type: String)
            - allergies (type: String)
            - accommodations (type: String)
        - Sample body: {"vaccinations": "COVID-19, Flu", "allergies": "Dairy", "accommodations": "Wheelchair Access"}
    - DELETE
        - Description: removes a single Patient object from the database
        - Returns a 204 No Content if the Patient was successfully removed, and a 404 Not Found if the Patient with the specified id is not in the database.
        - Fields: a single integer param "id" specifying the id number of the Patient to remove
        - Sample body: {"id": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
- /api/patients
    - GET
        - Description: receives the list of all Patient objects in the database
        - Returns a list of Patient objects and a 200 OK on success, and a 204 No Content if there are no Patients in the database.  Patient objects contain an integer identifier "id", a string value "vaccinations" listing any vaccinations the patient has received, a string value "allergies" listing any allergies for which the patient has been diagnosed, and a string value "accommodations" representing any accommodations the patient has been approved for.
        - Fields: None
        - Sample body: N/A
    - DELETE
        - Description: deletes all Patient objects in the database
        - Returns a 204 No Content on success
        - Fields: None
        - Sample body: N/A
- /api/patient/appointments
    - GET
        - Description: retrieves all Appointment objects scheduled for the Patient matching the specified id
        - Returns a list of Appointment objects and a 200 OK on success, and a 404 Not Found if the Patient with the specified id is not found in the database.  Appointment objects contain an integer identifier "id", a date value "dateTime" specifying when the appointment will take place, and an integer value "cost" representing the cost of the appointment.
        - Fields: a single integer param "patientId" specifying the id number of the Patient to search
        - Sample body: {"patientId": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
- /api/appointment/patient
    - GET
        - Description: retrieves the Patient for whom the Appointment with the specified id was scheduled
        - Returns a single Patient object and a 200 OK on success, and a 404 Not Found if the Appointment with the specified id is not found in the database.  Patient objects contain an integer identifier "id", a string value "vaccinations" listing any vaccinations the patient has received, a string value "allergies" listing any allergies for which the patient has been diagnosed, and a string value "accommodations" representing any accommodations the patient has been approved for.
        - Fields: a single integer param "patientId" specifying the id number of the Patient to search
        - Sample body: {"patientId": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
- /api/patient/appointment
    - POST
        - Description: links a Patient object to an Appointment object in the database to indicate that the appointment was scheduled for the patient
        - Returns a single Appointment object and a 200 OK on success, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.  Appointment objects contain an integer identifier "id", a date value "dateTime" specifying when the appointment will take place, and an integer value "cost" representing the cost of the appointment.
        - Fields: an integer param "patientId" specifying the id of the Patient and an integer param "appointmentId" specifying the id of the Appointment
    - DELETE
        - Description: removes a database link between a Patient object and an Appointment object to indicate that the patient cancelled the appointment
        - Returns a 204 No Content if the link was successfully removed, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.
        - Body fields: an integer param "patientId" specifying the id of the Patient and an integer param "appointmentId" specifying the id of the Appointment

#### Practitioner Data Endpoints
- /api/practitioner
    - GET
        - Description: receives a single MedicalPractitioner object from the database
        - Returns a single MedicalPractitioner object and a 200 OK on success, and a 404 Not Found if the MedicalPractitioner with the specified id is not found in the database.  MedicalPractitioner objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the practitioner, a string value "specialization" describing the practitioner's field of specialization, an integer value "consultationCost" for the cost of a consultation appointment with the practitioner, and an integer value "yearsExperience" denoting their number of years working in the field.
        - Fields: a single integer param "id" specifying the id number of the MedicalPractitioner to remove
        - Sample body: {"id": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
    - POST
        - Description: adds an entry for a single MedicalPractitioner object to the database
        - Returns the newly created MedicalPractitioner object and a 201 CREATED on success.  MedicalPractitioner objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the practitioner, a string value "specialization" describing the practitioner's field of specialization, an integer value "consultationCost" for the cost of a consultation appointment with the practitioner, and an integer value "yearsExperience" denoting their number of years working in the field.
        - Fields: a JSON object containing the following values
            - latitude (type: Double)
            - longitude (type: Double)
            - specialization (type: String)
            - consultationCost (type: Integer)
            - yearsExperience (type: Integer)
        - Sample body: {"latitude": 40.7, "longitude": 74.0, "specialization": "Surgery", "consultationCost": 50, "yearsExperience": 10}
    - PUT
        - Description: updates an entry for a single MedicalPractitioner object in the database
        - Returns the newly updated MedicalPractitioner object and a 200 OK on success, and a 404 Not Found on error if the MedicalPractitioner with the specified id is not found in the database.  MedicalPractitioner objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the practitioner, a string value "specialization" describing the practitioner's field of specialization, an integer value "consultationCost" for the cost of a consultation appointment with the practitioner, and an integer value "yearsExperience" denoting their number of years working in the field.
        - Fields: an integer param "id" specifying the id number of the MedicalPractitioner to update and a JSON object containing the following values
            - latitude (type: Double)
            - longitude (type: Double)
            - specialization (type: String)
            - consultationCost (type: Integer)
            - yearsExperience (type: Integer)
        - Sample body: {"latitude": 40.7, "longitude": 74.0, "specialization": "Surgery", "consultationCost": 50, "yearsExperience": 10}
    - DELETE
        - Description: removes a single MedicalPractitioner object from the database
        - Returns a 204 No Content if the MedicalPractitioner was successfully removed, and a 404 Not Found if the MedicalPractitioner with the specified id is not in the database.
        - Fields: a single integer param "id" specifying the id number of the MedicalPractitioner to remove
        - Sample body: {"id": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
- /api/practitioners
    - GET
        - Description: receives the list of all MedicalPractitioner objects in the database
        - Returns a list of MedicalPractitioner objects and a 200 OK on success, and a 204 No Content if there are no MedicalPractitioners in the database.  MedicalPractitioner objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the practitioner, a string value "specialization" describing the practitioner's field of specialization, an integer value "consultationCost" for the cost of a consultation appointment with the practitioner, and an integer value "yearsExperience" denoting their number of years working in the field.
        - Fields: None
        - Sample body: N/A
    - DELETE
        - Description: deletes all MedicalPractitioner objects in the database
        - Returns a 204 No Content on success
        - Fields: None
        - Sample body: N/A
- /api/practitioner/patients
    - GET
        - Description: retrieves all Patient objects that are patients of the MedicalPractitioner matching the specified id
        - Returns a list of Patient objects and a 200 OK on success, and a 404 Not Found if the MedicalPractitioner with the specified id is not found in the database.  Patient objects contain an integer identifier "id", a string value "vaccinations" listing any vaccinations the patient has received, a string value "allergies" listing any allergies for which the patient has been diagnosed, and a string value "accommodations" representing any accommodations the patient has been approved for.
        - Body fields: a single integer param "practitionerId" specifying the id number of the MedicalPractitioner to search
- /api/patient/practitioners
    - GET
        - Description: retrieves all MedicalPractitioner objects visited by the Patient with the specified id
        - Returns a list of MedicalPractitioner object and a 200 OK on success, and a 404 Not Found if the Patient with the specified id is not found in the database.  MedicalPractitioner objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the practitioner, a string value "specialization" describing the practitioner's field of specialization, an integer value "consultationCost" for the cost of a consultation appointment with the practitioner, and an integer value "yearsExperience" denoting their number of years working in the field.
        - Fields: a single integer param "patientId" specifying the id number of the Patient to search
        - Sample body: {"patientId": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
- /api/practitioner/patient
    - POST
        - Description: links a MedicalPractitioner object to a Patient object in the database to indicate that the patient is seeing the practitioner
        - Returns a single Patient object and a 200 OK on success, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.  Patient objects contain an integer identifier "id", a string value "vaccinations" listing any vaccinations the patient has received, a string value "allergies" listing any allergies for which the patient has been diagnosed, and a string value "accommodations" representing any accommodations the patient has been approved for.
        - Body fields: an integer param "practitionerId" specifying the id of the MedicalPractitioner and an integer param "patientId" specifying the id of the Patient
    - DELETE
        - Description: removes a database link between a MedicalPractitioner object and a Patient object to indicate that the patient is no longer seeing the practitioner
        - Returns a 204 No Content if the link was successfully removed, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.
        - Body fields: an integer param "practitionerId" specifying the id of the MedicalPractitioner and an integer param "patientId" specifying the id of the Patient
- /api/practitioner/appointments
    - GET
        - Description: retrieves all Appointment objects scheduled by the MedicalPractitioner matching the specified id
        - Returns a list of Appointment objects and a 200 OK on success, and a 404 Not Found if the MedicalPractitioner with the specified id is not found in the database.  Appointment objects contain an integer identifier "id", a date value "dateTime" specifying when the appointment will take place, and an integer value "cost" representing the cost of the appointment.
        - Fields: a single integer param "practitionerId" specifying the id number of the MedicalPractitioner to search
- /api/appointment/practitioner
    - GET
        - Description: retrieves the MedicalPractitioner who scheduled the Appointment with the specified id
        - Returns a single MedicalPractitioner object and a 200 OK on success, and a 404 Not Found if the Appointment with the specified id is not found in the database.  MedicalPractitioner objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the practitioner, a string value "specialization" describing the practitioner's field of specialization, an integer value "consultationCost" for the cost of a consultation appointment with the practitioner, and an integer value "yearsExperience" denoting their number of years working in the field.
        - Fields: a single integer param "appointmentId" specifying the id number of the Appointment to search
- /api/practitioner/appointment
    - POST
        - Description: links a MedicalPractitioner object to an Appointment object in the database to indicate that the practitioner scheduled the appointment
        - Returns a single Appointment object and a 200 OK on success, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.  Appointment objects contain an integer identifier "id", a date value "dateTime" specifying when the appointment will take place, and an integer value "cost" representing the cost of the appointment.
        - Fields: an integer param "practitionerId" specifying the id of the MedicalPractitioner and an integer param "appointmentId" specifying the id of the Appointment
    - DELETE
        - Description: removes a database link between a MedicalPractitioner object and an Appointment object to indicate that the practitioner cancelled the appointment
        - Returns a 204 No Content if the link was successfully removed, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.
        - Fields: an integer param "practitionerId" specifying the id of the MedicalPractitioner and an integer param "appointmentId" specifying the id of the Appointment
- /api/practitioner/prescriptions
    - GET
        - Description: retrieves all Prescription objects prescribed by the MedicalPractitioner matching the specified id
        - Returns a list of Prescription objects and a 200 OK on success, and a 404 Not Found if the MedicalPractitioner with the specified id is not found in the database.  Prescription objects contain an integer identifier "id", an integer "dosage" representing the number of pills taken at a time, an integer identifier "dailyUses" representing the number of times the medication should be taken daily, an integer "cost" describing the price of the prescription, and a string "instructions" providing any additional info on taking the medication.
        - Fields: a single integer param "practitionerId" specifying the id number of the MedicalPractitioner to search
- /api/prescription/practitioner
    - GET
        - Description: retrieves the MedicalPractitioner who prescribed the Prescription with the specified id
        - Returns a single MedicalPractitioner object and a 200 OK on success, and a 404 Not Found if the Prescription with the specified id is not found in the database.  MedicalPractitioner objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the practitioner, a string value "specialization" describing the practitioner's field of specialization, an integer value "consultationCost" for the cost of a consultation appointment with the practitioner, and an integer value "yearsExperience" denoting their number of years working in the field.
        - Fields: a single integer param "prescriptionId" specifying the id number of the Prescription to search
- /api/practitioner/prescription
    - POST
        - Description: links a MedicalPractitioner object to a Prescription object in the database to indicate that the practitioner prescribed the prescription
        - Returns a single Prescription object and a 200 OK on success, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.  Prescription objects contain an integer identifier "id", an integer "dosage" representing the number of pills taken at a time, an integer identifier "dailyUses" representing the number of times the medication should be taken daily, an integer "cost" describing the price of the prescription, and a string "instructions" providing any additional info on taking the medication.
        - Fields: an integer param "practitionerId" specifying the id of the MedicalPractitioner and an integer param "prescriptionId" specifying the id of the Prescription
    - DELETE
        - Description: removes a database link between a MedicalPractitioner object and a Prescription object to indicate that the practitioner cancelled the prescription
        - Returns a 204 No Content if the link was successfully removed, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.
        - Fields: an integer param "practitionerId" specifying the id of the MedicalPractitioner and an integer param "prescriptionId" specifying the id of the Prescription
- /api/practitioner/diagnoses
    - GET
        - Description: retrieves all Diagnosis objects written by the MedicalPractitioner matching the specified id
        - Returns a list of Diagnosis objects and a 200 OK on success, and a 404 Not Found if the MedicalPractitioner with the specified id is not found in the database.  Diagnosis objects contain an integer identifier "id", a string "condition" specifying the diagnosed condition, a string "treatmentInfo" describing a treatment plan, and a date value "date" denoting when the diagnosis was performed.
        - Fields: a single integer param "practitionerId" specifying the id number of the MedicalPractitioner to search
- /api/diagnosis/practitioner
    - GET
        - Description: retrieves the MedicalPractitioner who wrote the Diagnosis with the specified id
        - Returns a single MedicalPractitioner object and a 200 OK on success, and a 404 Not Found if the Diagnosis with the specified id is not found in the database.  MedicalPractitioner objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the practitioner, a string value "specialization" describing the practitioner's field of specialization, an integer value "consultationCost" for the cost of a consultation appointment with the practitioner, and an integer value "yearsExperience" denoting their number of years working in the field.
        - Fields: a single integer param "diagnosisId" specifying the id number of the Diagnosis to search
- /api/practitioner/diagnosis
    - POST
        - Description: links a MedicalPractitioner object to a Diagnosis object in the database to indicate that the practitioner wrote the diagnosis
        - Returns a single Diagnosis object and a 200 OK on success, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.  Diagnosis objects contain an integer identifier "id", a string "condition" specifying the diagnosed condition, a string "treatmentInfo" describing a treatment plan, and a date value "date" denoting when the diagnosis was performed.
        - Fields: an integer param "practitionerId" specifying the id of the MedicalPractitioner and an integer param "diagnosisId" specifying the id of the Diagnosis
    - DELETE
        - Description: removes a database link between a MedicalPractitioner object and a Diagnosis object to indicate that the practitioner removed the diagnosis
        - Returns a 204 No Content if the link was successfully removed, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.
        - Fields: an integer param "practitionerId" specifying the id of the MedicalPractitioner and an integer param "diagnosisId" specifying the id of the Diagnosis

#### Facility Data Endpoints
- /api/facility
    - GET
        - Description: receives a single Facility object from the database
        - Returns a single Facility object and a 200 OK on success, and a 404 Not Found if the Facility with the specified id is not found in the database.  Facility objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the facility, and a string value "specialization" describing the type of medical services that the facility offers.
        - Fields: a single integer param "id" specifying the id number of the Facility to remove
        - Sample body: {"id": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
    - POST
        - Description: adds an entry for a single Facility object to the database
        - Returns the newly created Facility object and a 201 CREATED on success.  Facility objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the facility, and a string value "specialization" describing the type of medical services that the facility offers.
        - Fields: a JSON object containing the following values
            - latitude (type: Double)
            - longitude (type: Double)
            - specialization (type: String)
        - Sample body: {"latitude": 38.6, "longitude": 75.0, "specialization": "Optometry"}
    - PUT
        - Description: updates an entry for a single Facility object in the database
        - Returns the newly updated Facility object and a 200 OK on success, and a 404 Not Found on error if the Facility with the specified id is not found in the database.  Facility objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the facility, and a string value "specialization" describing the type of medical services that the facility offers.
        - Fields: an integer param "id" specifying the id number of the Facility to update and a JSON object containing the following values
            - latitude (type: Double)
            - longitude (type: Double)
            - specialization (type: String)
        - Sample body: {"latitude": 38.6, "longitude": 75.0, "specialization": "Optometry"}
    - DELETE
        - Description: removes a single Facility object from the database
        - Returns a 204 No Content if the Facility was successfully removed, and a 404 Not Found if the Facility with the specified id is not in the database.
        - Fields: a single integer param "id" specifying the id number of the Facility to remove
        - Sample body: {"id": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
- /api/facilities
    - GET
        - Description: receives the list of all Facility objects in the database
        - Returns a list of Facility objects and a 200 OK on success, and a 204 No Content if there are no Facilities in the database.  Facility objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the facility, and a string value "specialization" describing the type of medical services that the facility offers.
        - Fields: None
        - Sample body: N/A
    - DELETE
        - Description: deletes all Facility objects in the database
        - Returns a 204 No Content on success
        - Fields: None
        - Sample body: N/A
- /api/facility/patients
    - GET
        - Description: retrieves all Patient objects that have attended a Facility matching the specified id
        - Returns a list of Patient objects and a 200 OK on success, and a 404 Not Found if the Facility with the specified id is not found in the database.  Patient objects contain an integer identifier "id", a string value "vaccinations" listing any vaccinations the patient has received, a string value "allergies" listing any allergies for which the patient has been diagnosed, and a string value "accommodations" representing any accommodations the patient has been approved for.
        - Fields: a single integer param "facilityId" specifying the id number of the Facility to search
- /api/patient/facilities
    - GET
        - Description: retrieves all Facility objects visited by the Patient with the specified id
        - Returns a list of Facility object and a 200 OK on success, and a 404 Not Found if the Patient with the specified id is not found in the database.  Facility objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the facility, and a string value "specialization" describing the type of medical services that the facility offers.
        - Fields: a single integer param "patientId" specifying the id number of the Patient to search
        - Sample body: {"patientId": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
- /api/facility/patient
    - POST
        - Description: links a Facility object to a Patient object in the database to indicate that the patient is visiting the facility
        - Returns a single Patient object and a 200 OK on success, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.  Patient objects contain an integer identifier "id", a string value "vaccinations" listing any vaccinations the patient has received, a string value "allergies" listing any allergies for which the patient has been diagnosed, and a string value "accommodations" representing any accommodations the patient has been approved for.
        - Fields: an integer param "facilityId" specifying the id of the Facility and an integer param "patientId" specifying the id of the Patient
    - DELETE
        - Description: removes a database link between a Facility object and a Patient object to indicate that the patient is no longer visiting the facility
        - Returns a 204 No Content if the link was successfully removed, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.
        - Fields: an integer param "facilityId" specifying the id of the Facility and an integer param "patientId" specifying the id of the Patient
- /api/facility/practitioners
    - GET
        - Description: retrieves all MedicalPractitioner objects that work at a Facility matching the specified id
        - Returns a list of MedicalPractitioner objects and a 200 OK on success, and a 404 Not Found if the Facility with the specified id is not found in the database.  MedicalPractitioner objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the practitioner, a string value "specialization" describing the practitioner's field of specialization, an integer value "consultationCost" for the cost of a consultation appointment with the practitioner, and an integer value "yearsExperience" denoting their number of years working in the field.
        - Fields: a single integer param "facilityId" specifying the id number of the Facility to search
- /api/practitioner/facility
    - GET
        - Description: retrieves the Facility that the MedicalPractitioner with the specified id works at
        - Returns a single Facility object and a 200 OK on success, and a 404 Not Found if the MedicalPractitioner with the specified id is not found in the database.  Facility objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the facility, and a string value "specialization" describing the type of medical services that the facility offers.
        - Fields: a single integer param "practitionerId" specifying the id number of the MedicalPractitioner to search
- /api/facility/practitioner
    - POST
        - Description: links a Facility object to a MedicalPractitioner object in the database to indicate that the practitioner works at the facility
        - Returns a single MedicalPractitioner object and a 200 OK on success, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.  MedicalPractitioner objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the practitioner, a string value "specialization" describing the practitioner's field of specialization, an integer value "consultationCost" for the cost of a consultation appointment with the practitioner, and an integer value "yearsExperience" denoting their number of years working in the field.
        - Fields: an integer param "facilityId" specifying the id of the Facility and an integer param "practitionerId" specifying the id of the MedicalPractitioner
    - DELETE
        - Description: removes a database link between a Facility object and a MedicalPractitioner object to indicate that the practitioner no longer works at the facility
        - Returns a 204 No Content if the link was successfully removed, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.
        - Fields: an integer param "facilityId" specifying the id of the Facility and an integer param "practitionerId" specifying the id of the MedicalPractitioner
- /api/facility/appointments
    - GET
        - Description: retrieves all Appointment objects scheduled at a Facility matching the specified id
        - Returns a list of Appointment objects and a 200 OK on success, and a 404 Not Found if the Facility with the specified id is not found in the database.  Appointment objects contain an integer identifier "id", a date value "dateTime" specifying when the appointment will take place, and an integer value "cost" representing the cost of the appointment.
        - Fields: a single integer param "facilityId" specifying the id number of the Facility to search
- /api/appointment/facility
    - GET
        - Description: retrieves the Facility where the Appointment with the specified id was scheduled
        - Returns a single Facility object and a 200 OK on success, and a 404 Not Found if the Appointment with the specified id is not found in the database.  Facility objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the facility, and a string value "specialization" describing the type of medical services that the facility offers.
        - Fields: a single integer param "appointmentId" specifying the id number of the Appointment to search
        - Sample params: ?appointmentId=6
- /api/facility/appointment
    - POST
        - Description: links a Facility object to an Appointment object in the database to indicate that the appointment will take place at the facility
        - Returns a single Appointment object and a 200 OK on success, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.  Appointment objects contain an integer identifier "id", a date value "dateTime" specifying when the appointment will take place, and an integer value "cost" representing the cost of the appointment.
        - Fields: an integer param "facilityId" specifying the id of the Facility and an integer param "appointmentId" specifying the id of the Appointment
    - DELETE
        - Description: removes a database link between a Facility object and an Appointment object to indicate that the appointment was cancelled at the facility
        - Returns a 204 No Content if the link was successfully removed, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.
        - Fields: an integer param "facilityId" specifying the id of the Facility and an integer param "appointmentId" specifying the id of the Appointment

#### Insurance Data Endpoints
- /api/policy
    - GET
        - Description: receives a single InsurancePolicy object from the database
        - Returns a single InsurancePolicy object and a 200 OK on success, and a 404 Not Found if the InsurancePolicy with the specified id is not found in the database.  InsurancePolicy objects contain an integer identifier "id" and an integer "premiumCost" describing the cost of the insurance policy.
        - Fields: a single integer param "id" specifying the id number of the InsurancePolicy to remove
        - Sample body: {"id": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
    - POST
        - Description: adds an entry for a single InsurancePolicy object to the database
        - Returns the newly created InsurancePolicy object and a 201 CREATED on success.  InsurancePolicy objects contain an integer identifier "id" and an integer "premiumCost" describing the cost of the insurance policy.
        - Fields: a JSON object containing the following values
            - premiumCost (type: Integer)
        - Sample body: {"premiumCost": 300}
    - PUT
        - Description: updates an entry for a single InsurancePolicy object in the database
        - Returns the newly updated InsurancePolicy object and a 200 OK on success, and a 404 Not Found on error if the InsurancePolicy with the specified id is not found in the database.  InsurancePolicy objects contain an integer identifier "id" and an integer "premiumCost" describing the cost of the insurance policy.
        - Fields: an integer param "id" specifying the id number of the InsurancePolicy to update and a JSON object containing the following values
            - premiumCost (type: Integer)
        - Sample body: {"premiumCost": 300}
    - DELETE
        - Description: removes a single InsurancePolicy object from the database
        - Returns a 204 No Content if the InsurancePolicy was successfully removed, and a 404 Not Found if the InsurancePolicy with the specified id is not in the database.
        - Fields: a single integer param "id" specifying the id number of the InsurancePolicy to remove
        - Sample body: {"id": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
- /api/policies
    - GET
        - Description: receives the list of all InsurancePolicy objects in the database
        - Returns a list of InsurancePolicy objects and a 200 OK on success, and a 204 No Content if there are no Policies in the database.  InsurancePolicy objects contain an integer identifier "id" and an integer "premiumCost" describing the cost of the insurance policy.
        - Fields: None
        - Sample body: N/A
    - DELETE
        - Description: deletes all InsurancePolicy objects in the database
        - Returns a 204 No Content on success
        - Fields: None
        - Sample body: N/A
- /api/policy/patients
    - GET
        - Description: retrieves all Patient objects that have an InsurancePolicy matching the specified id
        - Returns a list of Patient objects and a 200 OK on success, and a 404 Not Found if the InsurancePolicy with the specified id is not found in the database.  Patient objects contain an integer identifier "id", a string value "vaccinations" listing any vaccinations the patient has received, a string value "allergies" listing any allergies for which the patient has been diagnosed, and a string value "accommodations" representing any accommodations the patient has been approved for.
        - Fields: a single integer param "policyId" specifying the id number of the InsurancePolicy to search
- /api/patient/policy
    - GET
        - Description: retrieves the InsurancePolicy associated with the Patient with the specified id
        - Returns a single InsurancePolicy object and a 200 OK on success, and a 404 Not Found if the Patient with the specified id is not found in the database.  InsurancePolicy objects contain an integer identifier "id" and an integer "premiumCost" describing the cost of the insurance policy.
        - Fields: a single integer param "patientId" specifying the id number of the Patient to search
        - Sample body: {"patientId": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
- /api/policy/patient
    - POST
        - Description: links an InsurancePolicy object to a Patient object in the database to indicate that the patient is covered by the policy
        - Returns a single Patient object and a 200 OK on success, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.  Patient objects contain an integer identifier "id", a string value "vaccinations" listing any vaccinations the patient has received, a string value "allergies" listing any allergies for which the patient has been diagnosed, and a string value "accommodations" representing any accommodations the patient has been approved for.
        - Fields: an integer param "policyId" specifying the id of the InsurancePolicy and an integer param "patientId" specifying the id of the Patient
    - DELETE
        - Description: removes a database link between an InsurancePolicy object and a Patient object to indicate that the patient is no longer covered by the policy
        - Returns a 204 No Content if the link was successfully removed, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.
        - Fields: an integer param "policyId" specifying the id of the InsurancePolicy and an integer param "patientId" specifying the id of the Patient

#### Appointment Data Endpoints
- /api/appointment
    - GET
        - Description: receives a single Appointment object from the database
        - Returns a single Appointment object and a 200 OK on success, and a 404 Not Found if the Appointment with the specified id is not found in the database.  Appointment objects contain an integer identifier "id", a date value "dateTime" specifying when the appointment will take place, and an integer value "cost" representing the cost of the appointment.
        - Fields: a single integer param "id" specifying the id number of the Appointment to remove
        - Sample body: {"id": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
    - POST
        - Description: adds an entry for a single Appointment object to the database
        - Returns the newly created Appointment object and a 201 CREATED on success.  Appointment objects contain an integer identifier "id", a date value "dateTime" specifying when the appointment will take place, and an integer value "cost" representing the cost of the appointment.
        - Fields: a JSON object containing the following values
            - dateTime (type: String in the "yyyy-MM-dd HH:mm" datetime format)
            - cost (type: Integer)
        - Sample body: {"dateTime": "2023-10-25 2:30", "cost": 60}
    - PUT
        - Description: updates an entry for a single Appointment object in the database
        - Returns the newly updated Appointment object and a 200 OK on success, and a 404 Not Found on error if the Appointment with the specified id is not found in the database.  Appointment objects contain an integer identifier "id", a date value "dateTime" specifying when the appointment will take place, and an integer value "cost" representing the cost of the appointment.
        - Fields: an integer param "id" specifying the id number of the Appointment to update and a JSON object containing the following values
            - dateTime (type: String in the "yyyy-MM-dd HH:mm" datetime format)
            - cost (type: Integer)
        - Sample body: {"dateTime": "2023-10-25 2:30", "cost": 60}
    - DELETE
        - Description: removes a single Appointment object from the database
        - Returns a 204 No Content if the Appointment was successfully removed, and a 404 Not Found if the Appointment with the specified id is not in the database.
        - Fields: a single integer param "id" specifying the id number of the Appointment to remove
        - Sample body: {"id": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
- /api/appointments
    - GET
        - Description: receives the list of all Appointment objects in the database
        - Returns a list of Appointment objects and a 200 OK on success, and a 204 No Content if there are no Appointments in the database.  Appointment objects contain an integer identifier "id", a date value "dateTime" specifying when the appointment will take place, and an integer value "cost" representing the cost of the appointment.
        - Fields: None
        - Sample body: N/A
    - DELETE
        - Description: deletes all Appointment objects in the database
        - Returns a 204 No Content on success
        - Fields: None
        - Sample body: N/A

#### Prescription Data Endpoints
- /api/prescription
    - GET
        - Description: receives a single Prescription object from the database
        - Returns a single Prescription object and a 200 OK on success, and a 404 Not Found if the Prescription with the specified id is not found in the database.  Prescription objects contain an integer identifier "id", an integer "dosage" representing the number of pills taken at a time, an integer identifier "dailyUses" representing the number of times the medication should be taken daily, an integer "cost" describing the price of the prescription, and a string "instructions" providing any additional info on taking the medication.
        - Fields: a single integer param "id" specifying the id number of the Prescription to remove
        - Sample body: {"id": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
    - POST
        - Description: adds an entry for a single Prescription object to the database
        - Returns the newly created Prescription object and a 201 CREATED on success.  Prescription objects contain an integer identifier "id", an integer "dosage" representing the number of pills taken at a time, an integer identifier "dailyUses" representing the number of times the medication should be taken daily, an integer "cost" describing the price of the prescription, and a string "instructions" providing any additional info on taking the medication.
        - Fields: a JSON object containing the following values
            - dosage (type: Integer)
            - dailyUses (type: Integer)
            - cost (type: Integer)
            - instructions (type: String)
        - Sample body: {"dosage": 1, "dailyUses": 2, "cost": 150, "instructions": "Take one pill every 12 hours"}
    - PUT
        - Description: updates an entry for a single Prescription object in the database
        - Returns the newly updated Prescription object and a 200 OK on success, and a 404 Not Found on error if the Prescription with the specified id is not found in the database.  Prescription objects contain an integer identifier "id", an integer "dosage" representing the number of pills taken at a time, an integer identifier "dailyUses" representing the number of times the medication should be taken daily, an integer "cost" describing the price of the prescription, and a string "instructions" providing any additional info on taking the medication.
        - Fields: an integer param "id" specifying the id number of the Prescription to update and a JSON object containing the following values
            - dosage (type: Integer)
            - dailyUses (type: Integer)
            - cost (type: Integer)
            - instructions (type: String)
        - Sample body: {"dosage": 1, "dailyUses": 2, "cost": 150, "instructions": "Take one pill every 12 hours"}
    - DELETE
        - Description: removes a single Prescription object from the database
        - Returns a 204 No Content if the Prescription was successfully removed, and a 404 Not Found if the Prescription with the specified id is not in the database.
        - Fields: a single integer param "id" specifying the id number of the Prescription to remove
        - Sample body: {"id": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
- /api/prescriptions
    - GET
        - Description: receives the list of all Prescription objects in the database
        - Returns a list of Prescription objects and a 200 OK on success, and a 204 No Content if there are no Prescriptions in the database.  Prescription objects contain an integer identifier "id", an integer "dosage" representing the number of pills taken at a time, an integer identifier "dailyUses" representing the number of times the medication should be taken daily, an integer "cost" describing the price of the prescription, and a string "instructions" providing any additional info on taking the medication.
        - Fields:
            - None
        - Sample body: N/A
    - DELETE
        - Description: deletes all Prescription objects in the database
        - Returns a 204 No Content on success
        - Fields: None
        - Sample body: N/A

#### Diagnosis Data Endpoints
- /api/diagnosis
    - GET
        - Description: receives a single Diagnosis object from the database
        - Returns a single Diagnosis object and a 200 OK on success, and a 404 Not Found if the Diagnosis with the specified id is not found in the database.  Diagnosis objects contain an integer identifier "id", a string "condition" specifying the diagnosed condition, a string "treatmentInfo" describing a treatment plan, and a date value "date" denoting when the diagnosis was performed.
        - Fields: a single integer param "id" specifying the id number of the Diagnosis to remove
        - Sample body: {"id": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
    - POST
        - Description: adds an entry for a single Diagnosis object to the database
        - Returns the newly created Diagnosis object and a 201 CREATED on success.  Diagnosis objects contain an integer identifier "id", a string "condition" specifying the diagnosed condition, a string "treatmentInfo" describing a treatment plan, and a date value "date" denoting when the diagnosis was performed.
        - Fields: a JSON object containing the following values
            - condition (type: String)
            - treatmentInfo (type: String)
            - date (type: String in the "yyyy-MM-dd" date format)
        - Sample body: {"condition": "COVID-19", "treatmentInfo": "Antiviral Medication", "date": "2023-10-20"}
    - PUT
        - Description: updates an entry for a single Diagnosis object in the database
        - Returns the newly updated Diagnosis object and a 200 OK on success, and a 404 Not Found on error if the Diagnosis with the specified id is not found in the database.  Diagnosis objects contain an integer identifier "id", a string "condition" specifying the diagnosed condition, a string "treatmentInfo" describing a treatment plan, and a date value "date" denoting when the diagnosis was performed.
        - Fields: an integer param "id" specifying the id number of the Diagnosis to update and a JSON object containing the following values
            - condition (type: String)
            - treatmentInfo (type: String)
            - date (type: String in the "yyyy-MM-dd" date format)
        - Sample body: {"condition": "COVID-19", "treatmentInfo": "Antiviral Medication", "date": "2023-10-20"}
    - DELETE
        - Description: removes a single Diagnosis object from the database
        - Returns a 204 No Content if the Diagnosis was successfully removed, and a 404 Not Found if the Diagnosis with the specified id is not in the database.
        - Fields: a single integer param "id" specifying the id number of the Diagnosis to remove
        - Sample body: {"id": "3bec32c8-0e1f-490d-9092-3c7871f4f2e0"}
- /api/diagnoses
    - GET
        - Description: receives the list of all Diagnosis objects in the database
        - Returns a list of Diagnosis objects and a 200 OK on success, and a 204 No Content if there are no Diagnoses in the database.  Diagnosis objects contain an integer identifier "id", a string "condition" specifying the diagnosed condition, a string "treatmentInfo" describing a treatment plan, and a date value "date" denoting when the diagnosis was performed.
        - Fields: None
        - Sample body: N/A
    - DELETE
        - Description: deletes all Diagnosis objects in the database
        - Returns a 204 No Content on success
        - Fields: None
        - Sample body: N/A
        -
#### Historical Data Retrieval Endpoints
- /api/history
    - GET
        - Description: Retrieves a list of Diagnoses that meet the specified search criteria.
        - Returns a list of Diagnoses and 200 OK on success and a 404 Not Found if there are no conditions found by the specified search criteria.
        - Fields: “condition” specifies the medical condition to search for, “start” specifies the start of the time range in which to search for, “end” specifies the end of the time range in which to search for and the optional parameter “location” specifies the geographic region in which to search for.
        - Sample Body: {condition="COVID", start="2023-10-01", end="2023-10-26", location="40.8075, 73.9626"}

- api/history/week
    - GET
        - Description: Retrieves a list of Diagnoses that meet the specified search criteria within the past week
        - Returns a list of Diagnoses and 200 OK on success and a 404 Not Found if there are no conditions found by the specified search criteria.
        - Fields: “condition” specifies the medical condition to search for and the optional parameter “location” specifies the geographic region in which to search for.
        - Sample Params: {"condition": "COVID", "location": "40.8075, 73.9626"}
- api/history/month
    - GET
        - Description: Retrieves a list of Diagnoses that meet the specified search criteria within the past month
        - Returns a list of Diagnoses and 200 OK on success and a 404 Not Found if there are no conditions found by the specified search criteria.
        - Fields: “condition” specifies the medical condition to search for and the optional parameter “location” specifies the geographic region in which to search for.
        - Sample Params: {"condition": "COVID", "location": "40.8075, 73.9626"}
- api/history/year
    - GET
        - Description: Retrieves a list of Diagnoses that meet the specified search criteria within the past year
        - Returns a list of Diagnoses and 200 OK on success and a 404 Not Found if there are no conditions found by the specified search criteria.
        - Fields: “condition” specifies the medical condition to search for and the optional parameter “location” specifies the geographic region in which to search for.
        - Sample Params: {"condition": "COVID", "location": "40.8075, 73.9626"}
- api/history/top-conditions
    - GET
        - Description: Retrieves the most prevalent conditions within the specified search criteria
        - Returns a list of Diagnoses and 200 OK on success and a 404 Not Found if there are no conditions found by the specified search criteria.
        - Fields: “location” specifies the geographic region in which to search for, , “start” specifies the start of the time range in which to search for, “end” specifies the end of the time range in which to search for, and the optional parameter “N” specifies how many conditions to search for
          - Sample Params: {"condition": "COVID", "location": "40.8075, 73.9626"}


#### Background Check Endpoints
- /api/bgcheck
    - GET
        - Description:
        - Returns a JSON object of items indicating whether or not the requested user passes any of the given requirements and a 200 OK on success. Returns an error messaage and 400 Bad Request on missing user ID in request body.
        - Fields: a JSON object containing the following values:
            - id (type: Long)
            - vaccine (type: String, optional)
            - allergy (type: String, optional)
            - diagnosis (type: String, optional)
        - Sample body: {"id": 1002, "vaccine": "flu"}