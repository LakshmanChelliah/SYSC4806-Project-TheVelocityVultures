# 4th Year Project Management System
A web-based system designed to manage 4th-year project topics, student applications, oral presentations, and final report submissions. This system streamlines project coordination for professors, students, and the 4th-year project coordinator.  

## Project Hosted on Azure
Project is hosted on azure and available at this link:
https://sysc4806-lab5-erik-v2-gwhkcfeeckapb0e8.canadacentral-01.azurewebsites.net/home

## Milestone 3 Features

- Enforce report submission endpoint + deadline enforcement https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/37  
- Create Coordinator account type and coordinator home page https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/49  
- Define behaviour for projects with no description https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/51  
- Remove student ability to delete projects https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/52  
- Fix login header not updating after username change https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/53  
- Professor and student availability input for oral presentations https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/56  
- Room Assignment Module for oral presentations https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/57  
- Project ownership rules & edit restrictions https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/58  
- Student Profile: Add team information section https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/59  
- Fix coordinator header behaviour when logged in https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/67  
- Integration tests for new Milestone 3 features https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/61  

## Milestone 2 Features
- Project Creation UI for Profs https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/18
- Project Detail and availability, and search UI https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/32
	@@ -16,13 +31,11 @@ A web-based system designed to manage 4th-year project topics, student applicati
- Azure Hosting https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/4
- Simple Professor's Web UI https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/3

## UML Diagrams 

#### Module Diagram
<img width="1034" height="817" alt="components-Application" src="https://github.com/user-attachments/assets/0adcd5ed-c5d7-4c79-a708-77961efd7eb4" />

#### Database Schema Diagram
<img width="1543" height="531" alt="database-schema-m3-sysc-4806" src="https://github.com/user-attachments/assets/a4d18584-b337-434f-bee1-0726424e6037" />

#### Class UML Diagrams
<img width="1634" height="614" alt="student_class_diagram" src="https://github.com/user-attachments/assets/4f8fbfbc-5fec-4a24-a359-bef9507dbc8d" />
<img width="1840" height="707" alt="project_class_diagram" src="https://github.com/user-attachments/assets/3fd6fd29-9b56-4856-a759-c066660490d4" />
<img width="774" height="561" alt="professor_class_diagram" src="https://github.com/user-attachments/assets/9bf8fceb-6340-4983-891a-024d47344f1f" />
<img width="2021" height="885" alt="presentation_class_diagram" src="https://github.com/user-attachments/assets/70b60a77-1dc1-4545-a8e1-358f52db4973" />
<img width="812" height="544" alt="coordinator_class_diagram" src="https://github.com/user-attachments/assets/1acda742-b36a-45ad-9a35-0354b7b76178" />
<img width="1497" height="508" alt="availablility_class_diagram" src="https://github.com/user-attachments/assets/4327dac5-3911-4a35-9bcb-34eaae07ae7b" />
<img width="781" height="442" alt="auth_class_diagram" src="https://github.com/user-attachments/assets/df6d53a8-608c-42f9-a33d-defcfd958cf6" />
<img width="1872" height="849" alt="allocation_class_diagram" src="https://github.com/user-attachments/assets/aa19c59e-b14b-466f-846a-96248b8df925" />


## Technology Stack
	@@ -206,9 +127,9 @@ This project uses a GitHub Projects Kanban workflow to track tasks across the sp
Below is the snapshot of our board at the end of Milestone 2:
<img width="1910" height="863" alt="KanbanSS" src="https://github.com/user-attachments/assets/002cd4da-5b3d-4826-9938-5274f7b695d6" />

## 📌 Kanban Board Snapshot (Milestone 3) 
Below is the snapshot of our board for our plan for Milestone 3 (as of 12:56am on Dec 1):
<img width="1813" height="934" alt="image" src="https://github.com/user-attachments/assets/517088b2-9f85-4cda-a5ba-f3b3f4aace13" />


## Contributors
- Darren Wallace  
- Maahir Muhammad  
- Ayoub Hassan  
- Erik Cald  
- Lakshman Chelliah

  
## Installation & Setup
1. Clone the repository:  
   ```bash
   git clone https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures.git
   javac -d bin src/**/*.java
   java -cp bin Application
