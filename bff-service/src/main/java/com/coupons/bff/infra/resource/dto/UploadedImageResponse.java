package com.coupons.bff.infra.resource.dto;

public class UploadedImageResponse {

    private String path;

    public UploadedImageResponse() {}

    public UploadedImageResponse(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
