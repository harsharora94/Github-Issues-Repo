package com.example.harsh.testdemo.http.api;

import org.json.JSONArray;

/**
 * Created by harsh on 1/21/2017.
 */

public class JSONWebServiceResponse {

    private final JSONArray result;
    private final int code;

    public JSONWebServiceResponse(JSONArray result, int code)
    {
        super();
        this.result = result;
        this.code = code;
    }

    public JSONArray getResult() {
        return result;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "JSONWebServiceResponse {result: " + result + ", code: " + code + "}";
    }
}
