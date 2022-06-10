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
package l1j.server.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import l1j.extra.L1Trade;
import l1j.extra.L1WebShop;
import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.datatables.ShopTable;
import l1j.server.server.model.L1DwarfInventory;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.ServerBasePacket;

// Referenced classes of package l1j.server.server:
// PacketHandler, Logins, IpTable, LoginController,
// ClanTable, IdFactory
//
public class TradeClientThread implements Runnable, PacketOutput {
	private BufferedReader _in;
	private PrintWriter _out;
	private L1PcInstance _activeChar;
	private String _ip;
	private String _hostname;
	private Socket _csocket;
	private static Account _account;
	private static int _status = 0;
	private static String _return = "";

	public TradeClientThread(Socket socket) throws IOException {
		_csocket = socket;
		_ip = socket.getInetAddress().getHostAddress();
		if (Config.HOSTNAME_LOOKUPS) {
			_hostname = socket.getInetAddress().getHostName();
		} else {
			_hostname = _ip;
		}
		_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		_out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
	}

	public String getIp() {
		return _ip;
	}
	public String getHostname() {
		return _hostname;
	}
	private void insert_trade(int objid,L1ItemInstance item,int count,int e_money,int money){
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("INSERT INTO trade (objid,sell_acc,item_id,item_name,item_count,enchantlvl,durability,bless,e_money,money) VALUES(?,?,?,?,?,?,?,?,?,?)");
			pstm.setInt(1, objid);
			pstm.setString(2, _account.getName());
			pstm.setInt(3, item.getItemId());
			pstm.setString(4, item.getName());
			pstm.setInt(5, count);
			pstm.setInt(6, item.getEnchantLevel());
			pstm.setInt(7, item.get_durability());
			pstm.setInt(8, item.getBless());
			pstm.setInt(9, e_money);
			pstm.setInt(10, money);
			pstm.executeUpdate();
		}catch(Exception e){
			System.out.println(e.toString());
		}
	}
	private void trade_upd_money(L1Trade trade,Account buyer,int count){
		Account sell_acc = Account.load(trade.getSellAcc());
		Account buy_acc = buyer;
		if(!sell_acc.is_dwarf_load()){
			sell_acc.loadDwarfInventory();
		}
		if(!buy_acc.is_dwarf_load()){
			buy_acc.loadDwarfInventory();
		}
		L1DwarfInventory s_d = sell_acc.getWarehouse();
		L1DwarfInventory b_d = buy_acc.getWarehouse();
		L1ItemInstance s_e_m = s_d.findItemId(300009);
		L1ItemInstance s_m = s_d.findItemId(40308);
		int sell_arr[] = trade.calc_money(count * trade.getEMoney(),count * trade.getMoney());
		int seller_arr[] = trade.calc_money(((s_e_m!=null)?s_e_m.getCount():0) + sell_arr[0], ((s_m!=null)?s_m.getCount():0) + sell_arr[1]);
		L1ItemInstance b_e_m = b_d.findItemId(300009);
		L1ItemInstance b_m = b_d.findItemId(40308);
		//seller e money
		if(s_e_m == null){
			if(seller_arr[0]>0){
				s_e_m = ItemTable.getInstance().createItem(300009);
				s_e_m.setCount(seller_arr[0]);
				s_d.insertItem(s_e_m);
			}
		}else if(s_e_m.getCount() > 0){
			s_e_m.setCount(seller_arr[0]);
			s_d.updateItem(s_e_m);
		}
		//seller m
		if(s_m == null){
			if(seller_arr[1]>0){
				s_m = ItemTable.getInstance().createItem(40308);
				s_m.setCount(seller_arr[1]);
				s_d.insertItem(s_m);
			}
		}else if(s_m.getCount() > 0){
			s_m.setCount(seller_arr[1]);
			s_d.updateItem(s_m);
		}
		//buyer e money
		if(b_e_m != null){
			b_e_m.setCount(b_e_m.getCount() - sell_arr[0]);
			if(b_e_m.getCount() == 0){
				b_d.deleteItem(b_e_m);
			}else{
				b_d.updateItem(b_e_m);
			}
		}
		//buyer money
		if(b_m != null){
			b_m.setCount(b_m.getCount() - sell_arr[1]);
			if(b_m.getCount() == 0){
				b_d.deleteItem(b_m);
			}else{
				b_d.updateItem(b_m);
			}
		}
	}
	private void web_upd_money(Account buyer,int emoney,int money,int count){
		Account buy_acc = buyer;
		buy_acc.loadDwarfInventory();
		L1DwarfInventory b_d = buy_acc.getWarehouse();
		L1ItemInstance b_e_m = b_d.findItemId(300009);
		L1ItemInstance b_m = b_d.findItemId(40308);
		L1WebShop webshop = new L1WebShop();
		int sell_arr[] = webshop.calc_money(count * emoney ,count * money);
		//buyer money
		if(b_m != null){	//money found
			if(b_m.getCount() >= sell_arr[1]){	//money enough
				b_m.setCount(b_m.getCount() - sell_arr[1]);
				if(b_m.getCount() == 0){
					b_d.deleteItem(b_m);
				}else{
					b_d.updateItem(b_m);
				}
			}else{	//money not enough
				if(b_e_m.getCount() > sell_arr[0]){
					sell_arr[0] += 1;
					b_m.setCount(b_m.getCount() + 100000000 - sell_arr[1]);
					if(b_m.getCount() == 0){
						b_d.deleteItem(b_m);
					}else{
						b_d.updateItem(b_m);
					}
				}else{
					System.out.println(buyer.getName()+"金額不足");
				}
			}
		}else if(sell_arr[1]>0){	//money not found
			if(b_e_m.getCount() > sell_arr[0]){
				sell_arr[0] += 1;
				L1ItemInstance insert_money = ItemTable.getInstance().createItem(40308);
				b_d.insertItem(insert_money);
				b_d.loadItems();	//reload
				b_m = b_d.findItemId(40308);
				if(b_m != null){
					b_m.setCount(100000000 - sell_arr[1]);
					if(b_m.getCount() == 0){
						b_d.deleteItem(b_m);
					}else{
						b_d.updateItem(b_m);
					}
				}else{
					System.out.println("金錢尚未更新");
				}
			}else{
				System.out.println(buyer.getName()+"金額不足");
			}
		}
		//buyer e money
		if(b_e_m != null){
			if(b_e_m.getCount() >= sell_arr[0]){
				b_e_m.setCount(b_e_m.getCount() - sell_arr[0]);
				if(b_e_m.getCount() == 0){
					b_d.deleteItem(b_e_m);
				}else{
					b_d.updateItem(b_e_m);
				}
			}else{
				System.out.println(buyer.getName()+"金額不足");
			}
		}
	}
	private void sold_trade_item(L1Trade trade,Account buyer,int count){
		if(trade.getItemCount() >= count){
			L1ItemInstance target_item = new L1ItemInstance();
			buyer.loadDwarfInventory();
			L1DwarfInventory dwarf = buyer.getWarehouse();
			target_item = dwarf.findItemId(trade.getItemId());
			if(target_item != null){
				if(target_item.isStackable()){
					target_item.setCount(target_item.getCount()+count);
					dwarf.updateItem(target_item);
				}else{
					target_item = ItemTable.getInstance().createItem(trade.getItemId());
					target_item.setBless(trade.getBless());
					target_item.setEnchantLevel(trade.getEnchantlvl());
					dwarf.insertItem(target_item);
				}
			}else{
				target_item = ItemTable.getInstance().createItem(trade.getItemId());
				target_item.setBless(trade.getBless());
				target_item.setEnchantLevel(trade.getEnchantlvl());
				dwarf.insertItem(target_item);
			}
			if(trade.getItemCount() == count){
				trade.item_sold();
			}else if(trade.getItemCount() > count){
				trade.upd_trade(trade.getItemCount() - count);
			}
			trade_upd_money(trade,buyer,count);
		}
	}
	private void sold_web_item(Account buyer,int item_id,int count){
		L1ItemInstance target_item = new L1ItemInstance();
		buyer.loadDwarfInventory();
		L1DwarfInventory dwarf = buyer.getWarehouse();
		target_item = ItemTable.getInstance().createItem(item_id);
		if(target_item.isStackable()){
			L1ItemInstance item_in_dwarf = dwarf.findItemId(item_id);
			if(item_in_dwarf != null){
				System.out.println("has count:"+item_in_dwarf.getCount());
				item_in_dwarf.setCount(item_in_dwarf.getCount()+count);
				dwarf.updateItem(item_in_dwarf);
			}else{
				dwarf.insertItem(target_item);
				dwarf.loadItems();
				target_item.setCount(count);
				dwarf.updateItem(target_item);
			}
		}else{
			for(int i=0;i<count;i++){
				System.out.println("add"+i);
				dwarf.insertItem(target_item);
				target_item = ItemTable.getInstance().createItem(item_id);
			}
		}
		dwarf.loadItems();
		L1WebShop webshop = new L1WebShop();
		int money[] = webshop.calc_money(0, ShopTable.getInstance().getItemPrice(item_id));
		web_upd_money(buyer,money[0],money[1],count);
		
	}
	private static String addErrorMsg;
	private boolean add_trade_item(String acc,int objid,int count,int e_money,int money){
		boolean result = true;
		if(_account == null){
			_account = Account.load(acc);
		}
		if(_account.isAccountWarehouseLock()){
			result = false;
			addErrorMsg = "倉庫使用中";
		}else{
			_account.loadDwarfInventory();
			L1DwarfInventory warehouse = _account.getWarehouse();
			L1ItemInstance item = warehouse.getItem(objid);
			if(item == null){
				result = false;
				addErrorMsg = "倉庫中找無指定的道具";
			}else{
				if(item.getCount() < count){
					result = false;
					addErrorMsg = "指定販賣的數量大於您擁有的數量";
				}else{
					if(item.getCount() == count){
						warehouse.deleteItem(item);
					}else{
						item.setCount(item.getCount() - count);
						warehouse.updateItem(item);
					}
					insert_trade(objid, item, count, e_money, money);
					_account.updAccountWarehouse();
				}
			}
		}
		return result;
	}
	public void upd_both_warehouse(Account buyer,Account seller){
		buyer.updAccountWarehouse();
		seller.updAccountWarehouse();
	}
	private String trade(String buyer_acc,String trade_id,int count){
		String msg = "交易失敗，可能的原因如下：,";
		Account buyer = Account.load(buyer_acc);
		L1Trade trade = new L1Trade(Integer.parseInt(trade_id));
		trade.initialize();
		if(!trade.check_trade(count, buyer)){
			ArrayList<String> error = trade.getErrorMsg();
			for(int i=0;i<error.size();i++){
				msg += error.get(i)+",";
			}
		}else{
			sold_trade_item(trade,buyer,count);
			Account seller = Account.load(trade.getSellAcc());
			upd_both_warehouse(buyer,seller);
			msg = "交易成功";
		}
		return msg;
	}
	private String buy(String acc,int item_id,int count){
		String msg = "購買失敗，可能的原因如下：,";
		Account buyer = Account.load(acc);
		L1WebShop webshop = new L1WebShop();
		if(webshop._forbidden_list.contains(item_id)){
			msg += "此物品被禁止販售。";
		}else if(!webshop.check_trade( buyer,ShopTable.getInstance().getItemPrice(item_id), count)){
			ArrayList<String> error = webshop.getErrorMsg();
			for(int i=0;i<error.size();i++){
				msg += error.get(i)+",";
			}
		}else{
			sold_web_item(buyer,item_id,count);
			buyer.updAccountWarehouse();
			msg = "購買成功";
		}
		return msg;
	}
	@Override
	public void run() {
		try{
			if(_status>0){
				_out.write(_return,0,_return.length());
				_out.flush();
				_status = 0;
				_return = "";
			}
			String src;
			if(_status < 1){
				while((src = _in.readLine())!=null){
					String var1 = src.substring(0,src.indexOf("("));
					String var2 = src.substring(src.indexOf("(")+1, src.indexOf(")"));
					//get warehouse
					if(var1.equalsIgnoreCase("getwarehouse")){
						_status = 1;
						_account = Account.load(var2);
						if(!_account.is_dwarf_load()){
							_account.loadDwarfInventory();
						}
						List<L1ItemInstance> item_list = _account.getWarehouse().getItems();
						for(int i=0;i<item_list.size();i++){
							L1ItemInstance item = item_list.get(i);
							if(item.getItem().isTradable()){
								_return += item.getId() + ":";
								if(item.getEnchantLevel()>0){
									_return += "+" + item.getEnchantLevel()+item.getName();
								}else{
									_return += item.getName();
								}
								_return += ":"+item.getCount()+",";
							}
						}
					}else if(var1.equalsIgnoreCase("trade")){
						_status = 2;
						String temp[] = var2.split(",");
						String buyer_acc = temp[0];
						String trade_id = temp[1];
						int count = Integer.parseInt(temp[2]);
						_return = trade(buyer_acc,trade_id,count);
					}else if(var1.equalsIgnoreCase("add_trade_item")){
						_status = 3;
						String temp[] = var2.split(",");
						String result = (add_trade_item(temp[0],Integer.parseInt(temp[1]),Integer.parseInt(temp[2]),Integer.parseInt(temp[3]),Integer.parseInt(temp[4])))?"新增成功":"新增失敗，原因為："+addErrorMsg;
						System.out.println(result);
						_return = result;
					}else if(var1.equalsIgnoreCase("buy")){//buy,acc,id,cnt
						_status = 4;
						String temp[] = var2.split(",");
						String buyer_acc = temp[0];
						int item_id = Integer.parseInt(temp[1]);
						int count = Integer.parseInt(temp[2]);
						_return = buy(buyer_acc,item_id,count);
					}else{
						System.out.println("fail : "+var1);
					}
					if(src.contains("close")){
						break;
					}
				}
			}
			close();
		}catch(Exception e){
			System.out.println(e.toString());
		}
		return;
	}

	public void close() throws IOException {
		_csocket.close();
	}

	public void setActiveChar(L1PcInstance pc) {
		_activeChar = pc;
	}

	public L1PcInstance getActiveChar() {
		return _activeChar;
	}

	public void setAccount(Account account) {
		_account = account;
	}

	public Account getAccount() {
		return _account;
	}

	public String getAccountName() {
		if (_account == null) {
			return null;
		}
		return _account.getName();
	}

	@Override
	public void sendPacket(ServerBasePacket packet) {
		// TODO Auto-generated method stub
		
	}
}
