package com.training.vo;

import org.apache.struts.action.ActionForm;

public class FrontendActionForm extends ActionForm {
	private String pageNo;
	private String searchKeyword;
	private String customerID;
	private String inputMoney;
	private String goodsIDs[];
    private String buyQuantitys[];
    
    public String getPageNo() {
        return pageNo;
    }

    public void setPageNo(String pageNo) {
        this.pageNo = pageNo;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getInputMoney() {
        return inputMoney;
    }

    public void setInputMoney(String inputMoney) {
        this.inputMoney = inputMoney;
    }

    public String[] getGoodsIDs() {
        return goodsIDs;
    }

    public void setGoodsIDs(String[] goodsIDs) {
        this.goodsIDs = goodsIDs;
    }

    public String[] getBuyQuantitys() {
        return buyQuantitys;
    }

    public void setBuyQuantitys(String[] buyQuantitys) {
        this.buyQuantitys = buyQuantitys;
    }

}
