package com.nhnacademy.aiot;

public enum HttpStatus {
    OK(200, "OK"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    CONFLICT(409, "Conflict"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed");


    HttpStatus(int status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    final int status;
    final String reason;

    @Override
    public String toString() {
        return status + " " + reason;
    }
}
