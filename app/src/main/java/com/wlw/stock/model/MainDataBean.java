package com.wlw.stock.model;

public class MainDataBean implements Comparable<MainDataBean> {

    private String name;
    private String code;
    private String price;
    private String number;
    private String equity;
    private String progressDate;
    private String progress;

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getProgressDate() {
        return progressDate;
    }

    public void setProgressDate(String progressDate) {
        this.progressDate = progressDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getEquity() {
        return equity;
    }

    public void setEquity(String equity) {
        this.equity = equity;
    }


    @Override
    public int compareTo(MainDataBean o) {
        return Double.parseDouble(o.equity) - Double.parseDouble(this.equity) > 0 ? 1 : -1;
    }
}
