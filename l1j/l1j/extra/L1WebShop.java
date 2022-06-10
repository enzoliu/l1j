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

public class L1WebShop {
	private ArrayList<String> _trade_error_msg = new ArrayList<String>();
	public ArrayList<Integer> _forbidden_list = new ArrayList<Integer>();
	public L1WebShop() {
		_forbidden_list.add(217);
		_forbidden_list.add(40607);
		_forbidden_list.add(43000);
		_forbidden_list.add(40033);
		_forbidden_list.add(40034);
		_forbidden_list.add(40035);
		_forbidden_list.add(40036);
		_forbidden_list.add(40037);
		_forbidden_list.add(40038);
		_forbidden_list.add(300012);
	}
	public int[] calc_money(int e,long m){
		if(Math.floor(m/100000000)>0){
			e += Math.floor(m/100000000);
			m = m % 100000000;
		}
		int ret[] = {e,(int)m};
		return ret;
	}
	public boolean is_money_enough(Account buyer,int price,int count){
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
		int req_arr[] = calc_money(0,count * price);
		if(buy_arr[0] > req_arr[0]){
			result = true;
		}else if(buy_arr[0] == req_arr[0]){
			if(buy_arr[1] >= req_arr[1]){
				result = true;
			}
		}
		return result;
	}
	
	public boolean check_trade(Account acc,int price,int count){
		boolean result = false;
		if(!is_money_enough(acc,price,count)){
			putErrorMsg("倉庫餘額不足，請存入倉庫後再行交易。");
		}else{
			if(acc.isAccountWarehouseLock()){
				putErrorMsg("倉庫使用中，無法進行交易");;
			}else{
				result = true;
			}
		}
		return result;
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
}
