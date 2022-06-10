/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l1j.server.server.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.shop.L1Shop;
import l1j.server.server.templates.L1ShopItem;
import l1j.server.server.utils.SQLUtil;

public class ShopTable {

	private static final long serialVersionUID = 1L;

	private static Logger _log = Logger.getLogger(ShopTable.class.getName());

	private static ShopTable _instance;

	private Map<Integer, L1Shop> _allShops = new HashMap<Integer, L1Shop>();
	//新所有物品販賣 by eric1300460
	private Map<Integer, Integer> _allItemSells = new HashMap<Integer, Integer>();
	//~新所有物品販賣 by eric1300460
	public static ShopTable getInstance() {
		if (_instance == null) {
			_instance = new ShopTable();
		}
		return _instance;
	}

	private ShopTable() {
		loadShops();
		set_all_sell("armor");
		set_all_sell("weapon");
		set_all_sell("etcitem");
	}

	private ArrayList<Integer> enumNpcIds() {
		ArrayList<Integer> ids = new ArrayList<Integer>();

		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT DISTINCT npc_id FROM shop");
			rs = pstm.executeQuery();
			while (rs.next()) {
				ids.add(rs.getInt("npc_id"));
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs, pstm, con);
		}
		return ids;
	}
	
	private int find_item_price(String type,int itemId){
		String stmt_str = "";
		int ret_val = -127;
		if(type=="etcitem"){
			stmt_str = "SELECT item_price FROM etcitem WHERE item_id=?";
		}else if(type=="weapon"){
			stmt_str = "SELECT item_price FROM weapon WHERE item_id=?";
		}else if(type=="armor"){
			stmt_str = "SELECT item_price FROM armor WHERE item_id=?";
		}
		Connection conn_item = null;
		PreparedStatement stmt_item = null;
		ResultSet rs_item = null;
		try{
			conn_item = L1DatabaseFactory.getInstance().getConnection();
			stmt_item = conn_item.prepareStatement(stmt_str);
			stmt_item.setInt(1,itemId);
			rs_item = stmt_item.executeQuery();
			rs_item.last();
			if(rs_item.getRow() >0){
				ret_val = rs_item.getInt("item_price");
			}
			rs_item.close();
			stmt_item.close();
			conn_item.close();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs_item, stmt_item, conn_item);
		}
		return ret_val;
	}
	private void set_all_sell(String type){
		String stmt_str = "";
		stmt_str = "SELECT * FROM "+type;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int itemId = 0;
		int price = 0;
		try{
			conn = L1DatabaseFactory.getInstance().getConnection();
			stmt = conn.prepareStatement(stmt_str);
			rs = stmt.executeQuery();
			while(rs.next()){
				itemId = rs.getInt("item_id");
				price = rs.getInt("item_price")/2;
				if(_allItemSells.get(itemId)==null){
					_allItemSells.put(itemId, price);
				}
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs, stmt, conn);
		}
	}
	
	private L1Shop loadShop(int npcId, ResultSet rs) throws SQLException {
		List<L1ShopItem> sellingList = new ArrayList<L1ShopItem>();
		List<L1ShopItem> purchasingList = new ArrayList<L1ShopItem>();
		while (rs.next()) {
			int itemId = rs.getInt("item_id");
			int price_ori = 0;
			int result = 1;
			result = find_item_price("etcitem",itemId);
			if(result==-127){
				result = find_item_price("weapon",itemId);
				if(result==-127){
					result = find_item_price("armor",itemId);
				}
			}
			
			
			price_ori = result;
			
			int sellingPrice = price_ori;
			int purchasingPrice = price_ori/2;
			int packCount = rs.getInt("pack_count");
			packCount = packCount == 0 ? 1 : packCount;
			if (sellingPrice > 0) {
				L1ShopItem item = new L1ShopItem(itemId, sellingPrice,packCount);
				sellingList.add(item);
			}
			if (purchasingPrice > 0) {
				L1ShopItem item = new L1ShopItem(itemId, purchasingPrice,packCount);
				purchasingList.add(item);
			}
			if(_allItemSells.get(itemId)==null){
				_allItemSells.put(itemId, purchasingPrice);
			}
		}
		return new L1Shop(npcId, sellingList, purchasingList);
	}

	public void loadShops() {
		//fix by sdcom
		_allShops = new HashMap<Integer, L1Shop>();
		_allItemSells = new HashMap<Integer, Integer>();
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM shop WHERE npc_id=? ORDER BY order_id");
			for (int npcId : enumNpcIds()) {
				pstm.setInt(1, npcId);
				rs = pstm.executeQuery();
				L1Shop shop = loadShop(npcId, rs);
				_allShops.put(npcId, shop);
				rs.close();
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs, pstm, con);
		}
	}
	public void updOrder(int npc_id,int item_id,int order_id){
		// add	sdcom	2010/9/8
		try{
			Connection connection = null;
			connection = L1DatabaseFactory.getInstance().getConnection();
			PreparedStatement preparedstatement = connection.prepareStatement("UPDATE `shop` SET `order_id`=? WHERE `npc_id`=? AND `item_id`=?");
			preparedstatement.setInt(1, order_id);
			preparedstatement.setInt(2, npc_id);
			preparedstatement.setInt(3, item_id);
			preparedstatement.execute();		
			preparedstatement.close();
			connection.close();
		}catch(Exception e){
			System.out.println("update can't be correctly use");
		}
	}
	public void InsertItem(int npcid,int itemid,int order){	
		// add	sdcom	2010/9/8
		int maxorder = 0;
		L1Shop target_shop = get(npcid);
		for(int i=0;i<target_shop.getSellingItems().size();i++){
			if(order==-1){
				if((target_shop.getSellingItems().size()-1)>maxorder)
					maxorder = target_shop.getSellingItems().size();
			} else {
				maxorder = order;
				if((target_shop.getSellingItems().size()-1)>=order){
					updOrder(npcid,target_shop.getSellingItems().get(i).getItemId(),i);
				}
			}
		}
		
		try{
			Connection con = L1DatabaseFactory.getInstance().getConnection();
			PreparedStatement pstmt = con.prepareStatement("INSERT INTO `shop` (`npc_id`,`item_id`,`order_id`,`pack_count`) VALUES(?,?,?,?)");
			pstmt.setInt(1, npcid);
			pstmt.setInt(2, itemid);
			pstmt.setInt(3, maxorder);
			pstmt.setInt(4, 1);
			pstmt.execute();
			con.close();
			pstmt.close();
		}catch(Exception e){
			_log.warning("can't insert item into shop"+e);
		}
	}
	//新所有物品販賣 by eric1300460
	//fix by sdcom 
	public int getItemPrice(int id){
		if(_allItemSells.get(id)==null){
			return 0;
		}else{
			return _allItemSells.get(id);
		}
	}
	//~新所有物品販賣 by eric1300460
	public L1Shop get(int npcId) {
		return _allShops.get(npcId);
	}
}
