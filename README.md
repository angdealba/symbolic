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
    - Description: receives a single Patient object from the database
    - Returns a single Patient object and a 200 OK on success, and a 404 Not Found if the Patient with the specified id is not found in the database.  Patient objects contain an integer identifier "id", a string value "vaccinations" listing any vaccinations the patient has received, a string value "allergies" listing any allergies for which the patient has been diagnosed, and a string value "accommodations" representing any accommodations the patient has been approved for.
    - Fields: a single integer param "id" specifying the id number of the Patient to remove
    - Sample params: ?id=4
  - POST
    - Description: adds an entry for a single Patient object to the database
    - Returns the newly created Patient object and a 201 CREATED on success.  Patient objects contain an integer identifier "id", a string value "vaccinations" listing any vaccinations the patient has received, a string value "allergies" listing any allergies for which the patient has been diagnosed, and a string value "accommodations" representing any accommodations the patient has been approved for.
    - Fields: a JSON object containing the following values
      - vaccinations (type: String)
      - allergies (type: String)
      - accommodations (type: String)
    - Sample body: {"vaccinations": "COVID-19, Flu", "allergies": "Dairy", "accommodations": "Wheelchair Access"}
  - PUT
    - Description: updates an entry for a single Patient object in the database
    - Returns the newly updated Patient object and a 200 OK on success, and a 404 Not Found on error if the Patient with the specified id is not found in the database.  Patient objects contain an integer identifier "id", a string value "vaccinations" listing any vaccinations the patient has received, a string value "allergies" listing any allergies for which the patient has been diagnosed, and a string value "accommodations" representing any accommodations the patient has been approved for.
    - Fields: an integer param "id" specifying the id number of the Patient to update and a JSON object containing the following values
      - vaccinations (type: String)
      - allergies (type: String)
      - accommodations (type: String)
    - Sample body: {"vaccinations": "COVID-19, Flu", "allergies": "Dairy", "accommodations": "Wheelchair Access"}
  - DELETE
    - Description: removes a single Patient object from the database
    - Returns a 204 No Content if the Patient was successfully removed, and a 404 Not Found if the Patient with the specified id is not in the database.
    - Fields: a single integer param "id" specifying the id number of the Patient to remove
    - Sample params: ?id=4
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
    - Sample params: ?patientId=6
- /api/appointment/patient
  - GET
    - Description: retrieves the Patient for whom the Appointment with the specified id was scheduled
    - Returns a single Patient object and a 200 OK on success, and a 404 Not Found if the Appointment with the specified id is not found in the database.  Patient objects contain an integer identifier "id", a string value "vaccinations" listing any vaccinations the patient has received, a string value "allergies" listing any allergies for which the patient has been diagnosed, and a string value "accommodations" representing any accommodations the patient has been approved for.
    - Fields: a single integer param "patientId" specifying the id number of the Patient to search
    - Sample params: ?patientId=6
- /api/patient/appointment
  - POST
    - Description: links a Patient object to an Appointment object in the database to indicate that the appointment was scheduled for the patient
    - Returns a single Appointment object and a 200 OK on success, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.  Appointment objects contain an integer identifier "id", a date value "dateTime" specifying when the appointment will take place, and an integer value "cost" representing the cost of the appointment.
    - Fields: an integer param "patientId" specifying the id of the Patient and an integer param "appointmentId" specifying the id of the Appointment
    - Sample params: ?patientId=2&appointmentId=3
  - DELETE
    - Description: removes a database link between a Patient object and an Appointment object to indicate that the patient cancelled the appointment
    - Returns a 204 No Content if the link was successfully removed, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.
    - Fields: an integer param "patientId" specifying the id of the Patient and an integer param "appointmentId" specifying the id of the Appointment
    - Sample params: ?patientId=2&appointmentId=3

#### Practitioner Data Endpoints
- /api/practitioner
  - GET
    - Description: receives a single MedicalPractitioner object from the database
    - Returns a single MedicalPractitioner object and a 200 OK on success, and a 404 Not Found if the MedicalPractitioner with the specified id is not found in the database.  MedicalPractitioner objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the practitioner, a string value "specialization" describing the practitioner's field of specialization, an integer value "consultationCost" for the cost of a consultation appointment with the practitioner, and an integer value "yearsExperience" denoting their number of years working in the field.
    - Fields: a single integer param "id" specifying the id number of the MedicalPractitioner to remove
    - Sample params: ?id=4
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
    - Sample params: ?id=4
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
    - Fields: a single integer param "practitionerId" specifying the id number of the MedicalPractitioner to search
    - Sample params: ?practitionerId=6
- /api/patient/practitioners
  - GET
    - Description: retrieves all MedicalPractitioner objects visited by the Patient with the specified id
    - Returns a list of MedicalPractitioner object and a 200 OK on success, and a 404 Not Found if the Patient with the specified id is not found in the database.  MedicalPractitioner objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the practitioner, a string value "specialization" describing the practitioner's field of specialization, an integer value "consultationCost" for the cost of a consultation appointment with the practitioner, and an integer value "yearsExperience" denoting their number of years working in the field.
    - Fields: a single integer param "patientId" specifying the id number of the Patient to search
    - Sample params: ?patientId=6
- /api/practitioner/patient
  - POST
    - Description: links a MedicalPractitioner object to a Patient object in the database to indicate that the patient is seeing the practitioner
    - Returns a single Patient object and a 200 OK on success, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.  Patient objects contain an integer identifier "id", a string value "vaccinations" listing any vaccinations the patient has received, a string value "allergies" listing any allergies for which the patient has been diagnosed, and a string value "accommodations" representing any accommodations the patient has been approved for.
    - Fields: an integer param "practitionerId" specifying the id of the MedicalPractitioner and an integer param "patientId" specifying the id of the Patient
    - Sample params: ?practitionerId=2&patientId=3
  - DELETE
    - Description: removes a database link between a MedicalPractitioner object and a Patient object to indicate that the patient is no longer seeing the practitioner
    - Returns a 204 No Content if the link was successfully removed, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.
    - Fields: an integer param "practitionerId" specifying the id of the MedicalPractitioner and an integer param "patientId" specifying the id of the Patient
    - Sample params: ?practitionerId=2&patientId=3
- /api/practitioner/appointments
  - GET
    - Description: retrieves all Appointment objects scheduled by the MedicalPractitioner matching the specified id
    - Returns a list of Appointment objects and a 200 OK on success, and a 404 Not Found if the MedicalPractitioner with the specified id is not found in the database.  Appointment objects contain an integer identifier "id", a date value "dateTime" specifying when the appointment will take place, and an integer value "cost" representing the cost of the appointment.
    - Fields: a single integer param "practitionerId" specifying the id number of the MedicalPractitioner to search
    - Sample params: ?practitionerId=6
- /api/appointment/practitioner
  - GET
    - Description: retrieves the MedicalPractitioner who scheduled the Appointment with the specified id
    - Returns a single MedicalPractitioner object and a 200 OK on success, and a 404 Not Found if the Appointment with the specified id is not found in the database.  MedicalPractitioner objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the practitioner, a string value "specialization" describing the practitioner's field of specialization, an integer value "consultationCost" for the cost of a consultation appointment with the practitioner, and an integer value "yearsExperience" denoting their number of years working in the field.
    - Fields: a single integer param "appointmentId" specifying the id number of the Appointment to search
    - Sample params: ?appointmentId=6
- /api/practitioner/appointment
  - POST
    - Description: links a MedicalPractitioner object to an Appointment object in the database to indicate that the practitioner scheduled the appointment
    - Returns a single Appointment object and a 200 OK on success, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.  Appointment objects contain an integer identifier "id", a date value "dateTime" specifying when the appointment will take place, and an integer value "cost" representing the cost of the appointment.
    - Fields: an integer param "practitionerId" specifying the id of the MedicalPractitioner and an integer param "appointmentId" specifying the id of the Appointment
    - Sample params: ?practitionerId=2&appointmentId=3
  - DELETE
    - Description: removes a database link between a MedicalPractitioner object and an Appointment object to indicate that the practitioner cancelled the appointment
    - Returns a 204 No Content if the link was successfully removed, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.
    - Fields: an integer param "practitionerId" specifying the id of the MedicalPractitioner and an integer param "appointmentId" specifying the id of the Appointment
    - Sample params: ?practitionerId=2&appointmentId=3
- /api/practitioner/prescriptions
  - GET
    - Description: retrieves all Prescription objects prescribed by the MedicalPractitioner matching the specified id
    - Returns a list of Prescription objects and a 200 OK on success, and a 404 Not Found if the MedicalPractitioner with the specified id is not found in the database.  Prescription objects contain an integer identifier "id", an integer "dosage" representing the number of pills taken at a time, an integer identifier "dailyUses" representing the number of times the medication should be taken daily, an integer "cost" describing the price of the prescription, and a string "instructions" providing any additional info on taking the medication.
    - Fields: a single integer param "practitionerId" specifying the id number of the MedicalPractitioner to search
    - Sample params: ?practitionerId=6
- /api/prescription/practitioner
  - GET
    - Description: retrieves the MedicalPractitioner who prescribed the Prescription with the specified id
    - Returns a single MedicalPractitioner object and a 200 OK on success, and a 404 Not Found if the Prescription with the specified id is not found in the database.  MedicalPractitioner objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the practitioner, a string value "specialization" describing the practitioner's field of specialization, an integer value "consultationCost" for the cost of a consultation appointment with the practitioner, and an integer value "yearsExperience" denoting their number of years working in the field.
    - Fields: a single integer param "prescriptionId" specifying the id number of the Prescription to search
    - Sample params: ?prescriptionId=6
- /api/practitioner/prescription
  - POST
    - Description: links a MedicalPractitioner object to a Prescription object in the database to indicate that the practitioner prescribed the prescription
    - Returns a single Prescription object and a 200 OK on success, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.  Prescription objects contain an integer identifier "id", an integer "dosage" representing the number of pills taken at a time, an integer identifier "dailyUses" representing the number of times the medication should be taken daily, an integer "cost" describing the price of the prescription, and a string "instructions" providing any additional info on taking the medication.
    - Fields: an integer param "practitionerId" specifying the id of the MedicalPractitioner and an integer param "prescriptionId" specifying the id of the Prescription
    - Sample params: ?practitionerId=2&prescriptionId=3
  - DELETE
    - Description: removes a database link between a MedicalPractitioner object and a Prescription object to indicate that the practitioner cancelled the prescription
    - Returns a 204 No Content if the link was successfully removed, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.
    - Fields: an integer param "practitionerId" specifying the id of the MedicalPractitioner and an integer param "prescriptionId" specifying the id of the Prescription
    - Sample params: ?practitionerId=2&prescriptionId=3
- /api/practitioner/diagnoses
  - GET
    - Description: retrieves all Diagnosis objects written by the MedicalPractitioner matching the specified id
    - Returns a list of Diagnosis objects and a 200 OK on success, and a 404 Not Found if the MedicalPractitioner with the specified id is not found in the database.  Diagnosis objects contain an integer identifier "id", a string "condition" specifying the diagnosed condition, a string "treatmentInfo" describing a treatment plan, and a date value "date" denoting when the diagnosis was performed.
    - Fields: a single integer param "practitionerId" specifying the id number of the MedicalPractitioner to search
    - Sample params: ?practitionerId=6
- /api/diagnosis/practitioner
  - GET
    - Description: retrieves the MedicalPractitioner who wrote the Diagnosis with the specified id
    - Returns a single MedicalPractitioner object and a 200 OK on success, and a 404 Not Found if the Diagnosis with the specified id is not found in the database.  MedicalPractitioner objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the practitioner, a string value "specialization" describing the practitioner's field of specialization, an integer value "consultationCost" for the cost of a consultation appointment with the practitioner, and an integer value "yearsExperience" denoting their number of years working in the field.
    - Fields: a single integer param "diagnosisId" specifying the id number of the Diagnosis to search
    - Sample params: ?diagnosisId=6
- /api/practitioner/diagnosis
  - POST
    - Description: links a MedicalPractitioner object to a Diagnosis object in the database to indicate that the practitioner wrote the diagnosis
    - Returns a single Diagnosis object and a 200 OK on success, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.  Diagnosis objects contain an integer identifier "id", a string "condition" specifying the diagnosed condition, a string "treatmentInfo" describing a treatment plan, and a date value "date" denoting when the diagnosis was performed.
    - Fields: an integer param "practitionerId" specifying the id of the MedicalPractitioner and an integer param "diagnosisId" specifying the id of the Diagnosis
    - Sample params: ?practitionerId=2&diagnosisId=3
  - DELETE
    - Description: removes a database link between a MedicalPractitioner object and a Diagnosis object to indicate that the practitioner removed the diagnosis
    - Returns a 204 No Content if the link was successfully removed, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.
    - Fields: an integer param "practitionerId" specifying the id of the MedicalPractitioner and an integer param "diagnosisId" specifying the id of the Diagnosis
    - Sample params: ?practitionerId=2&diagnosisId=3

#### Facility Data Endpoints
- /api/facility
  - GET
    - Description: receives a single Facility object from the database
    - Returns a single Facility object and a 200 OK on success, and a 404 Not Found if the Facility with the specified id is not found in the database.  Facility objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the facility, and a string value "specialization" describing the type of medical services that the facility offers.
    - Fields: a single integer param "id" specifying the id number of the Facility to remove
    - Sample params: ?id=4
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
    - Sample params: ?id=4
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
    - Sample params: ?facilityId=6
- /api/patient/facilities
  - GET
    - Description: retrieves all Facility objects visited by the Patient with the specified id
    - Returns a list of Facility object and a 200 OK on success, and a 404 Not Found if the Patient with the specified id is not found in the database.  Facility objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the facility, and a string value "specialization" describing the type of medical services that the facility offers.
    - Fields: a single integer param "patientId" specifying the id number of the Patient to search
    - Sample params: ?patientId=6
- /api/facility/patient
  - POST
    - Description: links a Facility object to a Patient object in the database to indicate that the patient is visiting the facility
    - Returns a single Patient object and a 200 OK on success, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.  Patient objects contain an integer identifier "id", a string value "vaccinations" listing any vaccinations the patient has received, a string value "allergies" listing any allergies for which the patient has been diagnosed, and a string value "accommodations" representing any accommodations the patient has been approved for.
    - Fields: an integer param "facilityId" specifying the id of the Facility and an integer param "patientId" specifying the id of the Patient
    - Sample params: ?facilityId=2&patientId=3
  - DELETE
    - Description: removes a database link between a Facility object and a Patient object to indicate that the patient is no longer visiting the facility
    - Returns a 204 No Content if the link was successfully removed, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.
    - Fields: an integer param "facilityId" specifying the id of the Facility and an integer param "patientId" specifying the id of the Patient
    - Sample params: ?facilityId=2&patientId=3
- /api/facility/practitioners
  - GET
    - Description: retrieves all MedicalPractitioner objects that work at a Facility matching the specified id
    - Returns a list of MedicalPractitioner objects and a 200 OK on success, and a 404 Not Found if the Facility with the specified id is not found in the database.  MedicalPractitioner objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the practitioner, a string value "specialization" describing the practitioner's field of specialization, an integer value "consultationCost" for the cost of a consultation appointment with the practitioner, and an integer value "yearsExperience" denoting their number of years working in the field.
    - Fields: a single integer param "facilityId" specifying the id number of the Facility to search
    - Sample params: ?facilityId=6
- /api/practitioner/facility
  - GET
    - Description: retrieves the Facility that the MedicalPractitioner with the specified id works at
    - Returns a single Facility object and a 200 OK on success, and a 404 Not Found if the MedicalPractitioner with the specified id is not found in the database.  Facility objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the facility, and a string value "specialization" describing the type of medical services that the facility offers.
    - Fields: a single integer param "practitionerId" specifying the id number of the MedicalPractitioner to search
    - Sample params: ?practitionerId=6
- /api/facility/practitioner
  - POST
    - Description: links a Facility object to a MedicalPractitioner object in the database to indicate that the practitioner works at the facility
    - Returns a single MedicalPractitioner object and a 200 OK on success, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.  MedicalPractitioner objects contain an integer identifier "id", double values "latitude" and "longitude" representing the geolocation of the practitioner, a string value "specialization" describing the practitioner's field of specialization, an integer value "consultationCost" for the cost of a consultation appointment with the practitioner, and an integer value "yearsExperience" denoting their number of years working in the field.
    - Fields: an integer param "facilityId" specifying the id of the Facility and an integer param "practitionerId" specifying the id of the MedicalPractitioner
    - Sample params: ?facilityId=2&practitionerId=3
  - DELETE
    - Description: removes a database link between a Facility object and a MedicalPractitioner object to indicate that the practitioner no longer works at the facility
    - Returns a 204 No Content if the link was successfully removed, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.
    - Fields: an integer param "facilityId" specifying the id of the Facility and an integer param "practitionerId" specifying the id of the MedicalPractitioner
    - Sample params: ?facilityId=2&practitionerId=3
- /api/facility/appointments
  - GET
    - Description: retrieves all Appointment objects scheduled at a Facility matching the specified id
    - Returns a list of Appointment objects and a 200 OK on success, and a 404 Not Found if the Facility with the specified id is not found in the database.  Appointment objects contain an integer identifier "id", a date value "dateTime" specifying when the appointment will take place, and an integer value "cost" representing the cost of the appointment.
    - Fields: a single integer param "facilityId" specifying the id number of the Facility to search
    - Sample params: ?facilityId=6
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
    - Sample params: ?facilityId=2&appointmentId=3
  - DELETE
    - Description: removes a database link between a Facility object and an Appointment object to indicate that the appointment was cancelled at the facility
    - Returns a 204 No Content if the link was successfully removed, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.
    - Fields: an integer param "facilityId" specifying the id of the Facility and an integer param "appointmentId" specifying the id of the Appointment
    - Sample params: ?facilityId=2&appointmentId=3

#### Insurance Data Endpoints
- /api/policy
  - GET
    - Description: receives a single InsurancePolicy object from the database
    - Returns a single InsurancePolicy object and a 200 OK on success, and a 404 Not Found if the InsurancePolicy with the specified id is not found in the database.  InsurancePolicy objects contain an integer identifier "id" and an integer "premiumCost" describing the cost of the insurance policy.
    - Fields: a single integer param "id" specifying the id number of the InsurancePolicy to remove
    - Sample params: ?id=4
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
    - Sample params: ?id=4
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
    - Sample params: ?policyId=6
- /api/patient/policy
  - GET
    - Description: retrieves the InsurancePolicy associated with the Patient with the specified id
    - Returns a single InsurancePolicy object and a 200 OK on success, and a 404 Not Found if the Patient with the specified id is not found in the database.  InsurancePolicy objects contain an integer identifier "id" and an integer "premiumCost" describing the cost of the insurance policy.
    - Fields: a single integer param "patientId" specifying the id number of the Patient to search
    - Sample params: ?patientId=6
- /api/policy/patient
  - POST
    - Description: links an InsurancePolicy object to a Patient object in the database to indicate that the patient is covered by the policy
    - Returns a single Patient object and a 200 OK on success, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.  Patient objects contain an integer identifier "id", a string value "vaccinations" listing any vaccinations the patient has received, a string value "allergies" listing any allergies for which the patient has been diagnosed, and a string value "accommodations" representing any accommodations the patient has been approved for.
    - Fields: an integer param "policyId" specifying the id of the InsurancePolicy and an integer param "patientId" specifying the id of the Patient
    - Sample params: ?policyId=2&patientId=3
  - DELETE
    - Description: removes a database link between an InsurancePolicy object and a Patient object to indicate that the patient is no longer covered by the policy
    - Returns a 204 No Content if the link was successfully removed, and a 404 Not Found if there was an error or either of the specified objects cannot be found in the database.
    - Fields: an integer param "policyId" specifying the id of the InsurancePolicy and an integer param "patientId" specifying the id of the Patient
    - Sample params: ?policyId=2&patientId=3

#### Appointment Data Endpoints
- /api/appointment
  - GET
    - Description: receives a single Appointment object from the database
    - Returns a single Appointment object and a 200 OK on success, and a 404 Not Found if the Appointment with the specified id is not found in the database.  Appointment objects contain an integer identifier "id", a date value "dateTime" specifying when the appointment will take place, and an integer value "cost" representing the cost of the appointment.
    - Fields: a single integer param "id" specifying the id number of the Appointment to remove
    - Sample params: ?id=4
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
    - Sample params: ?id=4
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
    - Sample params: ?id=4
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
    - Sample params: ?id=4
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
    - Sample params: ?id=4
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
    - Sample params: ?id=4
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
    - Sample Params: ?condition=COVID&start=2023-10-01&end=2023-10-26&location=40.8075, 73.9626

- api/history/week 
  - GET 
    - Description: Retrieves a list of Diagnoses that meet the specified search criteria within the past week 
    - Returns a list of Diagnoses and 200 OK on success and a 404 Not Found if there are no conditions found by the specified search criteria. 
    - Fields: “condition” specifies the medical condition to search for and the optional parameter “location” specifies the geographic region in which to search for. 
    - Sample Params: ?condition=COVID&location=40.8075, 73.9626
- api/history/month
  - GET
    - Description: Retrieves a list of Diagnoses that meet the specified search criteria within the past month
    - Returns a list of Diagnoses and 200 OK on success and a 404 Not Found if there are no conditions found by the specified search criteria.
    - Fields: “condition” specifies the medical condition to search for and the optional parameter “location” specifies the geographic region in which to search for.
    - Sample Params: ?condition=COVID&location=40.8075, 73.9626
- api/history/year
  - GET
    - Description: Retrieves a list of Diagnoses that meet the specified search criteria within the past year
    - Returns a list of Diagnoses and 200 OK on success and a 404 Not Found if there are no conditions found by the specified search criteria.
    - Fields: “condition” specifies the medical condition to search for and the optional parameter “location” specifies the geographic region in which to search for.
    - Sample Params: ?condition=COVID&location=40.8075, 73.9626
- api/history/top-conditions 
  - GET 
    - Description: Retrieves the most prevalent conditions within the specified search criteria 
    - Returns a list of Diagnoses and 200 OK on success and a 404 Not Found if there are no conditions found by the specified search criteria. 
    - Fields: “location” specifies the geographic region in which to search for, , “start” specifies the start of the time range in which to search for, “end” specifies the end of the time range in which to search for, and the optional parameter “N” specifies how many conditions to search for 
    - Sample Params: ? location=40.8075, 73.9626&start=2023-10-01&end=2023-10-26&N=3


## Testing
In order to test our service, we have written a range of Unit Tests targeting all of the core methods to ensure that all of the data type classes behave as expected and that all of the functionality for setting and modifying values in the database work correctly.  Our tests rely upon the JUnit testing framework and aim to test every function within the data classes to ensure that they produce the expected outputs for various input scenarios.  We also aimed to test boundary conditions and exceptional scenarios within the input types for these classes to ensure that all possible inputs are handled appropriately and that our service does not reach an error state under unusual inputs.  Finally, we used the Mockito mocking framework to create mocks the repository lookup methods in our API service methods to simulate the database returning data objects matching a search query so that we are able to unit test the functionality of the Service class methods.

In order to test that our API entry points work correctly, we also performed API System testing using the Postman tool.  By passing a range of different Postman inputs to each of our different API endpoints, we were able to produce an exhaustive set of code-calls-code-over-network system-level tests that exhaustively check every endpoint and force them to produce every possible status code defined within our API.  We also tested the persistent data aspect of the service by terminating the application after saving the data to the database with POST requests and then restarting the service and performing GET requests to ensure that the data has persisted in the database after restart.  The Postman Collections feature also allows for a set of requests to be run multiple times by setting the Iterations field in the Runner > Functional > Run Configuration field to a value greater than 1, so by varying this Iterations value we were able to simulate having multiple distinct clients connect to our service and use the data.

### Unit Tests
All of our unit tests can be run within the IntelliJ Idea by setting up a default JUnit configuration under the Run > Edit Configurations menu and running the project in this configuration, or by executing the **./mvnw test** command from the root directory.  The unit test files can be found in the directory **src/test/java/com.symbolic.symbolic** and are organized into subdirectories matching the layout of the **src/main/java/com.symbolic.symbolic** code directory.  The naming convention for our test files is that the test code for a class **A** in subdirectory **dir** of the **src/main** root will be located in **dir/ATest.java**.  The unit tests for the endpoint Service classes that rely upon Mockito mocks for simulating the database functionality are located in the **src/test/java/com.symbolic.symbolic/service**.

### System Tests Corresponding to API
All of our unit tests can be run within the IntelliJ Idea by setting up a default JUnit configuration under the Run > Edit Configurations menu and running the project in this configuration, or by executing the **./mvnw test** command from the root directory.  The unit test files can be found in the directory **src/test/java/com.symbolic.symbolic** and are organized into subdirectories matching the layout of the **src/main/java/com.symbolic.symbolic** code directory.  The naming convention for our test files is that the test code for a class **A** in subdirectory **dir** of the **src/main** root will be located in **dir/ATest.java**.
Postman offers an export tool that exports all of the call settings and metadata needed to reproduce a set of calls, and we took advantage of this to produce a complete set of requests for each of our 7 data types.  The .json file outputs from each of these collections are stored in the **postman/** folder, and each of these .json files can be imported into an instance of Postman on the same machine as the service and run to replicate our test functionality.

A total of 176 API system tests are included across the two files in the **postman/** folder.  The **Base Requests.postman_collection.json** .json file should be run first in Postman and enumerates all the CRUD API endpoints for each of the data types in the service, while the **Join Requests.postman_collection.json** file should be run second and provides system tests for the join table functionality of linking tables, searching according to the links, and deleting links.

    
## Style Checker
The style checking for the project was done inside IntelliJ Idea using the Checkstyle-IDEA plugin, which allows for Checkstyle to be run as an embedded feature within the API.  For the project we chose to use the Google Checks checkstyle guide for the style of all of our code.  Before submitting the project, Checkstyle-IDEA was run on the entire **src/main/java** sources root directory and Checkstyle found no problems in any of the files contained in the subdirectories.  Although Checkstyle-IDEA does not produce a file output, a screenshot of this Checkstyle result is located at CheckstyleConfirmation.png in the root folder of this repository.