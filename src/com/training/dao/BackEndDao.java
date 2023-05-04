package com.training.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

import com.training.model.BeverageMember;
import com.training.model.Goods;
import com.training.model.SalesReport;

public class BackEndDao {
	
	private static BackEndDao backendDao = new BackEndDao();
	
	private BackEndDao(){ }

	public static BackEndDao getInstance(){
		return backendDao;
	}
	
	public BeverageMember queryAccountById(String id){
		BeverageMember member = null;		
		// querySQL SQL
		String querySQL = "SELECT IDENTIFICATION_NO, PASSWORD, CUSTOMER_NAME FROM BEVERAGE_MEMBER WHERE IDENTIFICATION_NO = ?";		
		// Step1:取得Connection
		try (Connection conn = DBConnectionFactory.getLocalDBConnection();
		    // Step2:Create prepareStatement For SQL
			PreparedStatement stmt = conn.prepareStatement(querySQL)){
			stmt.setString(1, id);
			try(ResultSet rs = stmt.executeQuery()){
				if(rs.next()){
					member = new BeverageMember();
					member.setIdNo(rs.getString("IDENTIFICATION_NO"));
					member.setCusName(rs.getString("CUSTOMER_NAME"));
					member.setPassWord(rs.getString("PASSWORD"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return member;
	}
	
	/**
	 * 後臺管理商品列表
	 * @return Set(Goods)
	 */
	public Set<Goods> queryGoods() {
		Set<Goods> goods = new LinkedHashSet<>(); 
		Goods good = null;
		String querySQL = "SELECT * FROM BEVERAGE_GOODS";
		try (Connection conn = DBConnectionFactory.getLocalDBConnection();
				PreparedStatement pstmt = conn.prepareStatement(querySQL)){
			try (ResultSet rs = pstmt.executeQuery()){
				while(rs.next()) {
					good=new Goods();
					good.setGoodsID(rs.getBigDecimal("GOODS_ID"));
					good.setGoodsName(rs.getString("GOODS_NAME"));
					good.setGoodsPrice(rs.getInt("PRICE"));
					good.setGoodsQuantity(rs.getInt("QUANTITY"));
					good.setGoodsImageName(rs.getString("IMAGE_NAME"));
					good.setStatus(rs.getString("STATUS"));
					goods.add(good);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return goods;
	}
	
	/**
	 * 後臺管理新增商品
	 * @param goods
	 * @return int(商品編號)
	 */
	public int createGoods(Goods goods){
		int goodsID = 0;
		try (Connection conn = DBConnectionFactory.getLocalDBConnection()){
			// 設置交易不自動提交
			conn.setAutoCommit(false);
			// Insert SQL			
			String insertSQL = 
					"INSERT INTO BEVERAGE_GOODS (GOODS_ID,GOODS_NAME,PRICE,QUANTITY,IMAGE_NAME,STATUS) "+
					"VALUES (BEVERAGE_GOODS_SEQ.NEXTVAL, ?, ?, ?, ?, ?)";
			String[] cols = {"GOODS_ID"};
			// Step2:Create prepareStatement For SQL
			try (PreparedStatement pstmt = conn.prepareStatement(insertSQL, cols)){
				// Step3:將"資料欄位編號"、"資料值"作為引數傳入	
				int count =1;
				pstmt.setString(count++, goods.getGoodsName());
				pstmt.setInt(count++, goods.getGoodsPrice());
				pstmt.setInt(count++, goods.getGoodsQuantity());
				pstmt.setString(count++, goods.getGoodsImageName());
				pstmt.setString(count++, goods.getStatus());
				// Step4:Execute SQL
				pstmt.executeUpdate();
				
				// 取對應的自增主鍵值
				ResultSet rsKeys = pstmt.getGeneratedKeys();
				rsKeys.next();
				goodsID = rsKeys.getInt(1);
				
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
		return goodsID;
	}
	
	/**
	 * 後臺管理更新商品
	 * @param goods
	 * @return boolean
	 */
	public boolean updateGoods(Goods goods) {
		boolean updateSuccess = false;
		try (Connection conn = DBConnectionFactory.getLocalDBConnection()){
			// 設置交易不自動提交
			conn.setAutoCommit(false);			
			// Update SQL
			String updateSql = "UPDATE BEVERAGE_GOODS "
					+ "SET PRICE = ?, QUANTITY = (SELECT QUANTITY FROM BEVERAGE_GOODS WHERE GOODS_ID = ?)+?, STATUS = ? "
					+ "WHERE GOODS_ID = ?";			
			// Step2:Create prepareStatement For SQL
			try (PreparedStatement pstmt = conn.prepareStatement(updateSql)){
				// Step3:將"資料欄位編號"、"資料值"作為引數傳入
				int count =1;
				pstmt.setInt(count++, goods.getGoodsPrice());
				pstmt.setBigDecimal(count++, goods.getGoodsID());
				pstmt.setInt(count++, goods.getGoodsQuantity());
				pstmt.setString(count++, goods.getStatus());
				pstmt.setBigDecimal(count++, goods.getGoodsID());
				// Step4:Execute SQL			
				int updateCount = pstmt.executeUpdate();
				System.out.println("更新異動的資料筆數:" + updateCount);
				// Step5:交易提交
				conn.commit();	
				if(updateCount>0){updateSuccess=true;};
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
	
	/**
	 * 後臺管理顧客訂單查詢
	 * @param queryStartDate
	 * @param queryEndDate
	 * @return Set(SalesReport)
	 */
	public Set<SalesReport> queryOrderBetweenDate(String queryStartDate, String queryEndDate) {
		Set<SalesReport> reports = new LinkedHashSet<>();
		SalesReport sReport = null;
		String querySQL = "WITH MEM AS (SELECT CUSTOMER_NAME, IDENTIFICATION_NO FROM BEVERAGE_MEMBER), "
				+ "ORD AS (SELECT ORDER_ID, ORDER_DATE, CUSTOMER_ID, GOODS_ID, GOODS_BUY_PRICE, BUY_QUANTITY FROM BEVERAGE_ORDER), "
				+ "GOOD AS (SELECT GOODS_ID, GOODS_NAME, PRICE, QUANTITY FROM BEVERAGE_GOODS) "
				+ "SELECT ORDER_ID, CUSTOMER_NAME, ORDER_DATE, GOODS_NAME, GOODS_BUY_PRICE, BUY_QUANTITY, BUY_QUANTITY*GOODS_BUY_PRICE AS BUY_AMOUNT "
				+ "FROM MEM, ORD, GOOD "
				+ "WHERE MEM.IDENTIFICATION_NO = ORD.CUSTOMER_ID "
				+ "  AND ORD.GOODS_ID = GOOD.GOODS_ID "
				+ "  AND ORD.ORDER_DATE >= TO_DATE(?, 'yyyy/MM-dd')-INTERVAL '1' DAY AND ORD.ORDER_DATE <= TO_DATE(?, 'yyyy/MM/dd')+INTERVAL '1' DAY";
		try (Connection conn = DBConnectionFactory.getLocalDBConnection();
				PreparedStatement pstmt = conn.prepareStatement(querySQL)){
					pstmt.setString(1, queryStartDate);
					pstmt.setString(2, queryEndDate);
					try (ResultSet rs = pstmt.executeQuery()){
						while(rs.next()) {
							sReport=new SalesReport();
							sReport.setOrderID(rs.getLong("ORDER_ID"));
							sReport.setCustomerName(rs.getString("CUSTOMER_NAME"));
							sReport.setOrderDate(rs.getString("ORDER_DATE"));
							sReport.setGoodsName(rs.getString("GOODS_NAME"));
							sReport.setGoodsBuyPrice(rs.getInt("GOODS_BUY_PRICE"));
							sReport.setBuyQuantity(rs.getInt("BUY_QUANTITY"));
							sReport.setBuyAmount(rs.getInt("GOODS_BUY_PRICE")*rs.getInt("BUY_QUANTITY"));
							reports.add(sReport);
						}
					}
			} catch (SQLException e) {
				e.printStackTrace();
		}
		return reports;
	}	
	
}
