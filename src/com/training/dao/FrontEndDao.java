package com.training.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.training.model.Goods;

public class FrontEndDao {
	
	private static FrontEndDao frontendDao = new FrontEndDao();
	
	private FrontEndDao(){ }

	public static FrontEndDao getInstance(){
		return frontendDao;
	}
	
	public Set<Goods> searchGoods(String pageNum) {
		Set<Goods> goods = new LinkedHashSet<>();
		int itemNum=6;
		int endRowNo = Integer.parseInt(pageNum)*itemNum;
		int startRowNo = endRowNo-(itemNum-1);
		String querySQL = "SELECT * FROM("
				+ "SELECT ROW_NUMBER() OVER (ORDER BY BG.GOODS_ID) as RN, BG.* "
				+ "FROM BEVERAGE_GOODS BG) "
				+ "WHERE RN>= ? AND RN<= ? ORDER BY GOODS_ID";
		// Step1:取得Connection
		try (Connection conn = DBConnectionFactory.getLocalDBConnection();
			 // Step2:Create PreparedStatement For SQL
			 PreparedStatement pstmt = conn.prepareStatement(querySQL)){
			// 設置查詢的欄位值
			int count =1;
//			pstmt.setString(count++, "%"+searchKeyword+"%");
			pstmt.setInt(count++, startRowNo);
			pstmt.setInt(count++, endRowNo);
			try (ResultSet rs = pstmt.executeQuery()){				
				// Step3:Process Result
				while(rs.next()) {
					Goods good = new Goods();
					good.setGoodsID(rs.getBigDecimal("GOODS_ID"));
					good.setGoodsName(rs.getString("GOODS_NAME"));
					good.setGoodsPrice(rs.getInt("PRICE"));
					good.setGoodsQuantity(rs.getInt("QUANTITY"));
					good.setGoodsImageName(rs.getString("IMAGE_NAME"));
					good.setStatus(rs.getString("STATUS"));
					goods.add(good);
				}	
			} catch (SQLException e) {
				throw e;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return goods;
	}
	
	//過濾並記錄所選商品
	public Map<Goods,Integer> goodsOrders(String[] goodsIDs, String[] buyQuantities){
		Set<Goods> selectedGoods = new LinkedHashSet<>();
		Map<Goods,Integer> goodsOrders = new HashMap<>();
		for(int i = 0; i < goodsIDs.length; i++) {
			if(Integer.parseInt(buyQuantities[i])>0){//過濾並取得buyQuantity>0的商品
				Goods good = new Goods();
			    String goodsID = goodsIDs[i];   
			    int buyQuantity = Integer.parseInt(buyQuantities[i]);
			    good.setGoodsID(new BigDecimal(goodsID));
//			    good.setGoodsQuantity(buyQuantity);
			    selectedGoods.add(good);
			    goodsOrders.put(good, buyQuantity);
			}
		}
		//讓goodsOrders抓庫存資料，比對庫存量及價格以利後續使用
		for(Map.Entry<Goods, Integer> entry : goodsOrders.entrySet()){
			String querySQL = "SELECT * FROM BEVERAGE_GOODS WHERE GOODS_ID=?";
			// Step1:取得Connection
			try (Connection conn = DBConnectionFactory.getLocalDBConnection();
				 // Step2:Create PreparedStatement For SQL
				PreparedStatement pstmt = conn.prepareStatement(querySQL)){
				pstmt.setBigDecimal(1,entry.getKey().getGoodsID());
				try (ResultSet rs = pstmt.executeQuery()){				
					// Step3:Process Result
					while(rs.next()) {
						entry.getKey().setGoodsName(rs.getString("GOODS_NAME"));
						entry.getKey().setGoodsPrice(rs.getInt("PRICE"));
						if(rs.getInt("QUANTITY")<1){
							goodsOrders.entrySet().remove(entry);
							System.out.println(rs.getString("GOODS_NAME")+"目前無庫存! 無法購買。");
						} else if(rs.getInt("QUANTITY")<entry.getValue()){
							entry.setValue(rs.getInt("QUANTITY"));
							System.out.println("因庫存不足使用者指定數量，訂單數量將更改為目前庫存所有數量");
						}
					}	
				} catch (SQLException e) {
					throw e;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		return goodsOrders;
	}
	
	//計算所選商品總金額
	public int orderSum(Map<Goods,Integer> goodsOrders) {
		Set<Goods> listedGoods = new LinkedHashSet<>();
		int totalPrice=0;
		for(Map.Entry<Goods, Integer> entry : goodsOrders.entrySet()){
			String querySQL = "SELECT * FROM BEVERAGE_GOODS WHERE GOODS_ID=?";
			// Step1:取得Connection
			try (Connection conn = DBConnectionFactory.getLocalDBConnection();
				 // Step2:Create PreparedStatement For SQL
				PreparedStatement pstmt = conn.prepareStatement(querySQL)){
				pstmt.setBigDecimal(1,entry.getKey().getGoodsID());
				try (ResultSet rs = pstmt.executeQuery()){				
					// Step3:Process Result
					while(rs.next()) {
						Goods good = new Goods();
						good.setGoodsID(rs.getBigDecimal("GOODS_ID"));
						good.setGoodsName(rs.getString("GOODS_NAME"));
						good.setGoodsPrice(rs.getInt("PRICE"));
						good.setGoodsQuantity(entry.getValue());
						good.setGoodsImageName(rs.getString("IMAGE_NAME"));
						good.setStatus(rs.getString("STATUS"));
						listedGoods.add(good);
					}	
				} catch (SQLException e) {
					throw e;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		for(Goods g:listedGoods){
			totalPrice+=g.getGoodsPrice()*g.getGoodsQuantity();
		}
		return totalPrice;
	}
	
	
	public boolean batchUpdateGoodsQuantity(Map<Goods,Integer> goodsOrders){
		boolean updateSuccess = false;
		try (Connection conn = DBConnectionFactory.getLocalDBConnection()){
			// 設置交易不自動提交
			conn.setAutoCommit(false);			
			// Update SQL
			String updateSql = "UPDATE BEVERAGE_GOODS "
					+ "SET QUANTITY = "
					+ "(SELECT QUANTITY FROM BEVERAGE_GOODS WHERE GOODS_ID = ?)-? "
					+ "WHERE GOODS_ID = ?";			
			// Step2:Create prepareStatement For SQL
			try (PreparedStatement pstmt = conn.prepareStatement(updateSql)){
				// Step3:將"資料欄位編號"、"資料值"作為引數傳入
				for(Map.Entry<Goods, Integer> entry : goodsOrders.entrySet()){
					int count =1;
					pstmt.setBigDecimal(count++, entry.getKey().getGoodsID());
					pstmt.setInt(count++, entry.getValue());
					pstmt.setBigDecimal(count++, entry.getKey().getGoodsID());
					pstmt.addBatch();
				};
				// Step4:Execute SQL			
				int [] updateStatus = pstmt.executeBatch();
				for(int update : updateStatus){
					if(update != -2){
						updateSuccess=false;
						break;
					} else {updateSuccess=true;}
				}
				// Step5:交易提交
				conn.commit();	
				
			} catch (SQLException e) {
				// 發生 Exception 交易資料 roll back
				conn.rollback();
				throw e;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return updateSuccess;
	}
	
	public boolean batchCreateGoodsOrder(String customerID, Map<Goods,Integer> goodsOrders){
		boolean insertSuccess = false;
		try (Connection conn = DBConnectionFactory.getLocalDBConnection()){
			// 設置交易不自動提交
			conn.setAutoCommit(false);
			// Insert SQL			
			String insertSQL = 
					"INSERT INTO BEVERAGE_ORDER (ORDER_ID, ORDER_DATE, CUSTOMER_ID, GOODS_ID, GOODS_BUY_PRICE, BUY_QUANTITY) "
					+ "VALUES (BEVERAGE_ORDER_SEQ.NEXTVAL, ?, ?, ?, ?, ?)";
			// Step2:Create prepareStatement For SQL
			try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)){
				// Step3:將"資料欄位編號"、"資料值"作為引數傳入	
				for (Map.Entry<Goods, Integer> entry : goodsOrders.entrySet()) {
					Goods good = entry.getKey();
					int quantity=entry.getValue();
					int count =1;
					pstmt.setTimestamp(count++, new Timestamp(new Date().getTime()));
					pstmt.setString(count++, customerID);
					pstmt.setBigDecimal(count++, good.getGoodsID());
					pstmt.setInt(count++, good.getGoodsPrice());
					pstmt.setInt(count++, quantity);
					pstmt.addBatch();
					System.out.println("商品名稱:"+good.getGoodsName());
					System.out.println("　　金額:"+good.getGoodsPrice()+" 購買數量:"+quantity);
				}
				// Step4:Execute SQL
				int [] updateStatus = pstmt.executeBatch();
				for(int update : updateStatus){
					if(update != -2){
						insertSuccess=false;
						break;
					} else {insertSuccess=true;}
				}
				
				// Step5:Transaction commit(交易提交)
				conn.commit();
				
			} catch (SQLException e) {
				// 若發生錯誤則資料 rollback(回滾)
				conn.rollback();
				throw e;
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return insertSuccess;
	}	
}
