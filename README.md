# AttendanceRefistrationWithSpringBoot
This is an evolution of the JavaAttendanceRegistrationSoftware the request from the client are now managed by SprinBoot
This simple Server/Client software that allows for clients to reggister their attendances in events created by administrators

Keywords
➡️ JAVA
➡️ JavaFX
➡️ OOP
➡️ MVC - Design Pattern
➡️ Serialization
➡️ TCP
➡️ UDP
➡️ SQLite (Relational Database)
Important Notes:
MainServer, Client and BackUpServer should all have the following respective configurations

5001 1099 database-backup "sqlite"
5001 localhost
path to temp file ex: "C:/Users/.../.../temp"
Someting.txt file in directory temp should be deleted before running the BackUpServer
Client credentials(it is possible to register new client accounts in the client app):
username: client@test password:1234
Admin credentials(it is only possible to add an admin through the sql file, an BD managment software like DataGrip does the trick):
username: admin@test password: 1234

I made this project alongside with: <a href="https://github.com/joaoeoneves">joaoeoneves</a> and <a href="https://github.com/WojciechLarecki"> WojciechLarecki</a>
