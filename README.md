 # About Repository
 
 This repository is created to share source codes of study, named "Effort Estimation for Architectural Refactoring of Data Tier Software".
 
 # Build Instructions
 
Eclipse Integrated Development Environment (IDE) is used for the development of our tool. src folder contains the source code that is exported as an archive file from this project. The project includes the implementation of the steps 1-3 that are depicted in Figure 2. The tool is a Spring Boot application and it is developed as a Maven project. 
It is necessary to install Maven as well as the Lombok plugin, which is used for generating boilerplate codes automatically required by the application on IDE before importing the project.

PL/SQL is used for implementing the last step (Effort Estimation) of our approach (See Figure 2). The file named "effort_est.sql" contains this implementation and it can be directly executed on an Oracle DBMS. Usage instructions are provided in the following.

# Usage Instructions

1)  Create a database user and required privileges by using following  commands  (Skip  this  step  if  you  have  anexisting one)

    CREATE USER :username IDENTIFIEDBY :password;GRANT CONNECT, RESOURCE, DBATO :username;
    
2)  Connect  database  via  SQL  PLUS  with  :username  and run "dbsetup.sql" to create the necessary table objects for storing analysis results.

3)  Update following properties in application.properties file and Run as Java application the class "DBDependencyAnalyzerApplication".
  
    --spring.datasource.username="?" 
    --spring.datasource.password="?" 
    --spring.datasource.url="?"
  
  Please note that this process can take several hours.
  
4)  Update iRunId and iSchema variables taking place in the file named “dbsetup.sql”. iRunId is a unique ID, which is  automatically  generated  by  the  tool.  It  is  equal  to  1 for the first run. Its value can be retrieved from the table named "DEPENDENCY_RESULT_MAIN" for the sub-sequent runs. iSchema is the name of the schema that is considered to be refactored.

5)  Run the updated "dbsetup.sql" file via SQL PLUS

6)    Predicted effort estimations can be obtained by executing the following SQL query.

      SELECT SCHEMA, EFFORT_1 CASE_a,EFFORT_2 Case_b FROM DEPENDENCY_RESULT_EST WHERE NRUNID=?
 
 
