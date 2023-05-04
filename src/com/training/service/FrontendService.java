package com.training.service;

import java.util.Map;
import java.util.Set;

import com.training.dao.FrontEndDao;
import com.training.model.Goods;

public class FrontendService {
	private static FrontendService frontendService = new FrontendService();

	private FrontendService(){ }
	
	private FrontEndDao frontendDao = FrontEndDao.getInstance();
	
	public static FrontendService getInstance(){
		return frontendService;
	}
	
	public Set<Goods> searchGoods(String pageNum){
		return frontendDao.searchGoods(pageNum);
	}
	
	public Map<Goods,Integer> goodsOrders(String[] goodsIDs, String[] buyQuantities){
		return frontendDao.goodsOrders(goodsIDs, buyQuantities);
	}
	
	public int orderSum(Map<Goods,Integer> goodsOrders){
		return frontendDao.orderSum(goodsOrders);
	}
	
	public boolean createOrder(String customerID, Map<Goods,Integer> goodsOrders){
		return frontendDao.batchCreateGoodsOrder(customerID, goodsOrders);
	}
	
	public boolean updateInv(Map<Goods,Integer> goodsOrders){
		return frontendDao.batchUpdateGoodsQuantity(goodsOrders);
	}
}
