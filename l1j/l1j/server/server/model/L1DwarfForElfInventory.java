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
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.templates.L1Item;
import l1j.server.server.utils.SQLUtil;

public class L1DwarfForElfInventory extends L1Inventory {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public L1DwarfForElfInventory(L1PcInstance owner) {
		_owner = owner;
	}

	// ＤＢのcharacter_itemsの読込
	@Override
	public void loadItems() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("SELECT * FROM character_elf_warehouse WHERE account_name = ?");
			pstm.setString(1, _owner.getAccountName());

			rs = pstm.executeQuery();

			while (rs.next()) {
				L1ItemInstance item = new L1ItemInstance();
				int objectId = rs.getInt("id");
				item.setId(objectId);
				L1Item itemTemplate = ItemTable.getInstance().getTemplate(
						rs.getInt("item_id"));
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

	// ＤＢのcharacter_elf_warehouseへ登録
	@Override
	public void insertItem(L1ItemInstance item) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			//屬性強化捲軸
			pstm = con
					.prepareStatement("INSERT INTO character_elf_warehouse SET id = ?, account_name = ?, item_id = ?, item_name = ?, count = ?, is_equipped=0, enchantlvl = ?, is_id = ?, durability = ?, charge_count = ?, remaining_time = ?, last_used = ?, attr = ?, attrlvl = ?, firemr = ?, watermr = ?, earthmr = ?, windmr = ?, addsp = ?, addhp = ?, addmp = ?, hpr = ?, mpr = ?, gethp = ?, getmp = ?, bless = ?");
			pstm.setInt(1, item.getId());
			pstm.setString(2, _owner.getAccountName());
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

	// ＤＢのcharacter_elf_warehouseを更新
	@Override
	public void updateItem(L1ItemInstance item) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("UPDATE character_elf_warehouse SET count = ? WHERE id = ?");
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

	// ＤＢのcharacter_elf_warehouseから削除
	@Override
	public void deleteItem(L1ItemInstance item) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("DELETE FROM character_elf_warehouse WHERE id = ?");
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

	private static Logger _log = Logger.getLogger(L1DwarfForElfInventory.class
			.getName());
	private final L1PcInstance _owner;
}
