package com.example.inizjob;

public class Job {
    public String title, company, location, salary, imageUrl;

    public Job() {} // חובה עבור פיירבייס

    public Job(String title, String company, String location, String salary) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.salary = salary;
    }
}