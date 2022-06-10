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
package l1j.server.server.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.templates.L1Item;
import l1j.server.server.utils.SQLUtil;

public class L1DwarfForClanInventory extends L1Inventory {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger _log = Logger.getLogger(L1DwarfForClanInventory.class
			.getName());
	private final L1Clan _clan;

	public L1DwarfForClanInventory(L1Clan clan) {
		_clan = clan;
	}

	// ＤＢのcharacter_itemsの読込
	@Override
	public synchronized void loadItems() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("SELECT * FROM clan_warehouse WHERE clan_name = ?");
			pstm.setString(1, _clan.getClanName());
			rs = pstm.executeQuery();
			while (rs.next()) {
				L1ItemInstance item = new L1ItemInstance();
				int objectId = rs.getInt("id");
				item.setId(objectId);
				int itemId = rs.getInt("item_id");
				L1Item itemTemplate = ItemTable.getInstance().getTemplate(
						itemId);
				if (itemTemplate == null) {
					throw new NullPointerException("item_id=" + itemId
							+ " not found");
				}
				item.setItem(itemTemplate);
				item.setCount(rs.getInt("count"));
				item.setEquipped(false);
				item.setEnchantLevel(rs.getInt("enchantlvl"));
				item.setIdentified(rs.getInt("is_id") != 0 ? true : false);
				item.set_durability(rs.getInt("durability"));
				item.setChargeCount(rs.getInt("charge_count"));
				item.setRemainingTime(rs.getInt("remaining_time"));
				item.setLastUsed(rs.getTimestamp("last_used"));
				item.setBless(rs.getInt("bless"));
				//屬性強化捲軸
				item.setAttribute(rs.getInt("attr")); 
				item.setAttributeLevel(rs.getInt("attrlvl"));
				//~屬性強化捲軸
				//飾品強化卷軸
				item.setFireMr(rs.getInt("firemr"));
				item.setWaterMr(rs.getInt("watermr"));
				item.setEarthMr(rs.getInt("earthmr"));
				item.setWindMr(rs.getInt("windmr"));
				item.setaddSp(rs.getInt("addsp"));
				item.setaddHp(rs.getInt("addhp"));
				item.setaddMp(rs.getInt("addmp"));
				item.setHpr(rs.getInt("hpr"));
				item.setMpr(rs.getInt("mpr"));
				//飾品強化卷軸
				//武器吸血吸魔魔法卷軸 by eric1300460
				item.setgetHp(rs.getInt("gethp"));
				item.setgetMp(rs.getInt("getmp"));
				//~武器吸血吸魔魔法卷軸 by eric1300460
				_items.add(item);
				L1World.getInstance().storeObject(item);
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	// ＤＢのclan_warehouseへ登録
	@Override
	public synchronized void insertItem(L1ItemInstance item) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			//屬性強化捲軸
			pstm = con
					.prepareStatement("INSERT INTO clan_warehouse SET id = ?, clan_name = ?, item_id = ?, item_name = ?, count = ?, is_equipped=0, enchantlvl = ?, is_id= ?, durability = ?, charge_count = ?, remaining_time = ?, last_used = ?, attr = ?, attrlvl = ?, firemr = ?, watermr = ?, earthmr = ?, windmr = ?, addsp = ?, addhp = ?, addmp = ?, hpr = ?, mpr = ?, gethp = ?, getmp = ?, bless = ?");
			pstm.setInt(1, item.getId());
			pstm.setString(2, _clan.getClanName());
			pstm.setInt(3, item.getItemId());
			pstm.setString(4, item.getName());
			pstm.setInt(5, item.getCount());
			pstm.setInt(6, item.getEnchantLevel());
			pstm.setInt(7, item.isIdentified() ? 1 : 0);
			pstm.setInt(8, item.get_durability());
			pstm.setInt(9, item.getChargeCount());
			pstm.setInt(10, item.getRemainingTime());
			pstm.setTimestamp(11, item.getLastUsed());
			pstm.setInt(12, item.getAttribute()); 
			pstm.setInt(13, item.getAttributeLevel()); 
			//~屬性強化捲軸
			//飾品強化卷軸
			pstm.setInt(14, item.getFireMr());
			pstm.setInt(15, item.getWaterMr());
			pstm.setInt(16, item.getEarthMr());
			pstm.setInt(17, item.getWindMr());
			pstm.setInt(18, item.getaddSp());
			pstm.setInt(19, item.getaddHp());
			pstm.setInt(20, item.getaddMp());
			pstm.setInt(21, item.getHpr());
			pstm.setInt(22, item.getMpr());
			//飾品強化卷軸
			//武器吸血吸魔魔法卷軸 by eric1300460
			pstm.setInt(23, item.getgetHp());
			pstm.setInt(24, item.getgetMp());
			//~武器吸血吸魔魔法卷軸 by eric1300460
			pstm.setInt(25, item.getBless());
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	// ＤＢのclan_warehouseを更新
	@Override
	public synchronized void updateItem(L1ItemInstance item) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("UPDATE clan_warehouse SET count = ? WHERE id = ?");
			pstm.setInt(1, item.getCount());
			pstm.setInt(2, item.getId());
			pstm.execute();

		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	// ＤＢのclan_warehouseから削除
	@Override
	public synchronized void deleteItem(L1ItemInstance item) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("DELETE FROM clan_warehouse WHERE id = ?");
			pstm.setInt(1, item.getId());
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		_items.remove(_items.indexOf(item));
	}

	// DBのクラン倉庫のアイテムを全て削除(血盟解散時のみ使用)
	public synchronized void deleteAllItems() {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("DELETE FROM clan_warehouse WHERE clan_name = ?");
			pstm.setString(1, _clan.getClanName());
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

}
