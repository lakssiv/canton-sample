package org.example.dto;

import java.util.Collections;
import java.util.List;

public class PackagesResponse {

    private List<String> packageIds;

    public List<String> getPackageIds() {
        return packageIds == null ? Collections.emptyList() : packageIds;
    }

    public void setPackageIds(List<String> packageIds) {
        this.packageIds = packageIds;
    }

    @Override
    public String toString() {
        return "PackagesResponse{" +
                "packageIds=" + getPackageIds() +
                '}';
    }
}