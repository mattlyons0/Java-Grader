![Logo](https://github.com/mattlyons0/Java-Grader/blob/DropboxGrader/src/Resources/iconBlue64.png?raw=true)
Java-Grader
===========

Fully Fledged Computer Science Homework Grading System designed to store files in Dropbox in order to remain serverless. Supporting Gradebook Reports, Emailing Grade Change Events, Assignments with Libraries, JUnit Tests, Simple Unit Tests and more.

### Contents
* [Disclaimer](#disclaimer)
* [Purpose](#purpose)
* [Screenshots](#screenshots)
* [Features](#features)
* [Intended Use](#intended-use)
* [Usage / Building](#usage--building)
* [To-Do](#to-do)

### Disclaimer
This project was developed for my High School AP Computer Science Class and has not been updated since. I will no longer maintain it as I no longer have a use for it.

If you have any questions about the codebase I am more than willing to answer and help explain the code. Just open an issue and I'll reply back as soon as I can!

### Purpose
- Automate as much grading as possible in a High School AP Computer Science Course
- Remove need for students to print out code
- Lower grade turnaround time
- Support resubmissions for partial credit
- Store all data in Dropbox and be completely serverless

### Screenshots
Overview:
![Overview Screenshot](https://github.com/mattlyons0/Java-Grader/blob/DropboxGrader/screenshots/overview.png?raw=true)
Manually Grade:
![Manually Grade Screenshot](https://github.com/mattlyons0/Java-Grader/blob/DropboxGrader/screenshots/manualGrade.png?raw=true)
Gradebook:
![Gradebook Screenshot](https://github.com/mattlyons0/Java-Grader/blob/DropboxGrader/screenshots/gradebook.png?raw=true)
Grade Reports:
![Grade Report Screenshot](https://github.com/mattlyons0/Java-Grader/blob/DropboxGrader/screenshots/printReport.png?raw=true)
Assignment Configuration (With Simple Unit Test Configured):
![Assignment Config Screenshot](https://github.com/mattlyons0/Java-Grader/blob/DropboxGrader/screenshots/simpleUnitTests.png?raw=true)

### Features
- Grading
  - Display Table of Submitted Assignments
    - Orderable by Assignment, Submission Date, Modification Date, Name or Grade Status
  - Allow for bulk modifications to assignments
    - Select multiple assignments and change any field on all of them at once
  - Filter Submitted Assignments by any field
  - Manual Grading
    - Highlight assignment(s) to create grading queue and click Grade
      - All readable files will be shown in an editor (.java files with syntax highlighting and .txt files)
      - Files may be executed and even edited then executed
      - Execution supports standard input and output
      - Executor does basic classpath analysis
        - If there is invalid code in a file which is not in the classpath compilation will not fail
        - Only dependencies used by main class will be compiled
      - If there are multiple runnable classes grader will be prompted which to run
      - Code can be run multiple times to ensure consistent output
      - Grade may be written with comments
      - Assignments submitted late will have an automatic point deduction and a comment stating said deduction
      - If configured an email may be sent to student when grade is written
    - Grading Queue will be iterated through each time an assignment is graded (grade will be written to gradebook)
    - Grade status will update in submitted Assignment UI once grades are written
    - Grade status will be updated for resubmitted assignments
  - Automated Grading
    - Assignments submitted with auto-grading enabled (any type of unit test specified) will be automatically graded in the background upon application startup (if they haven't been already)
    - Two types of Unit Tests are supported
      - Simple Unit Tests
        - Specify a function signature in the Assignment Creation UI and Java-Grader will search submitted assignments for that method signature then execute said method with specified arguments and test the output is as expected.
      - JUnit Tests
        - Upload a JUnit Test File (via Assignment Creation UI) and said file will be executed against submitted assignments.
      - Grades will be assigned based on lateness status and number of unit tests passed
      - Comments will be provided with which tests passed and failed
- Gradebook Management
  - Support multiple Gradebooks Per Class
  - Store Student Information
    - Name (for identifying submitted assignments)
    - Email (for sending reports when assignments are graded)
  - Store Assignment Information
    - Assignment Number & Name (for identifying submitted assignments)
    - Total Points & Due Date (for autograding and late point deduction)
    - Simple Unit Tests
      - Create Unit Tests from the UI
      - Enter method signature, arguments to pass and expected return value
      - Unit Tests will automatically be run and grades will be determined upon percentage passed
    - JUnit Tests
      - Specify JUnit test file(s)
      - Unit Tests will automatically be run and grades will be determined upon percentage passed
    - Libraries
      - Package will automatically be downloaded and included when assignment submissions are executed
  - Store Grade Information
    - Grade, Comments and Status in External Gradebook
  - Support Grade Reports
    - Allow Printing of All Student Grade Reports (One student Per Page), Specific Student Grade Reports, or entire Gradebook Table.
  - Email Grade Status
    - Support emailing student current grades


### Intended Use
- Create Assignment in Gradebook (in application)
  - Specify Assignment Number, Name, Total Points, Due Date, Libraries required to run, JUnit Tests for Auto Grading and/or *Simple* Unit Tests
- Give students a way to upload specifically named zipped files with homework assignments to a Dropbox Account (Recommended [DropItTo.me](https://dropitto.me/))
  - File Format: `P<PeriodNumber>_<FirstNameLastNameCamelCase>_<AssignmentNumber>_<AssignmentTitle>.zip`
  - Ex: `P2_MattLyons_26_Polygon.zip`
- Open application to grade
  - Assignments with Unit Tests will automatically be run and graded
  - Assignments late will automatically be assigned point deductions
  - Assignments with invalid naming schemes will prompt to be assigned missing fields
  - Assignments without Unit Tests will go into table to be manually graded
    - To manually grade assignments highlight assignments to create a grading queue
    - Click Grade to begin going through the queue
    - All assignments and their dependencies will be downloaded.
    - Each readable file extension (.java, .txt...) will be opened allowing grader to review code (with code highlighting), edit code and execute code (an arbitrary amount of times with standard input and output).
    - If there are multiple executable files grader will be prompted which to run.
    - Grader will be able to enter a numeric grade and a comment. If the assignment is submitted late an auto-generated comment will populate stating that.
  - After grading (if configured) an email will be sent to the student with the assignment grade and comment
- Open Application to view/edit/print gradebook
  - Open application and click Gradebook
  - There are two selection modes, View/Edit and Copy
    - Copy will copy a field on selection to allow for easy transfer into external gradebook (ex: school's own gradebook)
    - View/Edit Allow grades to be viewed, edited, unit tests to be rerun, student information to be edited/created and assignments to be edited/created
  - Grade reports can also be printed in either Table Mode, Individual Student Mode, or for All Students at once.

### Usage / Building
- Java-Grader requres configuration before it can be built and thus it is not possible to distribute a JAR file publicly without sharing said keys
- All secrets are stored in [Data.java](https://github.com/mattlyons0/Java-Grader/blob/DropboxGrader/src/DropboxGrader/Data/Data.java)
  - `APP_KEY` and `APP_SECRET` must be set
    - Values may be obtained from [Dropbox Developers](https://www.dropbox.com/developers/apps/create) and an application must be created with the `Dropbox API` and `Full Dropbox Access`
    - Note: Java-Grader will create a small number of predefined and configurable directories. It will not access any data other than its own.
    - Once an App Key and Secret have been set, any Dropbox Account may use Java Grader and attach it to their Dropbox Account
  - Mail Server Data is optional, it must be set if you desire to use emailing capabilities
    - To use emailing capabilites Java-Grader must be configured with a email server to send emails through. This is similar to information that must be provided to add an external email account to Gmail...
- Once `Data.java` has been configured Java-Grader may be compiled into a jar and or run


- Upon first launch a user will be prompted to allow access to his/her Dropbox Account, copy authorization code and then click another prompt in the application to log in
  - This is a one time authorization process and will remain valid unless the application access is revoked through the user's Dropbox Account
- From there the user must configure the application in Settings
  - If a incorrect folder or class period is specified files will not show
  - If JUnit Jars are not configured JUnit tests will not correctly run
  - Application settings will be saved for subsequent launches

### To-Do
*Features I would implement if I was still working on this project*: [http://mattlyons.net/projects/java-grader/](http://mattlyons.net/projects/java-grader)
