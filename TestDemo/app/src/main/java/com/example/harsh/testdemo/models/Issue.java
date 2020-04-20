package com.example.harsh.testdemo.models;

import java.util.Date;

/**
 * Created by harsh on 1/21/2017.
 */

public class Issue {

    private String title;
    private String body;
    private Date updatedAt;
    private Date createdAt;
    private int number;
    private int comments;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt(){return createdAt;}

    public void setCreatedAt(Date createdAt)
    {
        this.createdAt= createdAt;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }
}
