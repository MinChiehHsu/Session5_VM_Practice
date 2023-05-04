package com.training.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.training.model.Goods;
import com.training.service.BackendService;
import com.training.service.FrontendService;

public class FrontendServlet extends HttpServlet{
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGetAndPost(req, resp);
	}	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGetAndPost(req, resp);
	}

	protected void doGetAndPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// 解决 POST请求中文亂碼問題
		req.setCharacterEncoding("UTF-8");
		
		String action = req.getParameter("action");
		switch (action) {
			case "searchGoods":
				// 商品列表 
				searchGoods(req,resp);
				break;
			case "buyGoods":
				// 商品修改 
				buyGoods(req,resp);
				break;
//			case "addGoods":
//				// 商品新增
//				createGoods(req,resp);
//				break;
//			case "deleteAccount":
//				// 商品刪除
//				deleteAccount(req,resp);
//				break;
		}
	}
	
	private void searchGoods(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		System.out.println("顯示該頁商品列表...");
		Set<Goods> goodsList = FrontendService.getInstance().searchGoods(req.getParameter("pageNo"));
		
		goodsList.stream().forEach(goods -> System.out.println(goods));
		
		// Redirect to view
		resp.sendRedirect("VendingMachine.html");
	}
	
	private void buyGoods(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		System.out.println("buyGoods...");
		int insertedAmount=Integer.parseInt(req.getParameter("inputMoney"));
		System.out.println("投入金額:"+insertedAmount);
		
		String[] goodsIDs = req.getParameterValues("goodsID");
		String[] buyQuantities = req.getParameterValues("buyQuantity");
		//過濾所選商品(過濾並取得 buyQuantity>0 的商品)
		Map<Goods,Integer> selectedItems=FrontendService.getInstance().goodsOrders(goodsIDs, buyQuantities);
		
		//計算所選商品總金額(在此步驟抓"商品金額"X"所選數量")
		int orderSum =FrontendService.getInstance().orderSum(selectedItems);
		System.out.println("購買金額:"+orderSum);
		
		//投入金額大於購買金額則 "成立訂單" 並 "更新庫存"
		if(insertedAmount >= orderSum){
			FrontendService.getInstance().createOrder(req.getParameter("customerID"), selectedItems);
			FrontendService.getInstance().updateInv(selectedItems);
			System.out.println("找零金額:"+(insertedAmount-orderSum));
			System.out.println("--------------------------");
		} else { System.out.println("投入金額不足，訂單不成立");
					System.out.println("--------------------------");}
		
			
		// Redirect to view
		resp.sendRedirect("VendingMachine.html");
	}
}
