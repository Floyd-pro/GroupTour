package com.zjnu.grouptour.bean;

/**
 * @author luchen
 * @Date 2021/5/3 16:46
 * @Description Spinner 表名
 */
public class TableBean {

    private String tableName;

    public TableBean() {

    }

    public TableBean(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
