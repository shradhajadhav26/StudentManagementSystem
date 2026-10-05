import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;

public class StudentManagementSystem {

    // =========================================================
    // SETTINGS
    // =========================================================

    static final int PORT = 8080;
    static final int MAX_STUDENTS = 500;

    static ArrayList<Student> students = new ArrayList<>();


    // =========================================================
    // STUDENT CLASS
    // =========================================================

    static class Student {

        String name;
        String rollNo;
        String department;
        String semester;
        double marks;

        Student(String name,
                String rollNo,
                String department,
                String semester,
                double marks) {

            this.name = name;
            this.rollNo = rollNo;
            this.department = department;
            this.semester = semester;
            this.marks = marks;
        }
    }


    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) throws Exception {

        HttpServer server =
                HttpServer.create(
                        new InetSocketAddress(PORT),
                        0
                );

        server.createContext(
                "/",
                StudentManagementSystem::home
        );

        server.createContext(
                "/add",
                StudentManagementSystem::addStudent
        );

        server.createContext(
                "/update",
                StudentManagementSystem::updateStudent
        );

        server.createContext(
                "/delete",
                StudentManagementSystem::deleteStudent
        );

        server.createContext(
                "/clear",
                StudentManagementSystem::clearAll
        );

        server.setExecutor(
                Executors.newFixedThreadPool(10)
        );

        server.start();

        System.out.println();
        System.out.println("======================================");
        System.out.println(" STUDENT MANAGEMENT SYSTEM");
        System.out.println("======================================");
        System.out.println("Server started successfully!");
        System.out.println();
        System.out.println("Open in browser:");
        System.out.println("http://localhost:8080/");
        System.out.println();
        System.out.println("Maximum Students : 500");
        System.out.println("======================================");
    }


    // =========================================================
    // HOME PAGE
    // =========================================================

    static void home(HttpExchange exchange)
            throws IOException {

        String query =
                exchange.getRequestURI().getQuery();

        String search = "";
        String message = "";

        if (query != null) {

            Map<String, String> params =
                    parseQuery(query);

            search =
                    params.getOrDefault(
                            "q",
                            ""
                    );

            message =
                    params.getOrDefault(
                            "msg",
                            ""
                    );
        }

        String html =
                createHTML(
                        search,
                        message
                );

        sendResponse(
                exchange,
                html
        );
    }


    // =========================================================
    // ADD STUDENT
    // =========================================================

    static void addStudent(
            HttpExchange exchange
    ) throws IOException {

        Map<String, String> data =
                getPostData(exchange);

        String name =
                data.getOrDefault(
                        "name",
                        ""
                ).trim();

        String roll =
                data.getOrDefault(
                        "roll",
                        ""
                ).trim();

        String department =
                data.getOrDefault(
                        "department",
                        ""
                ).trim();

        String semester =
                data.getOrDefault(
                        "semester",
                        ""
                ).trim();

        String marksText =
                data.getOrDefault(
                        "marks",
                        ""
                ).trim();


        // Check empty fields

        if (name.isEmpty()
                || roll.isEmpty()
                || department.isEmpty()
                || semester.isEmpty()
                || marksText.isEmpty()) {

            redirect(
                    exchange,
                    "/?msg=" +
                    encode(
                            "Please fill all fields."
                    )
            );

            return;
        }


        // Maximum 500

        if (students.size() >= MAX_STUDENTS) {

            redirect(
                    exchange,
                    "/?msg=" +
                    encode(
                            "Maximum 500 students allowed."
                    )
            );

            return;
        }


        // Duplicate roll number

        for (Student s : students) {

            if (s.rollNo.equalsIgnoreCase(roll)) {

                redirect(
                        exchange,
                        "/?msg=" +
                        encode(
                                "Roll number already exists."
                        )
                );

                return;
            }
        }


        try {

            double marks =
                    Double.parseDouble(
                            marksText
                    );


            if (marks < 0 || marks > 100) {

                redirect(
                        exchange,
                        "/?msg=" +
                        encode(
                                "Marks must be between 0 and 100."
                        )
                );

                return;
            }


            Student student =
                    new Student(
                            name,
                            roll,
                            department,
                            semester,
                            marks
                    );

            students.add(student);


            redirect(
                    exchange,
                    "/?msg=" +
                    encode(
                            "Student added successfully!"
                    )
            );

        } catch (NumberFormatException e) {

            redirect(
                    exchange,
                    "/?msg=" +
                    encode(
                            "Enter valid marks."
                    )
            );
        }
    }


    // =========================================================
    // UPDATE STUDENT
    // =========================================================

    static void updateStudent(
            HttpExchange exchange
    ) throws IOException {

        Map<String, String> data =
                getPostData(exchange);

        String name =
                data.getOrDefault(
                        "name",
                        ""
                ).trim();

        String roll =
                data.getOrDefault(
                        "roll",
                        ""
                ).trim();

        String department =
                data.getOrDefault(
                        "department",
                        ""
                ).trim();

        String semester =
                data.getOrDefault(
                        "semester",
                        ""
                ).trim();

        String marksText =
                data.getOrDefault(
                        "marks",
                        ""
                ).trim();


        if (name.isEmpty()
                || roll.isEmpty()
                || department.isEmpty()
                || semester.isEmpty()
                || marksText.isEmpty()) {

            redirect(
                    exchange,
                    "/?msg=" +
                    encode(
                            "Please fill all fields."
                    )
            );

            return;
        }


        Student found = null;


        for (Student s : students) {

            if (s.rollNo.equalsIgnoreCase(roll)) {

                found = s;
                break;
            }
        }


        if (found == null) {

            redirect(
                    exchange,
                    "/?msg=" +
                    encode(
                            "Student not found."
                    )
            );

            return;
        }


        try {

            double marks =
                    Double.parseDouble(
                            marksText
                    );


            if (marks < 0 || marks > 100) {

                redirect(
                        exchange,
                        "/?msg=" +
                        encode(
                                "Marks must be between 0 and 100."
                        )
                );

                return;
            }


            found.name = name;
            found.department = department;
            found.semester = semester;
            found.marks = marks;


            redirect(
                    exchange,
                    "/?msg=" +
                    encode(
                            "Student updated successfully!"
                    )
            );

        } catch (NumberFormatException e) {

            redirect(
                    exchange,
                    "/?msg=" +
                    encode(
                            "Enter valid marks."
                    )
            );
        }
    }


    // =========================================================
    // DELETE STUDENT
    // =========================================================

    static void deleteStudent(
            HttpExchange exchange
    ) throws IOException {

        Map<String, String> data =
                getPostData(exchange);

        String roll =
                data.getOrDefault(
                        "roll",
                        ""
                ).trim();


        if (roll.isEmpty()) {

            redirect(
                    exchange,
                    "/?msg=" +
                    encode(
                            "Enter Roll No."
                    )
            );

            return;
        }


        boolean deleted = false;


        Iterator<Student> iterator =
                students.iterator();


        while (iterator.hasNext()) {

            Student s =
                    iterator.next();


            if (s.rollNo.equalsIgnoreCase(roll)) {

                iterator.remove();

                deleted = true;

                break;
            }
        }


        if (deleted) {

            redirect(
                    exchange,
                    "/?msg=" +
                    encode(
                            "Student deleted successfully!"
                    )
            );

        } else {

            redirect(
                    exchange,
                    "/?msg=" +
                    encode(
                            "Student not found."
                    )
            );
        }
    }


    // =========================================================
    // CLEAR ALL
    // =========================================================

    static void clearAll(
            HttpExchange exchange
    ) throws IOException {

        students.clear();

        redirect(
                exchange,
                "/?msg=" +
                encode(
                        "All student records cleared."
                )
        );
    }


    // =========================================================
    // CREATE HTML GUI
    // =========================================================

    static String createHTML(
            String search,
            String message
    ) {

        StringBuilder html =
                new StringBuilder();


        // =====================================================
        // CALCULATE STATISTICS
        // =====================================================

        double totalMarks = 0;

        for (Student s : students) {

            totalMarks += s.marks;
        }


        double averageMarks = 0;

        if (!students.isEmpty()) {

            averageMarks =
                    totalMarks /
                    students.size();
        }


        int progress =
                (students.size() * 100)
                / MAX_STUDENTS;


        // =====================================================
        // FILTER STUDENTS
        // =====================================================

        ArrayList<Student> displayStudents =
                new ArrayList<>();


        if (search == null
                || search.trim().isEmpty()) {

            displayStudents.addAll(students);

        } else {

            String searchText =
                    search.toLowerCase();


            for (Student s : students) {

                if (s.name
                        .toLowerCase()
                        .contains(searchText)

                        ||

                    s.rollNo
                        .toLowerCase()
                        .contains(searchText)

                        ||

                    s.department
                        .toLowerCase()
                        .contains(searchText)

                        ||

                    s.semester
                        .toLowerCase()
                        .contains(searchText)) {

                    displayStudents.add(s);
                }
            }
        }


        // =====================================================
        // HTML START
        // =====================================================

        html.append("""
<!DOCTYPE html>

<html>

<head>

<meta charset="UTF-8">

<meta name="viewport"
      content="width=device-width,
               initial-scale=1.0">

<title>
Student Management System
</title>


<style>

/* =========================================================
   GLOBAL
========================================================= */

* {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

body {

    font-family:
        Arial,
        Helvetica,
        sans-serif;

    background:
        #f1f5f9;

    color:
        #1e293b;
}


/* =========================================================
   HEADER
========================================================= */

.header {

    background:
        linear-gradient(
            135deg,
            #0b2859,
            #1769c2
        );

    color:
        white;

    padding:
        30px 7%;

    box-shadow:
        0 5px 20px
        rgba(0,0,0,0.15);
}

.header-content {

    max-width:
        1400px;

    margin:
        auto;
}

.logo-area {

    display:
        flex;

    align-items:
        center;

    gap:
        18px;
}

.logo {

    width:
        60px;

    height:
        60px;

    border-radius:
        15px;

    background:
        rgba(255,255,255,0.15);

    display:
        flex;

    align-items:
        center;

    justify-content:
        center;

    font-size:
        30px;
}

.header h1 {

    font-size:
        28px;

    margin-bottom:
        6px;
}

.header p {

    color:
        #dbeafe;

    font-size:
        14px;
}


/* =========================================================
   CONTAINER
========================================================= */

.container {

    width:
        90%;

    max-width:
        1400px;

    margin:
        30px auto;
}


/* =========================================================
   DASHBOARD CARDS
========================================================= */

.dashboard {

    display:
        grid;

    grid-template-columns:
        repeat(3, 1fr);

    gap:
        20px;

    margin-bottom:
        25px;
}

.card {

    background:
        white;

    padding:
        24px;

    border-radius:
        17px;

    border:
        1px solid #e2e8f0;

    box-shadow:
        0 5px 18px
        rgba(15,23,42,0.08);
}

.card-title {

    color:
        #64748b;

    font-size:
        13px;

    text-transform:
        uppercase;

    letter-spacing:
        1px;
}

.card-value {

    font-size:
        30px;

    font-weight:
        bold;

    color:
        #123f82;

    margin-top:
        8px;
}

.card small {

    display:
        block;

    margin-top:
        8px;

    color:
        #64748b;
}

.progress {

    height:
        7px;

    background:
        #e2e8f0;

    border-radius:
        10px;

    margin-top:
        13px;

    overflow:
        hidden;
}

.progress-bar {

    height:
        100%;

    background:
        #2563eb;

    width:
""");

        html.append(progress);

        html.append("""
%;
}


/* =========================================================
   MESSAGE
========================================================= */

.message {

    padding:
        14px 18px;

    background:
        #dcfce7;

    border:
        1px solid #86efac;

    color:
        #166534;

    border-radius:
        10px;

    margin-bottom:
        20px;

    font-weight:
        bold;

    font-size:
        14px;
}


/* =========================================================
   MAIN GRID
========================================================= */

.main-grid {

    display:
        grid;

    grid-template-columns:
        380px 1fr;

    gap:
        25px;
}


/* =========================================================
   CARDS
========================================================= */

.form-card,
.table-card {

    background:
        white;

    border-radius:
        18px;

    padding:
        25px;

    border:
        1px solid #e2e8f0;

    box-shadow:
        0 5px 18px
        rgba(15,23,42,0.08);
}


/* =========================================================
   TITLES
========================================================= */

.section-title {

    font-size:
        21px;

    font-weight:
        bold;

    color:
        #123f82;

    margin-bottom:
        6px;
}

.section-subtitle {

    color:
        #64748b;

    font-size:
        13px;

    margin-bottom:
        22px;
}


/* =========================================================
   FORM
========================================================= */

.form-group {

    margin-bottom:
        15px;
}

.form-group label {

    display:
        block;

    font-size:
        13px;

    font-weight:
        bold;

    color:
        #475569;

    margin-bottom:
        7px;
}

input,
select {

    width:
        100%;

    padding:
        12px 13px;

    border:
        1px solid #cbd5e1;

    border-radius:
        9px;

    font-size:
        14px;

    outline:
        none;

    background:
        #f8fafc;

    transition:
        0.2s;
}

input:focus,
select:focus {

    border-color:
        #2563eb;

    background:
        white;

    box-shadow:
        0 0 0 3px
        rgba(37,99,235,0.10);
}


/* =========================================================
   BUTTONS
========================================================= */

.button-grid {

    display:
        grid;

    grid-template-columns:
        1fr 1fr;

    gap:
        10px;

    margin-top:
        15px;
}

button {

    border:
        none;

    border-radius:
        9px;

    padding:
        12px;

    font-size:
        13px;

    font-weight:
        bold;

    cursor:
        pointer;

    transition:
        0.2s;
}

button:hover {

    transform:
        translateY(-1px);

    box-shadow:
        0 4px 12px
        rgba(0,0,0,0.12);
}

.btn-primary {

    background:
        #2563eb;

    color:
        white;
}

.btn-warning {

    background:
        #e89b13;

    color:
        white;
}

.btn-danger {

    background:
        #dc3545;

    color:
        white;
}

.btn-secondary {

    background:
        #475569;

    color:
        white;
}

.btn-light {

    background:
        #e2e8f0;

    color:
        #334155;
}


/* =========================================================
   SEARCH
========================================================= */

.search-box {

    display:
        flex;

    gap:
        10px;

    margin-bottom:
        20px;
}

.search-box input {

    flex:
        1;
}

.search-box button {

    width:
        110px;
}


/* =========================================================
   TABLE
========================================================= */

.table-wrapper {

    overflow-x:
        auto;

    border:
        1px solid #e2e8f0;

    border-radius:
        12px;
}

table {

    width:
        100%;

    border-collapse:
        collapse;

    min-width:
        700px;
}

thead {

    background:
        #123f82;

    color:
        white;
}

th {

    padding:
        14px;

    text-align:
        left;

    font-size:
        12px;

    text-transform:
        uppercase;

    letter-spacing:
        0.5px;
}

td {

    padding:
        14px;

    border-bottom:
        1px solid #e2e8f0;

    font-size:
        13px;
}

tbody tr:hover {

    background:
        #f8fafc;
}

.student-name {

    font-weight:
        bold;
}

.roll-badge {

    background:
        #dbeafe;

    color:
        #1d4ed8;

    padding:
        5px 9px;

    border-radius:
        20px;

    font-weight:
        bold;

    font-size:
        12px;
}

.semester {

    background:
        #f1f5f9;

    padding:
        5px 9px;

    border-radius:
        7px;
}

.marks {

    font-weight:
        bold;

    color:
        #0f766e;
}

.empty {

    text-align:
        center;

    padding:
        35px;

    color:
        #94a3b8;
}


/* =========================================================
   FOOTER
========================================================= */

.footer {

    text-align:
        center;

    color:
        #64748b;

    font-size:
        12px;

    margin-top:
        30px;

    padding:
        20px;
}


/* =========================================================
   RESPONSIVE
========================================================= */

@media(max-width: 1000px) {

    .main-grid {

        grid-template-columns:
            1fr;
    }

    .dashboard {

        grid-template-columns:
            repeat(3, 1fr);
    }
}

@media(max-width: 700px) {

    .container {

        width:
            94%;
    }

    .dashboard {

        grid-template-columns:
            1fr;
    }

    .header h1 {

        font-size:
            21px;
    }

    .button-grid {

        grid-template-columns:
            1fr;
    }
}

</style>

</head>


<body>


<!-- =====================================================
     HEADER
===================================================== -->

<header class="header">

<div class="header-content">

<div class="logo-area">

<div class="logo">
🎓
</div>

<div>

<h1>
Student Record Management System
</h1>

<p>
Pillai College of Engineering
&nbsp; | &nbsp;
Academic Student Portal
</p>

</div>

</div>

</div>

</header>


<!-- =====================================================
     CONTAINER
===================================================== -->

<div class="container">


<!-- =====================================================
     DASHBOARD
===================================================== -->

<div class="dashboard">


<div class="card">

<div class="card-title">
Total Students
</div>

<div class="card-value">
""");

        html.append(students.size());

        html.append("""
</div>

<div class="progress">

<div class="progress-bar">
</div>

</div>

<small>
Maximum capacity: 500 students
</small>

</div>



<div class="card">

<div class="card-title">
Average Marks
</div>

<div class="card-value">
""");

        html.append(
                String.format(
                        "%.2f",
                        averageMarks
                )
        );

        html.append("""
%
</div>

<small>
Overall academic performance
</small>

</div>



<div class="card">

<div class="card-title">
System Status
</div>

<div class="card-value"
     style="color:#0f9d72">

● Online

</div>

<small>
Java Server • localhost:8080
</small>

</div>


</div>


<!-- =====================================================
     MESSAGE
===================================================== -->

""");

        if (message != null
                && !message.isEmpty()) {

            html.append(
                    "<div class=\"message\">"
            );

            html.append(
                    escape(message)
            );

            html.append(
                    "</div>"
            );
        }


        // ==================================================
        // MAIN GRID
        // ==================================================

        html.append("""
<div class="main-grid">


<!-- =====================================================
     FORM
===================================================== -->

<div class="form-card">

<div class="section-title">
Student Details
</div>

<div class="section-subtitle">
Add or update student information
</div>


<form action="/add"
      method="POST">


<div class="form-group">

<label>
Student Name
</label>

<input
type="text"
name="name"
id="name"
placeholder="Enter full name"
required>

</div>


<div class="form-group">

<label>
Roll Number
</label>

<input
type="text"
name="roll"
id="roll"
placeholder="Enter roll number"
required>

</div>


<div class="form-group">

<label>
Department
</label>

<select
name="department"
id="department"
required>

<option value="">
Select Department
</option>

<option>EXTC</option>

<option>Computer Engineering</option>

<option>Information Technology</option>

<option>Electronics Engineering</option>

<option>Mechanical Engineering</option>

<option>Civil Engineering</option>

<option>AI & Data Science</option>

<option>IoT</option>

</select>

</div>


<div class="form-group">

<label>
Semester
</label>

<select
name="semester"
id="semester"
required>

<option value="">
Select Semester
</option>

<option>Semester 1</option>

<option>Semester 2</option>

<option>Semester 3</option>

<option>Semester 4</option>

<option>Semester 5</option>

<option>Semester 6</option>

<option>Semester 7</option>

<option>Semester 8</option>

</select>

</div>


<div class="form-group">

<label>
Marks
</label>

<input
type="number"
name="marks"
id="marks"
min="0"
max="100"
step="0.01"
placeholder="Enter marks"
required>

</div>


<div class="button-grid">

<button
class="btn-primary"
type="submit">

+ Add Student

</button>

<button
class="btn-warning"
type="button"
onclick="updateStudent()">

✎ Update

</button>

</div>

</form>


<div class="button-grid">


<form action="/delete"
      method="POST"
      onsubmit="return confirmDelete()">

<input
type="hidden"
name="roll"
id="deleteRoll">

<button
class="btn-danger"
type="submit"
style="width:100%">

Delete Student

</button>

</form>


<button
class="btn-secondary"
type="button"
onclick="clearForm()">

Clear Form

</button>


</div>


<form
action="/clear"
method="POST"
onsubmit="return confirm('Are you sure you want to delete ALL student records?');">

<button
class="btn-light"
type="submit"
style="width:100%; margin-top:10px">

Clear All Records

</button>

</form>


</div>


<!-- =====================================================
     TABLE
===================================================== -->

<div class="table-card">

<div class="section-title">
Student Records
</div>

<div class="section-subtitle">
View, search and manage registered students
</div>


<form
action="/"
method="GET"
class="search-box">

<input
type="text"
name="q"
value="
""");

        html.append(
                escape(
                        search == null
                                ? ""
                                : search
                )
        );

        html.append("""
"
placeholder="Search by name, roll number, department...">

<button
class="btn-primary"
type="submit">

Search

</button>

</form>


<div class="table-wrapper">

<table>

<thead>

<tr>

<th>
#
</th>

<th>
Student
</th>

<th>
Roll No.
</th>

<th>
Department
</th>

<th>
Semester
</th>

<th>
Marks
</th>

</tr>

</thead>

<tbody>
""");


        // ==================================================
        // TABLE ROWS
        // ==================================================

        int serial = 1;


        for (Student s : displayStudents) {

            html.append("""
<tr>

<td>
""");

            html.append(serial++);

            html.append("""
</td>

<td>

<div class="student-name">
""");

            html.append(
                    escape(s.name)
            );

            html.append("""
</div>

</td>

<td>

<span class="roll-badge">
""");

            html.append(
                    escape(s.rollNo)
            );

            html.append("""
</span>

</td>

<td>
""");

            html.append(
                    escape(s.department)
            );

            html.append("""
</td>

<td>

<span class="semester">
""");

            html.append(
                    escape(s.semester)
            );

            html.append("""
</span>

</td>

<td>

<span class="marks">
""");

            html.append(
                    String.format(
                            "%.2f",
                            s.marks
                    )
            );

            html.append("""
%
</span>

</td>

</tr>
""");
        }


        if (displayStudents.isEmpty()) {

            html.append("""
<tr>

<td colspan="6"
    class="empty">

No student records found.

</td>

</tr>
""");
        }


        // ==================================================
        // CLOSE HTML
        // ==================================================

        html.append("""
</tbody>

</table>

</div>

</div>

</div>


<div class="footer">

Student Record Management System
&nbsp; • &nbsp;
Java HTTP Server
&nbsp; • &nbsp;
Maximum 500 Records

</div>


</div>


<script>


// =========================================================
// CLEAR FORM
// =========================================================

function clearForm() {

    document.getElementById("name").value = "";

    document.getElementById("roll").value = "";

    document.getElementById("department").value = "";

    document.getElementById("semester").value = "";

    document.getElementById("marks").value = "";

}


// =========================================================
// UPDATE STUDENT
// =========================================================

function updateStudent() {

    let name =
        document.getElementById("name").value;

    let roll =
        document.getElementById("roll").value;

    let department =
        document.getElementById("department").value;

    let semester =
        document.getElementById("semester").value;

    let marks =
        document.getElementById("marks").value;


    if (
        name === "" ||
        roll === "" ||
        department === "" ||
        semester === "" ||
        marks === ""
    ) {

        alert(
            "Please fill all fields."
        );

        return;
    }


    let form =
        document.createElement("form");

    form.method = "POST";

    form.action = "/update";


    let fields = {

        name: name,

        roll: roll,

        department: department,

        semester: semester,

        marks: marks

    };


    for (
        let key in fields
    ) {

        let input =
            document.createElement("input");

        input.type = "hidden";

        input.name = key;

        input.value =
            fields[key];

        form.appendChild(input);
    }


    document.body.appendChild(form);

    form.submit();
}


// =========================================================
// DELETE
// =========================================================

function confirmDelete() {

    let roll =
        document.getElementById("roll").value;


    if (roll === "") {

        alert(
            "Enter Roll Number first."
        );

        return false;
    }


    document.getElementById(
        "deleteRoll"
    ).value = roll;


    return confirm(
        "Are you sure you want to delete this student?"
    );
}

</script>


</body>

</html>
""");


        return html.toString();
    }


    // =========================================================
    // POST DATA
    // =========================================================

    static Map<String, String> getPostData(
            HttpExchange exchange
    ) throws IOException {

        String body =
                new String(
                        exchange
                                .getRequestBody()
                                .readAllBytes(),
                        StandardCharsets.UTF_8
                );

        return parseQuery(body);
    }


    // =========================================================
    // QUERY PARSER
    // =========================================================

    static Map<String, String> parseQuery(
            String query
    ) {

        Map<String, String> map =
                new HashMap<>();


        if (query == null
                || query.isEmpty()) {

            return map;
        }


        String[] pairs =
                query.split("&");


        for (String pair : pairs) {

            String[] parts =
                    pair.split(
                            "=",
                            2
                    );


            String key =
                    decode(parts[0]);


            String value = "";


            if (parts.length > 1) {

                value =
                        decode(parts[1]);
            }


            map.put(
                    key,
                    value
            );
        }


        return map;
    }


    // =========================================================
    // ENCODE
    // =========================================================

    static String encode(
            String value
    ) {

        try {

            return URLEncoder.encode(
                    value,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {

            return value;
        }
    }


    // =========================================================
    // DECODE
    // =========================================================

    static String decode(
            String value
    ) {

        try {

            return URLDecoder.decode(
                    value,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {

            return value;
        }
    }


    // =========================================================
    // HTML ESCAPE
    // =========================================================

    static String escape(
            String value
    ) {

        if (value == null) {

            return "";
        }


        return value

                .replace(
                        "&",
                        "&amp;"
                )

                .replace(
                        "<",
                        "&lt;"
                )

                .replace(
                        ">",
                        "&gt;"
                )

                .replace(
                        "\"",
                        "&quot;"
                )

                .replace(
                        "'",
                        "&#39;"
                );
    }


    // =========================================================
    // SEND RESPONSE
    // =========================================================

    static void sendResponse(
            HttpExchange exchange,
            String response
    ) throws IOException {

        byte[] bytes =
                response.getBytes(
                        StandardCharsets.UTF_8
                );


        exchange
                .getResponseHeaders()
                .set(
                        "Content-Type",
                        "text/html; charset=UTF-8"
                );


        exchange.sendResponseHeaders(
                200,
                bytes.length
        );


        OutputStream output =
                exchange.getResponseBody();


        output.write(bytes);

        output.close();
    }


    // =========================================================
    // REDIRECT
    // =========================================================

    static void redirect(
            HttpExchange exchange,
            String location
    ) throws IOException {

        exchange
                .getResponseHeaders()
                .set(
                        "Location",
                        location
                );


        exchange.sendResponseHeaders(
                303,
                -1
        );


        exchange.close();
    }
}