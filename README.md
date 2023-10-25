# symbolic

## How to Build, Run, and Test the Service
Our service depends upon a MySQL database to provide persistent
storage for all of the medical data supplied by client software.
The database should be configured using the following guide:
1. Install MySQL Community Server from this link if it is not already installed: https://dev.mysql.com/downloads/mysql/
2. If on Unix/MacOS, open a Terminal and run **mysql -u root** and input the root account password set up during installation.
If on Windows, open a Command Prompt, run **cd C:\Program Files\MySQL\My SQL Server 8.0\bin** then **mysql.exe -u root -p** and enter the root password.
3. In the MySQL shell, create a new user that will control the test database with the command
**CREATE USER 'testuser'@'localhost' IDENTIFIED BY 'password';**
4. Create the database for the project with the command **CREATE DATABASE medical_db;**
5. Give the new user permissions to access and modify this database with the command **GRANT ALL ON medical_db.* TO 'testuser'@'localhost';**

Now that the database has been configured, our service can either by
cloning the repository and opening it in the IntelliJ Idea IDE or from
the command line using the Maven build tool.
1. Clone the repo to a local machine with **git clone https://github.com/angdealba/symbolic.git**
2. If using IntelliJ, open the project in the IDE and select **Build > Build Project** to complete the build process
3. If using IntelliJ, run the project by selecting **Run > Run 'SymbolicApplication'**.
If using the command line, **cd**ing into the project directory and running **./mvnw spring-boot:run** will execute the build and run processes.
4. The service can then be accessed at **https://localhost:8080/api**.
5. Our full unit testing suite can be run with the command **./mvnw test**.


## API Documentation
### Key Endpoints
- /api/search/practitioner
- /api/search/facility
- /api/history
- /api/checker

### CRUD Data Endpoints
Our API provides a large number of endpoints for managing the
Create, Read, Update, Delete (CRUD) lifecycle for all the
primary data types in our service (Patient, Practitioner, Facility,
Insurance, Appointment, Prescription, Diagnosis). These endpoints are
listed below along with information on the request methods (GET, POST,
PUT, DELETE) and the inputs and outputs for each endpoint.

#### Patient Data Endpoints
- /api/patient
  - GET
    - A
  - POST
    - A
  - PUT
    - A
  - DELETE
    - A
- /api/patients
  - GET
    - A
  - DELETE
    - A
- /api/patient/appointments
  - GET
    - A
- /api/appointment/patient
  - GET
    - A
- /api/patient/appointment
  - POST
    - A
  - DELETE
    - A

#### Practitioner Data Endpoints
- /api/practitioner
  - GET
    - A
  - POST
    - A
  - PUT
    - A
  - DELETE
    - A
- /api/practitioners
  - GET
    - A
  - DELETE
    - A
- /api/practitioner/patients
  - GET
    - A
- /api/patient/practitioners
  - GET
    - A
- /api/practitioner/patient
  - POST
    - A
  - DELETE
    - A
- /api/practitioner/appointments
  - GET
    - A
- /api/appointment/practitioner
  - GET
    - A
- /api/practitioner/appointment
  - POST
    - A
  - DELETE
    - A
- /api/practitioner/prescriptions
  - GET
    - A
- /api/prescription/practitioner
  - GET
    - A
- /api/practitioner/prescription
  - POST
    - A
  - DELETE
    - A
- /api/practitioner/diagnoses
  - GET
    - A
- /api/diagnosis/practitioner
  - GET
    - A
- /api/practitioner/diagnosis
  - POST
    - A
  - DELETE
    - A

#### Facility Data Endpoints
- /api/facility
  - GET
    - A
  - POST
    - A
  - PUT
    - A
  - DELETE
    - A
- /api/facilities
  - GET
    - A
  - DELETE
    - A
- /api/facility/patients
  - GET
    - A
- /api/patient/facilities
  - GET
    - A
- /api/facility/patient
  - POST
    - A
  - DELETE
    - A
- /api/facility/practitioners
  - GET
    - A
- /api/practitioner/facility
  - GET
    - A
- /api/facility/practitioner
  - POST
    - A
  - DELETE
    - A
- /api/facility/appointments
  - GET
    - A
- /api/appointment/facility
  - GET
    - A
- /api/facility/appointment
  - POST
    - A
  - DELETE
    - A

#### Insurance Data Endpoints
- /api/policy
  - GET
    - A
  - POST
    - A
  - PUT
    - A
  - DELETE
    - A
- /api/policies
  - GET
    - A
  - DELETE
    - A
- /api/policy/patients
  - GET
    - A
- /api/patient/policies
  - GET
    - A
- /api/policy/patient
  - POST
    - A
  - DELETE
    - A

#### Appointment Data Endpoints
- /api/appointment
  - GET
    - A
  - POST
    - A
  - PUT
    - A
  - DELETE
    - A
- /api/appointments
  - GET
    - A
  - DELETE
    - A

#### Prescription Data Endpoints
- /api/prescription
  - GET
    - A
  - POST
    - A
  - PUT
    - A
  - DELETE
    - A
- /api/prescriptions
  - GET
    - A
  - DELETE
    - A

#### Diagnosis Data Endpoints
- /api/diagnosis
  - GET
    - A
  - POST
    - A
  - PUT
    - A
  - DELETE
    - A
- /api/diagnoses
  - GET
    - A
  - DELETE
    - A

## Testing

### Unit Tests

### System Tests Corresponding to API

    
## Style Checker