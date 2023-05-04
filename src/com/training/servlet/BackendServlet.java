package com.training.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import javax.servlet.http.Part;

import com.training.model.Goods;
import com.training.model.SalesReport;
import com.training.service.BackendService;

@MultipartConfig
public class BackendServlet extends HttpServlet {
	
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
			case "queryGoods":
				// 商品列表 
				queryGoods(req,resp);
				break;
			case "updateGoods":
				// 商品修改 
				modifyGoods(req,resp);
				break;
			case "addGoods":
				// 商品新增
				createGoods(req,resp);
				break;
			case "querySalesReport":
				// 銷售報表
				queryOrderBetweenDate(req,resp);
				break;
		}
	}
	
	private void queryGoods(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		System.out.println("庫存一覽");
		Set<Goods> goodsList = BackendService.getInstance().queryGoods();
		
		goodsList.stream().forEach(goods -> System.out.println(goods));
		
		// Redirect to view
		resp.sendRedirect("VM_Backend_GoodsList.html");
	}
	
	private void modifyGoods(HttpServletRequest req, HttpServletResponse resp) throws IOException {		 
		// 將表單資料轉換儲存資料物件
		Goods good = new Goods();
		good.setGoodsID(new BigDecimal(req.getParameter("goodsID")));
		good.setGoodsPrice(Integer.parseInt(req.getParameter("goodsPrice")));
		good.setGoodsQuantity(Integer.parseInt(req.getParameter("goodsQuantity")));
		good.setStatus(req.getParameter("status"));
		
		boolean modifyResult = BackendService.getInstance().modifyGoods(good);
		String message = modifyResult ? "商品資料修改成功！" : "商品資料修改失敗！";
		System.out.println(message);
		
		// Redirect to view
		resp.sendRedirect("VM_Backend_GoodsReplenishment.html");
	}
	

	private void createGoods(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		// 將表單資料轉換儲存資料物件
		System.out.println("新增商品作業...");
		
		Goods good = new Goods();

		good.setGoodsName(req.getParameter("goodsName"));
		good.setGoodsPrice(Integer.parseInt(req.getParameter("goodsPrice")));
		good.setGoodsQuantity(Integer.parseInt(req.getParameter("goodsQuantity")));		
		//取得圖片相關資訊及上傳圖片
		Part filePart = req.getPart("goodsImage");
		String fileName = filePart.getSubmittedFileName();
		good.setGoodsImageName(fileName);
		//取得圖片資料夾路徑(在web.xml的init-Param)
		
		String goodsImgPath = getInitParameter("GoodsImgPath");
		String serverGoodsImgPath = getServletContext().getRealPath(goodsImgPath);
		Path serverImgPath = Paths.get(serverGoodsImgPath).resolve(fileName);
	    try (InputStream fileContent = filePart.getInputStream();){
	        Files.copy(fileContent, serverImgPath, StandardCopyOption.REPLACE_EXISTING);
	    }
		good.setStatus(req.getParameter("status"));
		
		int GID = BackendService.getInstance().createGoods(good);
		boolean createResult = GID>0 ? true:false;
		String message = createResult ? "商品新增成功！" : "商品新增失敗！";
		System.out.println(message);
		System.out.println("新增商品: " +req.getParameter("goodsName")+" ID為: "+GID);
		
		
		// Redirect to view
		resp.sendRedirect("VM_Backend_GoodsCreate.html");		
	}
	
	private void queryOrderBetweenDate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		System.out.println("querySalesReport...");
		Set<SalesReport> reports = BackendService.getInstance().queryOrderBetweenDate(req.getParameter("queryStartDate"), req.getParameter("queryEndDate"));
		reports.stream().forEach(r -> System.out.println(r));
		
		// Redirect to view
		resp.sendRedirect("VM_Backend_GoodsSaleReport.html");		
	}
	
}
