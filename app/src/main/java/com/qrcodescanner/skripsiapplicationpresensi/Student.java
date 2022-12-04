package com.qrcodescanner.skripsiapplicationpresensi;

// this is modal class
public class Student {
    private String studentID; //1000101
    private String email; //s55555@gmail.com
    private String name; //Jeremy Pongantung
    private String password; //s55555
    private String status; //outsider dekat

    public Student() {
    }

    public Student(String studentID, String email, String name, String password, String status) {
        this.studentID = studentID;
        this.email = email;
        this.name = name;
        this.password = password;
        this.status = status;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
