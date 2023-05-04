package com.training.action;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.upload.FormFile;

import com.training.model.Goods;
import com.training.model.SalesReport;
import com.training.service.BackendService;
import com.training.vo.BackendActionForm;


public class BackendAction extends DispatchAction{
	//商品查詢頁面
//	public ActionForward queryGoodsPage(ActionMapping mapping, ActionForm form, 
//            HttpServletRequest request, HttpServletResponse response) throws Exception{
//		return mapping.findForward("VM_Backend_GoodsListPage");
//	}
	public ActionForward queryGoods(ActionMapping mapping, ActionForm form, 
            HttpServletRequest request, HttpServletResponse response) throws Exception{
		System.out.println("庫存一覽");
		Set<Goods> goodsList = BackendService.getInstance().queryGoods();
		
		goodsList.stream().forEach(goods -> System.out.println(goods));
		
		// Redirect to view
		return mapping.findForward("VM_Backend_GoodsListPage");
	}
	
	//商品維護頁面
	public ActionForward updateGoodsPage(ActionMapping mapping, ActionForm form, 
            HttpServletRequest request, HttpServletResponse response) throws Exception{
		return mapping.findForward("VM_Backend_GoodsReplenishmentPage");
	}
	public ActionForward updateGoods(ActionMapping mapping, ActionForm form, 
            HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		BackendActionForm updateGoodsForm = (BackendActionForm)form;
		Goods good = new Goods();
		good.setGoodsID(updateGoodsForm.getGoodsID());
		good.setGoodsPrice(updateGoodsForm.getGoodsPrice());
		good.setGoodsQuantity(updateGoodsForm.getGoodsQuantity());
		good.setStatus(updateGoodsForm.getStatus());
		
		boolean modifyResult = BackendService.getInstance().modifyGoods(good);
		String message = modifyResult ? "商品資料修改成功！" : "商品資料修改失敗！";
		System.out.println(message);
		
		// Redirect to view
		return mapping.findForward("VM_Backend_GoodsReplenishment");
	}
	
	//新增商品頁
	public ActionForward createGoodsPage(ActionMapping mapping, ActionForm form, 
            HttpServletRequest request, HttpServletResponse response) throws Exception{
		return mapping.findForward("VM_Backend_GoodsCreatePage");
	}
	public ActionForward createGoods(ActionMapping mapping, ActionForm form, 
            HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		BackendActionForm createGoodsForm = (BackendActionForm)form;
		Goods goods = new Goods();
		FormFile goodsImage = createGoodsForm.getGoodsImage();
	    if (goodsImage != null) {
	    	String goodsImageName = goodsImage.getFileName();
	        ServletContext servletContext = getServlet().getServletContext();
	        String goodsImgPath = servletContext.getInitParameter("GoodsImgPath");
	        String serverGoodsImgPath = servletContext.getRealPath(goodsImgPath);
	                  
	        Path serverImgPath = Paths.get(serverGoodsImgPath).resolve(goodsImageName);
	        try (InputStream fileContent = goodsImage.getInputStream()) {
	            Files.copy(fileContent, serverImgPath, StandardCopyOption.REPLACE_EXISTING);
	        }
	        goods.setGoodsImageName(goodsImageName);
	    } else {
	        createGoodsForm.setGoodsImageName("Image not available");
	    }
	    
		goods.setGoodsName(createGoodsForm.getGoodsName());
		goods.setGoodsPrice(createGoodsForm.getGoodsPrice());
		goods.setGoodsQuantity(createGoodsForm.getGoodsQuantity());	
		goods.setStatus(createGoodsForm.getStatus());
		
	    int GID = BackendService.getInstance().createGoods(goods);
		boolean createResult = GID>0 ? true:false;
		String message = createResult ? "商品新增成功！" : "商品新增失敗！";
		System.out.println(message);
		System.out.println("新增商品: " +goods.getGoodsName()+" ID為: "+GID);
		return mapping.findForward("VM_Backend_GoodsCreate");
	}
	
	//銷售報表頁面
	public ActionForward queryOrderBetweenDatePage(ActionMapping mapping, ActionForm form, 
            HttpServletRequest request, HttpServletResponse response) throws Exception{
		return mapping.findForward("VM_Backend_GoodsSaleReportPage");
	}
	public ActionForward queryOrderBetweenDate(ActionMapping mapping, ActionForm form, 
            HttpServletRequest request, HttpServletResponse response) throws Exception{
		System.out.println("querySalesReport...");
		BackendActionForm salesRepoForm = (BackendActionForm)form;
		Set<SalesReport> reports = BackendService.getInstance().queryOrderBetweenDate(salesRepoForm.getQueryStartDate(), salesRepoForm.getQueryEndDate());
		reports.stream().forEach(r -> System.out.println(r));
		
		// Redirect to view
		return mapping.findForward("VM_Backend_GoodsSaleReport");		
	}
}
