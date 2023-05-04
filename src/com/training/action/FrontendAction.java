package com.training.action;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.training.model.Goods;
import com.training.service.FrontendService;
import com.training.vo.FrontendActionForm;

public class FrontendAction extends DispatchAction{
	
	public ActionForward searchGoods(ActionMapping mapping, ActionForm form, 
            HttpServletRequest request, HttpServletResponse response) throws Exception{
		System.out.println("顯示該頁商品列表...");
		FrontendActionForm pageList = (FrontendActionForm)form;
		Set<Goods> goodsList = FrontendService.getInstance().searchGoods(pageList.getPageNo());
		
		goodsList.stream().forEach(goods -> System.out.println(goods));
		
		// Redirect to view
		return mapping.findForward("VM_Frontend");
	}
	
	public ActionForward buyGoods(ActionMapping mapping, ActionForm form, 
            HttpServletRequest request, HttpServletResponse response) throws Exception{
		System.out.println("buyGoods...");
		FrontendActionForm buyAction = (FrontendActionForm)form;
		
		int insertedAmount=Integer.parseInt(buyAction.getInputMoney());
		System.out.println("投入金額:"+insertedAmount);
		
		String[] goodsIDs = buyAction.getGoodsIDs();
		String[] buyQuantities = buyAction.getBuyQuantitys();
		//過濾所選商品(過濾並取得 buyQuantity>0 的商品)
		Map<Goods,Integer> selectedItems=FrontendService.getInstance().goodsOrders(goodsIDs, buyQuantities);
		
		//計算所選商品總金額(在此步驟抓"商品金額"X"所選數量")
		int orderSum =FrontendService.getInstance().orderSum(selectedItems);
		System.out.println("購買金額:"+orderSum);
		
		//投入金額大於購買金額則 "成立訂單" 並 "更新庫存"
		if(insertedAmount >= orderSum){
			FrontendService.getInstance().createOrder(buyAction.getCustomerID(), selectedItems);
			FrontendService.getInstance().updateInv(selectedItems);
			System.out.println("找零金額:"+(insertedAmount-orderSum));
			System.out.println("--------------------------");
		} else { System.out.println("投入金額不足，訂單不成立");
					System.out.println("--------------------------");}
		
		// Redirect to view
		return mapping.findForward("VM_Frontend");
	}
}
