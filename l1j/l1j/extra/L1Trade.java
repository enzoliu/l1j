package l1j.extra;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.Account;
import l1j.server.server.model.L1DwarfInventory;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.utils.SQLUtil;

public class L1Trade {
	private int _objid;
	private String _sell_acc;
	private int _item_id;
	private int _item_count;
	private int _char_id;
	private int _enchantlvl;
	private int _bless;
	private int _e_money;
	private int _money;
	private boolean _is_sold;
	private ArrayList<String> _trade_error_msg = new ArrayList<String>();
	
	public L1Trade(int objid) {
		setObjId(objid);
	}
	public void initialize(){
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM trade WHERE objid = ?");
			pstm.setInt(1, _objid);
			rs = pstm.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					setSellAcc(rs.getString("sell_acc"));
					setItemId(rs.getInt("item_id"));
					setItemCount(rs.getInt("item_count"));
					setCharId(rs.getInt("char_id"));
					setEnchantlvl(rs.getInt("enchantlvl"));
					setBless(rs.getInt("bless"));
					setEMoney(rs.getInt("e_money"));
					setMoney(rs.getInt("money"));
					setIsSold(rs.getInt("is_selled"));
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
	public void upd_trade(int count){
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("UPDATE trade SET item_count = ? WHERE objid = ?");
			pstm.setInt(1, count);
			pstm.setInt(2, getObjId());
			pstm.executeUpdate();
			setItemCount(count);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
	public void item_sold(){
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("UPDATE trade SET item_count = 0, is_selled = 1, selled_date = now() WHERE objid = ?");
			pstm.setInt(1, getObjId());
			pstm.executeUpdate();
			setItemCount(0);
			setIsSold(true);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
	public int getItemCount(Account acc,int item_id){
		int result = 0;
		if(!acc.is_dwarf_load()){
			acc.loadDwarfInventory();
		}
		L1DwarfInventory dwarf = acc.getWarehouse();
		L1ItemInstance item = dwarf.findItemId(item_id);
		if(item != null){
			result = item.getCount();
		}
		return result;
	}
	public int[] calc_money(int e,int m){
		if(Math.floor(m/100000000)>0){
			e += Math.floor(m/100000000);
			m = m % 100000000;
		}
		int ret[] = {e,m};
		return ret;
	}
	public boolean is_money_enough(int count,Account buyer){
		boolean result = false;
		if(!buyer.is_dwarf_load()){
			buyer.loadDwarfInventory();
		}
		L1DwarfInventory b_dwarf = buyer.getWarehouse();
		int b_e_m = 0;
		int b_m = 0;
		if(b_dwarf.findItemId(300009) != null){
			b_e_m = b_dwarf.findItemId(300009).getCount();
		}
		if(b_dwarf.findItemId(40308) != null){
			b_m = b_dwarf.findItemId(40308).getCount();
		}
		int buy_arr[] = calc_money(b_e_m,b_m);
		int req_arr[] = calc_money(count * getEMoney(),count * getMoney());
		if(buy_arr[0] > req_arr[0]){
			result = true;
		}else if(buy_arr[0] == req_arr[0]){
			if(buy_arr[1] >= req_arr[1]){
				result = true;
			}
		}
		return result;
	}
	public boolean check_trade(int count,Account buyer_acc){
		boolean result = true;
		Account b_acc = buyer_acc;
		Account s_acc = Account.load(getSellAcc());
		if(buyer_acc.getName().equals(getSellAcc())){
			result = false;
			putErrorMsg("買賣帳號相同。");
		}
		if(getItemCount() < count){
			result = false;
			putErrorMsg("剩餘數量不足。");
		}
		if(b_acc.isAccountWarehouseLock() || s_acc.isAccountWarehouseLock()){
			result = false;
			putErrorMsg("買方或賣方倉庫使用中。");
		}
		if(!is_money_enough(count,b_acc)){
			result = false;
			putErrorMsg("餘額不足。");
		}
		return result;
	}
	public boolean check_trade(int count,String buyer_acc){
		Account b_acc = Account.load(buyer_acc);
		return check_trade(count,b_acc);
	}
	private void setObjId(int id){
		_objid = id;
	}
	private void setSellAcc(String str){
		_sell_acc = str;
	}
	private void setItemId(int id){
		_item_id = id;
	}
	private void setItemCount(int cnt){
		_item_count = cnt;
	}
	private void setCharId(int id){
		_char_id = id;
	}
	private void setEnchantlvl(int lvl){
		_enchantlvl = lvl;
	}
	private void setBless(int bless){
		_bless = bless;
	}
	private void setEMoney(int em){
		_e_money = em;
	}
	private void setMoney(int m){
		_money = m;
	}
	private void setIsSold(int status){
		_is_sold = (status==1)?true:false;
	}
	private void setIsSold(boolean torf){
		_is_sold = torf;
	}
	private void putErrorMsg(String msg){
		_trade_error_msg.add(msg);
	}
	public ArrayList<String> getErrorMsg(){
		return _trade_error_msg;
	}
	public void clearErrorMsg(){
		_trade_error_msg.clear();
	}
	public int getObjId(){
		return _objid;
	}
	public String getSellAcc(){
		return _sell_acc;
	}
	public int getItemId(){
		return _item_id;
	}
	public int getItemCount(){
		return _item_count;
	}
	public int getCharId(){
		return _char_id;
	}
	public int getEnchantlvl(){
		return _enchantlvl;
	}
	public int getBless(){
		return _bless;
	}
	public int getEMoney(){
		return _e_money;
	}
	public int getMoney(){
		return _money;
	}
	public boolean is_sold(){
		return _is_sold;
	}
}
