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

import java.text.DecimalFormat;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_AddItem;
import l1j.server.server.serverpackets.S_CharVisualUpdate;
import l1j.server.server.serverpackets.S_DeleteInventoryItem;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_ItemColor;
import l1j.server.server.serverpackets.S_ItemStatus;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_ItemName;
import l1j.server.server.serverpackets.S_ItemAmount;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.storage.CharactersItemStorage;
import l1j.server.server.templates.L1Item;

public class L1PcInventory extends L1Inventory {
	//所有狀態儲存
	public static final int COL_ALL = 0;
	//~所有狀態儲存
	//屬性強化捲軸
	public static final int COL_ATTRIBUTE = 2048;
	public static final int COL_ATTRIBUTELVL = 1024;
	//~屬性強化捲軸
	//飾品強化卷軸
	public static final int COL_FIREMR = 137;
	public static final int COL_WATERMR = 136;
	public static final int COL_WINDMR = 135;
	public static final int COL_EARTHMR = 134;
	public static final int COL_ADDSP = 133;
	public static final int COL_ADDHP = 132;
	public static final int COL_ADDMP = 131;
	public static final int COL_HPR = 130;
	public static final int COL_MPR = 129;
	//飾品強化卷軸
	//武器吸血吸魔魔法卷軸 by eric1300460
	public static final int COL_GETHP = 138;
	public static final int COL_GETMP = 139;
	//~武器吸血吸魔魔法卷軸 by eric1300460
	private static final long serialVersionUID = 1L;

	private static Logger _log = Logger
			.getLogger(L1PcInventory.class.getName());

	private static final int MAX_SIZE = 180;

	private final L1PcInstance _owner; // 所有者プレイヤー

	private int _arrowId; // 優先して使用されるアローのItemID

	private int _stingId; // 優先して使用されるスティングのItemID

	public L1PcInventory(L1PcInstance owner) {
		_owner = owner;
		_arrowId = 0;
		_stingId = 0;
	}

	public L1PcInstance getOwner() {
		return _owner;
	}
	
	// 240段階のウェイトを返す
	public int getWeight240() {
		return calcWeight240(getWeight());
	}

	// 240段階のウェイトを算出する
	public int calcWeight240(int weight) {
		int weight240 = 0;
		if (Config.RATE_WEIGHT_LIMIT != 0) {
			double maxWeight = _owner.getMaxWeight();
			if (weight > maxWeight) {
				weight240 = 240;
			} else {
				double wpTemp = (weight * 100 / maxWeight) * 240.00 / 100.00;
				DecimalFormat df = new DecimalFormat("00.##");
				df.format(wpTemp);
				wpTemp = Math.round(wpTemp);
				weight240 = (int) (wpTemp);
			}
		} else { // ウェイトレートが０なら重量常に０
			weight240 = 0;
		}
		return weight240;
	}

	@Override
	public int checkAddItem(L1ItemInstance item, int count) {
		return checkAddItem(item, count, true);
	}

	public int checkAddItem(L1ItemInstance item, int count, boolean message) {
		if (item == null) {
			return -1;
		}
		if (getSize() > MAX_SIZE
				|| (getSize() == MAX_SIZE && (!item.isStackable() || !checkItem(item
						.getItem().getItemId())))) { // 容量確認
			if (message) {
				sendOverMessage(263); // \f1一人のキャラクターが持って歩けるアイテムは最大180個までです。
			}
			return SIZE_OVER;
		}

		int weight = getWeight() + item.getItem().getWeight() * count / 1000 + 1;
		if (weight < 0 || (item.getItem().getWeight() * count / 1000) < 0) {
			if (message) {
				sendOverMessage(82); // アイテムが重すぎて、これ以上持てません。
			}
			return WEIGHT_OVER;
		}
		if (calcWeight240(weight) >= 240) {
			if (message) {
				sendOverMessage(82); // アイテムが重すぎて、これ以上持てません。
			}
			return WEIGHT_OVER;
		}

		L1ItemInstance itemExist = findItemId(item.getItemId());
		if (itemExist != null && (itemExist.getCount() + count) > MAX_AMOUNT) {
			if (message) {
				getOwner().sendPackets(new S_ServerMessage(166,
						"所持有的金幣",
						"超過20億。")); // \f1%0が%4%1%3%2
			}
			return AMOUNT_OVER;
		}
		
		return OK;
	}

	public void sendOverMessage(int message_id) {
		_owner.sendPackets(new S_ServerMessage(message_id));
	}

	// ＤＢのcharacter_itemsの読込
	@Override
	public void loadItems() {
		try {
			CharactersItemStorage storage = CharactersItemStorage.create();

			for (L1ItemInstance item : storage.loadItems(_owner.getId())) {
				_items.add(item);

				if (item.isEquipped()) {
					item.setEquipped(false);
					setEquipped(item, true, true, false);
				}
				if (item.getItem().getType2() == 0 && item.getItem()
						.getType() == 2) { // light系アイテム
					item.setRemainingTime(item.getItem().getLightFuel());
				}
				L1World.getInstance().storeObject(item);
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	// ＤＢのcharacter_itemsへ登録
	@Override
	public void insertItem(L1ItemInstance item) {
		_owner.sendPackets(new S_AddItem(item));
		if (item.getItem().getWeight() != 0) {
			_owner.sendPackets(
					new S_PacketBox(S_PacketBox.WEIGHT, getWeight240()));
		}
		try {
			CharactersItemStorage storage = CharactersItemStorage.create();
			storage.storeItem(_owner.getId(), item);
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	public static final int COL_BLESS = 512;

	public static final int COL_REMAINING_TIME = 256;

	public static final int COL_CHARGE_COUNT = 128;

	public static final int COL_ITEMID = 64;

	public static final int COL_DELAY_EFFECT = 32;

	public static final int COL_COUNT = 16;

	public static final int COL_EQUIPPED = 8;

	public static final int COL_ENCHANTLVL = 4;

	public static final int COL_IS_ID = 2;

	public static final int COL_DURABILITY = 1;

	@Override
	public void updateItem(L1ItemInstance item) {
		updateItem(item, COL_COUNT);
		if (item.getItem().isToBeSavedAtOnce()) {
			saveItem(item, COL_COUNT);
		}
	}

	/**
	 * インベントリ内のアイテムの状態を更新する。
	 * 
	 * @param item -
	 *            更新対象のアイテム
	 * @param column -
	 *            更新するステータスの種類
	 */
	@Override
	public void updateItem(L1ItemInstance item, int column) {
		// TODO 飾品強化卷軸
		switch (column) {
		case COL_FIREMR:
			_owner.sendPackets(new S_ItemStatus(item));
			break;
		case COL_WATERMR:
			_owner.sendPackets(new S_ItemStatus(item));
			break;
		case COL_EARTHMR:
			_owner.sendPackets(new S_ItemStatus(item));
			break;
		case COL_WINDMR:
			_owner.sendPackets(new S_ItemStatus(item));
			break;
		case COL_ADDSP:
			_owner.sendPackets(new S_ItemStatus(item));
			break;
		case COL_ADDHP:
			_owner.sendPackets(new S_ItemStatus(item));
			break;
		case COL_ADDMP:
			_owner.sendPackets(new S_ItemStatus(item));
			break;
		case COL_HPR:
			_owner.sendPackets(new S_ItemStatus(item));
			break;
		case COL_MPR:
			_owner.sendPackets(new S_ItemStatus(item));
			break;
		// 飾品強化卷軸
		// 武器吸血吸魔魔法卷軸 by eric1300460
		case COL_GETHP:
			_owner.sendPackets(new S_ItemStatus(item));
			break;
		case COL_GETMP:
			_owner.sendPackets(new S_ItemStatus(item));
			break;
		// ~武器吸血吸魔魔法卷軸 by eric1300460
		// 屬性強化捲軸
		// add 即時更新
		case COL_ATTRIBUTE:
			_owner.sendPackets(new S_ItemStatus(item));
			break;
		case COL_ATTRIBUTELVL:
			_owner.sendPackets(new S_ItemStatus(item));
			break;
		// end 2008.11.23
		// ~屬性強化捲軸
		case COL_BLESS: // 祝福・封印
			_owner.sendPackets(new S_ItemColor(item));
			break;
		case COL_REMAINING_TIME: // 使用可能な残り時間
			_owner.sendPackets(new S_ItemName(item));
			break;
		case COL_CHARGE_COUNT: // チャージ数
			_owner.sendPackets(new S_ItemName(item));
			break;
		case COL_ITEMID: // 別のアイテムになる場合(便箋を開封したときなど)
			_owner.sendPackets(new S_ItemStatus(item));
			_owner.sendPackets(new S_ItemColor(item));
			_owner.sendPackets(new S_PacketBox(S_PacketBox.WEIGHT,
					getWeight240()));
			break;
		case COL_DELAY_EFFECT: // 効果ディレイ
			break;
		case COL_COUNT: // カウント
			_owner.sendPackets(new S_ItemAmount(item));

			int weight = item.getWeight();
			if (weight != item.getLastWeight()) {
				item.setLastWeight(weight);
				_owner.sendPackets(new S_ItemStatus(item));
			} else {
				_owner.sendPackets(new S_ItemName(item));
			}
			if (item.getItem().getWeight() != 0) {
				// XXX 240段階のウェイトが変化しない場合は送らなくてよい
				_owner.sendPackets(new S_PacketBox(S_PacketBox.WEIGHT,
						getWeight240()));
			}
			break;
		case COL_EQUIPPED: // 装備状態
			_owner.sendPackets(new S_ItemName(item));
			break;
		case COL_ENCHANTLVL: // エンチャント
			_owner.sendPackets(new S_ItemStatus(item));
			break;
		case COL_IS_ID: // 確認状態
			_owner.sendPackets(new S_ItemStatus(item));
			_owner.sendPackets(new S_ItemColor(item));
			break;
		case COL_DURABILITY: // 耐久性
			_owner.sendPackets(new S_ItemStatus(item));
			break;
		}
	}

	//更新所有狀態
	public void updateAll(L1ItemInstance item,CharactersItemStorage storage){
        try {
			storage.updateItemAttribute(item); 
			storage.updateItemAttributeLevel(item); 
			storage.updateFireMr(item);
			storage.updateWaterMr(item);
			storage.updateEarthMr(item);
			storage.updateWindMr(item);
			storage.updateaddSp(item);;
			storage.updateaddHp(item);
			storage.updateaddMp(item);
			storage.updateHpr(item);
			storage.updateMpr(item);
			storage.updateGetHp(item);
			storage.updateGetMp(item);
			storage.updateItemBless(item);
			storage.updateItemRemainingTime(item);
			storage.updateItemChargeCount(item);
			storage.updateItemId(item);
			storage.updateItemDelayEffect(item);
			storage.updateItemCount(item);
			storage.updateItemEquipped(item);
			storage.updateItemEnchantLevel(item);
			storage.updateItemIdentified(item);
			storage.updateItemDurability(item);
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	//~更新所有狀態
	
	/**
	 * インベントリ内のアイテムの状態をDBに保存する。
	 * 
	 * @param item -
	 *            更新対象のアイテム
	 * @param column -
	 *            更新するステータスの種類
	 */
	public void saveItem(L1ItemInstance item, int column) {
		/*
		if (column == 0) {
			return;
		}
		*/
		try {
			CharactersItemStorage storage = CharactersItemStorage.create();
			//屬性強化捲軸
			switch(column){
			case COL_ATTRIBUTE: // 屬性 
	            storage.updateItemAttribute(item); 
	        break; 

			case COL_ATTRIBUTELVL: // 屬性階級 
	            storage.updateItemAttributeLevel(item); 
	        break; 
	        //~屬性強化捲軸
			//飾品強化卷軸
			case COL_FIREMR:
				storage.updateFireMr(item);
			break;
			case COL_WATERMR:
				storage.updateWaterMr(item);
			break;
			case COL_EARTHMR:
				storage.updateEarthMr(item);
			break;
			case COL_WINDMR:
				storage.updateWindMr(item);
			break;
			case COL_ADDSP:
				storage.updateaddSp(item);;
			break;
			case COL_ADDHP:
				storage.updateaddHp(item);
			break;
			case COL_ADDMP:
				storage.updateaddMp(item);
			break;
			case COL_HPR:
				storage.updateHpr(item);
			break;
			case COL_MPR:
				storage.updateMpr(item);
			break;
			//飾品強化卷軸
			//武器吸血吸魔魔法卷軸 by eric1300460
			case COL_GETHP:
				storage.updateGetHp(item);
			break;
			case COL_GETMP:
				storage.updateGetMp(item);
			break;
			//~武器吸血吸魔魔法卷軸 by eric1300460
			case COL_BLESS: // 祝福・封印
				storage.updateItemBless(item);
			break;
			case COL_REMAINING_TIME: // 使用可能な残り時間
				storage.updateItemRemainingTime(item);
			break;
			case COL_CHARGE_COUNT: // チャージ数
				storage.updateItemChargeCount(item);
			break;
			case COL_ITEMID: // 別のアイテムになる場合(便箋を開封したときなど)
				storage.updateItemId(item);
			break;
			case COL_DELAY_EFFECT: // 効果ディレイ
				storage.updateItemDelayEffect(item);
			break;
			case COL_COUNT: // カウント
				storage.updateItemCount(item);
			break;
			case COL_EQUIPPED: // 装備状態
				storage.updateItemEquipped(item);
			break;
			case COL_ENCHANTLVL: // エンチャント
				storage.updateItemEnchantLevel(item);
			break;
			case COL_IS_ID: // 確認状態
				storage.updateItemIdentified(item);
			break;
			case COL_DURABILITY: // 耐久性
				storage.updateItemDurability(item);
			break;
			case COL_ALL: //所有狀態
				updateAll(item, storage);
			break;
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	// ＤＢのcharacter_itemsから削除
	@Override
	public void deleteItem(L1ItemInstance item) {
		try {
			CharactersItemStorage storage = CharactersItemStorage.create();

			storage.deleteItem(item);
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		if (item.isEquipped()) {
			setEquipped(item, false);
		}
		_owner.sendPackets(new S_DeleteInventoryItem(item));
		_items.remove(item);
		if (item.getItem().getWeight() != 0) {
			_owner.sendPackets(
					new S_PacketBox(S_PacketBox.WEIGHT, getWeight240()));
		}
	}

	// アイテムを装着脱着させる（L1ItemInstanceの変更、補正値の設定、character_itemsの更新、パケット送信まで管理）
	public void setEquipped(L1ItemInstance item, boolean equipped) {
		setEquipped(item, equipped, false, false);
	}
	
	public void setEquipped(L1ItemInstance item, boolean equipped, boolean loaded, boolean changeWeapon) {
		if (item.isEquipped() != equipped) { // 設定値と違う場合だけ処理
			L1Item temp = item.getItem();
			if (equipped) { // 装着
			//TODO 判斷裝備穿著數量 by elfooxx
                int count = 0 ; 
               if ( temp.getType2() == 2) { 
                for ( L1ItemInstance hasEquip : _owner.getEquipSlot().getArmors() ) { 
                  // 如果要穿的裝備 跟已經穿上的種類一樣 計算穿上的數量  
                  if ( temp.getType() == hasEquip.getItem().getType() ) 
                    count ++ ; 
                  // 9為戒指 可以裝兩個  
                  if ( ( temp.getType() == 9 && count >= 2 ) || ( temp.getType() != 9 && count >= 1 ) ) {   
                    _owner.getNetConnection().sendPacket(new S_Disconnect()); // 裝備出異常　斷線 
                    return;
                  } // if 裝備數量額滿 裝備出異常　斷線　return;   
                } // for 已經裝備的物品 
                } 			// if temp is an armor    
             // elfooxx 判斷裝備穿著數量 End
               
				item.setEquipped(true);
				_owner.getEquipSlot().set(item);
			} else { // 脱着
				if (!loaded) {
					// インビジビリティクローク バルログブラッディクローク装備中でインビジ状態の場合はインビジ状態の解除
					if (temp.getItemId() == 20077 || temp.getItemId() == 20062 || temp.getItemId() == 120077) {
						if (_owner.isInvisble()) {
							_owner.delInvis();
							return;
						}
					}
				}
				
				item.setEquipped(false);
				_owner.getEquipSlot().remove(item);
			}
			if (!loaded) { // 最初の読込時はＤＢパケット関連の処理はしない
				// XXX:意味のないセッター
				_owner.setCurrentHp(_owner.getCurrentHp());
				_owner.setCurrentMp(_owner.getCurrentMp());
				updateItem(item, COL_EQUIPPED);
				_owner.sendPackets(new S_OwnCharStatus(_owner));
				if (temp.getType2() == 1 && changeWeapon == false) { // 武器の場合はビジュアル更新。ただし、武器の持ち替えで武器を脱着する時は更新しない
					_owner.sendPackets(new S_CharVisualUpdate(_owner));
					_owner.broadcastPacket(new S_CharVisualUpdate(_owner));
				}
			}
		}
	}

	// 特定のアイテムを装備しているか確認
	public boolean checkEquipped(int id) {
		for (Object itemObject : _items) {
			L1ItemInstance item = (L1ItemInstance) itemObject;
			if (item.getItem().getItemId() == id && item.isEquipped()) {
				return true;
			}
		}
		return false;
	}

	// 特定のアイテムを全て装備しているか確認（セットボーナスがあるやつの確認用）
	public boolean checkEquipped(int[] ids) {
		for (int id : ids) {
			if (!checkEquipped(id)) {
				return false;
			}
		}
		return true;
	}

	// 特定のタイプのアイテムを装備している数
	public int getTypeEquipped(int type2, int type) {
		int equipeCount = 0;
		for (Object itemObject : _items) {
			L1ItemInstance item = (L1ItemInstance) itemObject;
			if (item.getItem().getType2() == type2
					&& item.getItem().getType() == type && item.isEquipped()) {
				equipeCount++;
			}
		}
		return equipeCount;
	}

	// 装備している特定のタイプのアイテム
	public L1ItemInstance getItemEquipped(int type2, int type) {
		L1ItemInstance equipeitem = null;
		for (Object itemObject : _items) {
			L1ItemInstance item = (L1ItemInstance) itemObject;
			if (item.getItem().getType2() == type2
					&& item.getItem().getType() == type && item.isEquipped()) {
				equipeitem = item;
				break;
			}
		}
		return equipeitem;
	}

	// 装備しているリング
	public L1ItemInstance[] getRingEquipped() {
		L1ItemInstance equipeItem[] = new L1ItemInstance[2];
		int equipeCount = 0;
		for (Object itemObject : _items) {
			L1ItemInstance item = (L1ItemInstance) itemObject;
			if (item.getItem().getType2() == 2
					&& item.getItem().getType() == 9 && item.isEquipped()) {
				equipeItem[equipeCount] = item;
				equipeCount++;
				if (equipeCount == 2) {
					break;
				}
			}
		}
		return equipeItem;
	}

	// 変身時に装備できない装備を外す
	public void takeoffEquip(int polyid) {
		takeoffWeapon(polyid);
		takeoffArmor(polyid);
	}

	// 変身時に装備できない武器を外す
	private void takeoffWeapon(int polyid) {
		if (_owner.getWeapon() == null) { // 素手
			return;
		}

		boolean takeoff = false;
		int weapon_type = _owner.getWeapon().getItem().getType();
		// 装備出来ない武器を装備してるか？
		takeoff = !L1PolyMorph.isEquipableWeapon(polyid, weapon_type);

		if (takeoff) {
			setEquipped(_owner.getWeapon(), false, false, false);
		}
	}

	// 変身時に装備できない防具を外す
	private void takeoffArmor(int polyid) {
		L1ItemInstance armor = null;

		// ヘルムからイヤリングまでチェックする
		for (int type = 0; type <= 12; type++) {
			// 装備していて、装備不可の場合は外す
			if (getTypeEquipped(2, type) != 0
					&& !L1PolyMorph.isEquipableArmor(polyid, type)) {
				if (type == 9) { // リングの場合は、両手分外す
					armor = getItemEquipped(2, type);
					if (armor != null) {
						setEquipped(armor, false, false, false);
					}
					armor = getItemEquipped(2, type);
					if (armor != null) {
						setEquipped(armor, false, false, false);
					}
				} else {
					armor = getItemEquipped(2, type);
					if (armor != null) {
						setEquipped(armor, false, false, false);
					}
				}
			}
		}
	}

	// 使用するアローの取得
	public L1ItemInstance getArrow() {
		return getBullet(0);
	}

	// 使用するスティングの取得
	public L1ItemInstance getSting() {
		return getBullet(15);
	}

	private L1ItemInstance getBullet(int type) {
		L1ItemInstance bullet;
		int priorityId = 0;
		if (type == 0) {
			priorityId = _arrowId; // アロー
		}
		if (type == 15) {
			priorityId = _stingId; // スティング
		}
		if (priorityId > 0) // 優先する弾があるか
		{
			bullet = findItemId(priorityId);
			if (bullet != null) {
				return bullet;
			} else // なくなっていた場合は優先を消す
			{
				if (type == 0) {
					_arrowId = 0;
				}
				if (type == 15) {
					_stingId = 0;
				}
			}
		}

		for (Object itemObject : _items) // 弾を探す
		{
			bullet = (L1ItemInstance) itemObject;
			if (bullet.getItem().getType() == type) {
				if (type == 0) {
					_arrowId = bullet.getItem().getItemId(); // 優先にしておく
				}
				if (type == 15) {
					_stingId = bullet.getItem().getItemId(); // 優先にしておく
				}
				return bullet;
			}
		}
		return null;
	}

	// 優先するアローの設定
	public void setArrow(int id) {
		_arrowId = id;
	}

	// 優先するスティングの設定
	public void setSting(int id) {
		_stingId = id;
	}

	// 装備によるＨＰ自然回復補正
	public int hpRegenPerTick() {
		int hpr = 0;
		for (Object itemObject : _items) {
			L1ItemInstance item = (L1ItemInstance) itemObject;
			if (item.isEquipped()) {
				hpr += item.getItem().get_addhpr()+ item.getHpr();//飾品強化卷軸
			}
		}
		return hpr;
	}

	// 装備によるＭＰ自然回復補正
	public int mpRegenPerTick() {
		int mpr = 0;
		for (Object itemObject : _items) {
			L1ItemInstance item = (L1ItemInstance) itemObject;
			if (item.isEquipped()) {
				mpr += item.getItem().get_addmpr()+ item.getMpr();//飾品強化卷軸
			}
		}
		return mpr;
	}

	public L1ItemInstance CaoPenalty() {
		Random random = new Random();
		int rnd = random.nextInt(_items.size());
		L1ItemInstance penaltyItem = _items.get(rnd);
		if (penaltyItem.getItem().getItemId() == L1ItemId.ADENA // アデナ、トレード不可のアイテムは落とさない
				|| !penaltyItem.getItem().isTradable()) {
			return null;
		}
		Object[] petlist = _owner.getPetList().values().toArray();
		for (Object petObject : petlist) {
			if (petObject instanceof L1PetInstance) {
				L1PetInstance pet = (L1PetInstance) petObject;
				if (penaltyItem.getId() == pet.getItemObjId()) {
					return null;
				}
			}
		}
		setEquipped(penaltyItem, false);
		return penaltyItem;
	}
}
