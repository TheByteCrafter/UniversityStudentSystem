📋 Copy README.mdCopied to clipboard!
🎓 University Student System
A comprehensive Object-Oriented Student Management System built with Java Swing and MySQL. This application manages students, lecturers, courses, scores, and a library module with borrowing and reservation features.

✨ Features
👨‍🎓 Student Management
Add, update, delete, and view students
Enroll students in courses (maximum 5 per student)
View enrolled courses and scores
Generate result slips with grade calculation
👨‍🏫 Lecturer Management
Add, update, delete, and view lecturers
Assign courses to lecturers
View assigned courses
📚 Course Management
Add, update, delete, and view courses
Assign credit hours and link to lecturers
Track course enrollments
📊 Score Management
Enter CAT (max 30) and Exam (max 70) scores
Automatic grade calculation (A, B, C, D, F)
Search scores by student
Update existing scores
📖 Library Module
Search books by title with live filtering
Borrow books (14-day loan period)
Return books with overdue fine calculation
Reserve books when copies are unavailable
View and cancel reservations (max 3 per student)
Automatic notification for reserved books
🏗️ Architecture
The project follows the MVC (Model-View-Controller) pattern:

src/
├── model/          # Data models (Student, Lecturer, Course, Book, etc.)
├── view/           # GUI components (Swing)
├── controller/     # Business logic and database operations
└── utils/          # Utilities (DatabaseConnection, FileStorage, etc.)
Data Persistence
MySQL Database – Primary storage for all data
File Storage – Backup/fallback using serialization
🛠️ Technologies Used
Java (JDK 11 or higher)
Java Swing – GUI framework
MySQL – Relational database
JDBC – Database connectivity
Git – Version control
📋 Prerequisites
Java JDK 11 or higher
MySQL Server 8.0 or higher
MySQL Connector/J (JDBC driver)
🚀 Installation & Setup
1. Clone the Repository
git clone https://github.com/TheByteCrafter/UniversityStudentSystem.git
cd UniversityStudentSystem
2. Database Setup
The application automatically creates the database and tables on first run.
Ensure MySQL is running and update credentials in DatabaseConnection.java:

private String url = "jdbc:mysql://127.0.0.1:3306/student_system?useSSL=false&serverTimezone=UTC";
private String username = "root";
private String password = "your_password";
3. Add MySQL Connector
Download mysql-connector-java-8.0.33.jar (or latest)
Add it to your project's classpath
4. Run the Application
javac -cp .;mysql-connector-java-8.0.33.jar src/Main.java
java -cp .;mysql-connector-java-8.0.33.jar Main
5. Default Login Credentials
Role	Username	Password
Admin	admin	admin123
Lecturer	lecturer1	pass123
Student	student1	pass123
🔄 Workflow Example
Admin adds students, lecturers, and courses
Admin enrolls students in courses
Lecturer logs in to enter scores for students
Student logs in to view results and check enrolled courses
Student borrows/reserves books from the library
Admin manages book inventory
📊 Database Schema
Key tables:

users – Authentication
students, lecturers – Person details
courses – Course information
enrollments – Student-course relationships
scores – Student grades
books, borrow_records, reservations – Library module
🧪 Testing
Add sample data via the GUI or SQL scripts
Test enrollment limits (max 5 courses)
Test duplicate entry handling
Test overdue book calculations
🤝 Contributing
Fork the repository
Create a feature branch (git checkout -b feature/AmazingFeature)
Commit changes (git commit -m 'Add some AmazingFeature')
Push to branch (git push origin feature/AmazingFeature)
Open a Pull Request
📝 License
This project is for educational purposes as part of COSC 223: Object Oriented Programming at Chuka University.

👥 Authors
Your Name – Initial work – TheByteCrafter
🙏 Acknowledgements
Chuka University – COSC 223 course
All contributors and testers
📧 Contact
For any inquiries, please contact: fmmuthengi@chuka.ac.ke
