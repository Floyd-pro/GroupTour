package com.zjnu.grouptour.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author luchen
 * @Date 2021/5/3 13:43
 * @Description
 */
public class AdminInfoManage {
    private String leftTitle;
    private List<String> rightDatas;

    public String getLeftTitle() {
        return leftTitle == null ? "" : leftTitle;
    }

    public List<String> getRightDatas() {
        if (rightDatas == null) {
            return new ArrayList<>();
        }
        return rightDatas;
    }

    public void setLeftTitle(String leftTitle) {
        this.leftTitle = leftTitle;
    }

    public void setRightDatas(List<String> rightDatas) {
        this.rightDatas = rightDatas;
    }
}
