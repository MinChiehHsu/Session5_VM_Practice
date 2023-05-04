package com.training.service;

import java.util.List;
import java.util.Set;

import com.training.dao.BackEndDao;
import com.training.model.Goods;
import com.training.model.SalesReport;

public class BackendService {
	
	private static BackendService backendService = new BackendService();

	private BackendService(){ }
	
	private BackEndDao backendDao = BackEndDao.getInstance();
	
	public static BackendService getInstance(){
		return backendService;
	}
	
	public Set<Goods> queryGoods(){
		
		return backendDao.queryGoods();
	}

	public boolean modifyGoods(Goods goods) {	
		
		return backendDao.updateGoods(goods);
	}

	public int createGoods(Goods goods) {
		
		return backendDao.createGoods(goods);
	}

	public Set<SalesReport> queryOrderBetweenDate(String queryStartDate, String queryEndDate) {

		return backendDao.queryOrderBetweenDate(queryStartDate, queryEndDate);
	}
}
