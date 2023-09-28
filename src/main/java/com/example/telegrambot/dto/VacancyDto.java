package com.example.telegrambot.dto;

import com.opencsv.bean.CsvBindAndSplitByPositions;
import com.opencsv.bean.CsvBindByName;

public class VacancyDto {
    @CsvBindByName(column = "id")
    private String id;
    @CsvBindByName(column = "title")
    private String title;
    @CsvBindByName(column = "Short description")
    private String shortDescription;
    @CsvBindByName(column = "Long description")
    private String longDescription;
    @CsvBindByName(column = "company")
    private String company;
    @CsvBindByName(column = "salary")
    private String salary;
    @CsvBindByName(column = "link")
    private String link;

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
}
