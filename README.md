# Personal Knowledge Vault

A simple desktop mini project built with **Java + Swing + JDBC + MySQL**.
You can save your notes, useful website links, categories, tags and favorite
resources in one place.

This project is written for engineering students: plain Java, plain SQL, no
Spring Boot, no Hibernate, no Maven.

---

## 1. Folder structure

```
Personal Knowledge Vault (this folder)
|
|-- database.sql              <- SQL script that creates the database
|-- run.bat                   <- double-click to compile and run
|-- README.md                 <- this file
|
|-- lib/
|   +-- mysql-connector-j-8.4.0.jar   <- JDBC driver (Java talks to MySQL)
|
|-- src/
    |-- Main.java             <- starting point
    |-- DBConnection.java     <- database connection (change password here)
    |-- Session.java          <- remembers who is logged in
    |-- LoginPage.java        <- login screen
    |-- RegisterPage.java     <- registration screen
    |-- Dashboard.java        <- home screen with all buttons
    |-- AddNote.java          <- add / edit a note
    |-- ViewNotes.java        <- view, edit, delete notes
    |-- AddResource.java      <- add / edit a resource
    |-- ViewResources.java    <- view, edit, delete resources
    |-- SearchPage.java       <- search notes and resources
    |-- FavoritesPage.java    <- see all favorite items
    +-- CategoriesPage.java   <- create / delete categories
|
+-- out/                      <- compiled .class files (created automatically)
```

---

## 2. Requirements

1. **JDK 8 or newer** (`javac -version` should print a version)
2. **MySQL Server** (MySQL 8.x, or XAMPP which contains MySQL)
3. The JDBC driver `lib/mysql-connector-j-8.4.0.jar` (already included)

### Already installed on this computer (set up for you)

| Item | Value |
|------|-------|
| MySQL version | MySQL Server **8.4.11** Community Edition |
| Windows service | **MySQL84** (Automatic start, port 3306) |
| Install folder | `C:\Program Files\MySQL\MySQL Server 8.4` |
| Config file | `C:\Program Files\MySQL\MySQL Server 8.4\my.ini` |
| Data folder | `C:\ProgramData\MySQL\MySQL Server 8.4\Data` |
| Login | user `root`, password `root` (already matches `DBConnection.java`) |
| Database | `personal_knowledge_vault` is already created with the demo user `student` |
| PATH | `...\MySQL Server 8.4\bin` is on the system PATH |

Useful commands (PowerShell / CMD):

```
Start-Service MySQL84      :: start the database
Stop-Service MySQL84       :: stop the database
mysql -u root -proot       :: open the MySQL command line client
```

> This project has **no packages**, so the `.java` files sit directly in `src/`
> and all classes can see each other without any import of your own classes.

---

## 3. Step 1 - Create the database

Open **MySQL Workbench** (or the MySQL command line) and run the whole file
`database.sql`.

In the MySQL command line you can also do it in one line:

```
mysql -u root -p < database.sql
```

This creates:

* database `personal_knowledge_vault`
* tables `users`, `categories`, `notes`, `resources`
* 6 default categories (Programming, AI/ML, Mathematics, Physics, Chemistry, Personal)
* one demo user: **username = student, password = 123456**

---

## 4. Step 2 - Change MySQL username and password (IMPORTANT)

Open the file **`src/DBConnection.java`**. The first lines of the class are:

```java
// ===== CHANGE THESE 3 LINES FOR YOUR MySQL SETUP =====
private static final String DB_URL =
        "jdbc:mysql://localhost:3306/personal_knowledge_vault"
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "root";
// ====================================================
```

Change only what you need:

| Your MySQL setup                              | What to write                     |
|-----------------------------------------------|-----------------------------------|
| MySQL on this computer, user `root`/`root`    | keep it as it is                  |
| user `root` with **no password** (XAMPP)      | `DB_PASSWORD = ""`                |
| user `student` password `secret`              | `DB_USER = "student"`, `DB_PASSWORD = "secret"` |
| MySQL on another computer / other port        | change `localhost:3306` in `DB_URL` |

**This is the only file you need to edit for the database connection.**
Every other class takes its connection from `DBConnection.getConnection()`.

---

## 5. Step 3 - Compile and run

### Option A (easiest)

Double-click **`run.bat`**.

### Option B (command line - Windows PowerShell / CMD)

```
javac -encoding UTF-8 -cp "lib/*" -d out src\*.java
java -cp "out;lib/*" Main
```

### Option C (Linux / macOS)

```
javac -encoding UTF-8 -cp "lib/*" -d out src/*.java
java -cp "out:lib/*" Main
```

Note: Windows uses `;` between class path parts, Linux/macOS use `:`.

Then log in with `student` / `123456`, or press **Register** to make a new user.

---

## 6. How to use the program

1. **Login** - username and password are checked against the `users` table.
2. **Dashboard** - 8 buttons: Add Note, View Notes, Add Resource, View Resources,
   Search, Favorites, Categories, Logout.
3. **Add Note** - title, content, category (dropdown), favorite checkbox.
   The `notes` table gets one new row (`INSERT`).
4. **View Notes** - all notes in a table.
   * **Edit** opens the same form filled with the note (`SELECT` + `UPDATE`).
   * **Delete** removes the row (`DELETE`).
   * **Toggle Favorite** flips the favorite flag (`UPDATE ... SET favorite = NOT favorite`).
5. **Add / View Resources** - same idea for websites, YouTube links, GitHub
   repositories and online courses. In *View Resources* the **Open Link**
   button opens the URL in your browser.
6. **Categories** - add your own categories or delete unused ones.
   A category that is used by a note cannot be deleted (foreign key protects it).
7. **Search** - type a keyword, choose Notes or Resources and optionally a
   category. SQL `LIKE` does the search on title, content / URL / description.
8. **Favorites** - shows everything you marked as favorite.
9. **Logout** - clears `Session` and returns to the login window.

---

## 7. Database tables

```
users      (user_id, username, password, email)
categories (category_id, category_name)
notes      (note_id, user_id, title, content, category_id, favorite, created_at)
resources  (resource_id, user_id, title, url, description, category_id, favorite, created_at)
```

Foreign keys used:

* `notes.user_id` / `resources.user_id` -> `users.user_id` (ON DELETE CASCADE)
* `notes.category_id` / `resources.category_id` -> `categories.category_id`
* `favorite` is a `BOOLEAN` (MySQL stores it as 0 / 1)

---

## 8. Troubleshooting

| Message | Fix |
|---|---|
| `Access denied for user 'root'` | Wrong `DB_USER` / `DB_PASSWORD` in `DBConnection.java` |
| `Communications link failure` | MySQL server is not running. Start it (services.msc or XAMPP control panel) |
| `Unknown database 'personal_knowledge_vault'` | Run `database.sql` first |
| `Public Key Retrieval is not allowed` | Keep `allowPublicKeyRetrieval=true` in the URL |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | Keep `lib/mysql-connector-j-8.4.0.jar` on the class path (`-cp "lib/*"`) |
| `Table '...' doesn't exist` | Run `database.sql` again, it creates all tables |

---

## 9. Good points for your project presentation

* **JDBC**: `DBConnection.java` uses `DriverManager.getConnection(...)`.
* **PreparedStatement**: every query uses `?` placeholders, so no SQL injection
  and no string concatenation of user input.
* **CRUD**: Create (INSERT), Read (SELECT), Update (UPDATE), Delete (DELETE)
  are used in Notes, Resources, Categories and Users.
* **GUI**: JFrame, JPanel, JLabel, JTextField, JTextArea, JButton, JTable,
  JComboBox, JCheckBox - all from Java Swing.
* **JOIN**: notes/resources are shown together with their category name using
  `LEFT JOIN categories ... ON ...`.
* **Search**: SQL `LIKE '%keyword%'` with parameter binding.

## 10. Honest limitations (good to mention in the viva)

* Passwords are stored as plain text because this is a teaching project.
  A real application must store a **hashed** password (for example SHA-256 or bcrypt).
* Categories are shared by all users; notes and resources belong to one user.
* There is no pagination; the tables show all rows of the logged-in user.
