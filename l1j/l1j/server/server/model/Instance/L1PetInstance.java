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

package l1j.server.server.model.Instance;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.Random;

import l1j.server.server.ActionCodes;
import l1j.server.server.IdFactory;
import l1j.server.server.datatables.ExpTable;
import l1j.server.server.datatables.PetTable;
import l1j.server.server.datatables.PetTypeTable;
import l1j.server.server.model.L1Attack;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1World;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_HPMeter;
import l1j.server.server.serverpackets.S_NpcChatPacket;
import l1j.server.server.serverpackets.S_PetMenuPacket;
import l1j.server.server.serverpackets.S_PetPack;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillHaste;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.templates.L1Pet;
import l1j.server.server.templates.L1PetType;
import static l1j.server.server.model.skill.L1SkillId.*;
//所有怪物都可抓進化
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.utils.IntRange;
import l1j.server.server.model.L1Object;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.datatables.BossSpawnTable;
import l1j.server.Config;
//~所有怪物都可抓進化

public class L1PetInstance extends L1NpcInstance {

	private static final long serialVersionUID = 1L;
	private static Random _random = new Random();

	// ターゲットがいない場合の処理
	@Override
	public boolean noTarget() {
		if (_currentPetStatus == 3) { // ● 休憩の場合
			return true;
		} else if (_currentPetStatus == 4) { // ● 配備の場合
			if (_petMaster != null
					&& _petMaster.getMapId() == getMapId()
					&& getLocation().getTileLineDistance(
							_petMaster.getLocation()) < 5) {
				int dir = targetReverseDirection(_petMaster.getX(), _petMaster
						.getY());
				dir = checkObject(getX(), getY(), getMapId(), dir);
				setDirectionMove(dir);
				setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
			} else { // 主人を見失うか５マス以上はなれたら休憩状態に
				_currentPetStatus = 3;
				return true;
			}
		} else if (_currentPetStatus == 5) { // ● 警戒の場合はホームへ
			if (Math.abs(getHomeX() - getX()) > 1
					|| Math.abs(getHomeY() - getY()) > 1) {
				int dir = moveDirection(getHomeX(), getHomeY());
				if (dir == -1) { // ホームが離れすぎてたら現在地がホーム
					setHomeX(getX());
					setHomeY(getY());
				} else {
					setDirectionMove(dir);
					setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
				}
			}
		} else if (_currentPetStatus == 7) { // ● ペットの笛で主人の元へ
			if (_petMaster != null
					&& _petMaster.getMapId() == getMapId()
					&& getLocation().getTileLineDistance(
							_petMaster.getLocation()) <= 1) {
				_currentPetStatus = 3;
				return true;
			}
			int locx = _petMaster.getX() + _random.nextInt(1);
			int locy = _petMaster.getY() + _random.nextInt(1);
			int dir = moveDirection(locx, locy);
			if (dir == -1) { // 主人を見失うかはなれたらその場で休憩状態に
				_currentPetStatus = 3;
				return true;
			}
			setDirectionMove(dir);
			setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
		} else if (_petMaster != null && _petMaster.getMapId() == getMapId()) { // ●
			// 主人を追尾
			if (getLocation().getTileLineDistance(_petMaster.getLocation()) > 2) {
				int dir = moveDirection(_petMaster.getX(), _petMaster.getY());
				/*if (dir == -1) { // 主人が離れすぎたら休憩状態に TODO 寵物防呆修正　by 阿傑
				_currentPetStatus = 3;
				return true;
				}*///end
				setDirectionMove(dir);
				setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
			}
		} /*else { // ● 主人を見失ったら休憩狀態に　寵物防呆修正　by 阿傑
		_currentPetStatus = 3;
		return true;
	}*///end
		return false;
	}

	// ペットを引き出した場合
	public L1PetInstance(L1Npc template, L1PcInstance master, L1Pet l1pet) {
		super(template);

		_petMaster = master;
		_itemObjId = l1pet.get_itemobjid();
		_type = PetTypeTable.getInstance().get(template.get_npcId());
		//所有怪物都可抓進化
		if(_type==null){
			setPetTypeByNpcId(template.get_npcId());
		}
		//~所有怪物都可抓進化
		// ステータスを上書き
		setId(l1pet.get_objid());
		setName(l1pet.get_name());
		setLevel(l1pet.get_level());
		// HPMPはMAXとする
		setMaxHp(l1pet.get_hp());
		setCurrentHpDirect(l1pet.get_hp());
		setMaxMp(l1pet.get_mp());
		setCurrentMpDirect(l1pet.get_mp());
		setExp(l1pet.get_exp());
		setExpPercent(ExpTable.getExpPercentage(l1pet.get_level(), l1pet
				.get_exp()));
		setLawful(l1pet.get_lawful());
		setTempLawful(l1pet.get_lawful());

		setMaster(master);
		setX(master.getX() + _random.nextInt(5) - 2);
		setY(master.getY() + _random.nextInt(5) - 2);
		setMap(master.getMapId());
		setHeading(5);
		setLightSize(template.getLightSize());

		_currentPetStatus = 3;

		L1World.getInstance().storeObject(this);
		L1World.getInstance().addVisibleObject(this);
		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(this)) {
			onPerceive(pc);
		}
		//TODO 寵物領出後獲得加速術1800秒  by 阿傑
		broadcastPacket(new S_SkillSound(getId(), 3104));
		broadcastPacket(new S_SkillHaste(getId(), 1, 0));
		setMoveSpeed(1);
		setSkillEffect(43, 1800000);
		//寵物領出後獲得加速術1800秒  end
		master.addPet(this);
	}

	// ペットをテイムした場合
	public L1PetInstance(L1NpcInstance target, L1PcInstance master, int itemid) {
		super(null);

		_petMaster = master;
		_itemObjId = itemid;
		_type = PetTypeTable.getInstance().get(
				target.getNpcTemplate().get_npcId());
		//所有怪物都可抓進化
		if(_type==null){
			setPetTypeByNpcId(target.getNpcTemplate().get_npcId());
		}
		//~所有怪物都可抓進化
		// ステータスを上書き
		setId(IdFactory.getInstance().nextId());
		setting_template(target.getNpcTemplate());
		setCurrentHpDirect(target.getCurrentHp());
		setCurrentMpDirect(target.getCurrentMp());
		setExp(750); // Lv.5のEXP
		setExpPercent(0);
		setLawful(0);
		setTempLawful(0);

		setMaster(master);
		setX(target.getX());
		setY(target.getY());
		setMap(target.getMapId());
		setHeading(target.getHeading());
		setLightSize(target.getLightSize());
		setPetcost(6);
		setInventory(target.getInventory());
		target.setInventory(null);

		_currentPetStatus = 3;
		
		//TODO 修正馴養後回血回魔by 阿傑
		stopHpRegeneration();
		if (getMaxHp() > getCurrentHp()) {
			startHpRegeneration();
		}
		stopMpRegeneration();
		if (getMaxMp() > getCurrentMp()) {
			startMpRegeneration();
		}
		//修正馴養後回血回血  end

		target.deleteMe();
		L1World.getInstance().storeObject(this);
		L1World.getInstance().addVisibleObject(this);
		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(this)) {
			onPerceive(pc);
		}

		master.addPet(this);
		PetTable.getInstance().storeNewPet(target, getId(), itemid);
	}

	// 攻撃でＨＰを減らすときはここを使用
	@Override
	public void receiveDamage(L1Character attacker, int damage) {
		if (getCurrentHp() > 0) {
			if (damage > 0) { // 回復の場合は攻撃しない。
				setHate(attacker, 0); // ペットはヘイト無し
				removeSkillEffect(FOG_OF_SLEEPING);
			}

			if (attacker instanceof L1PcInstance && damage > 0) {
				L1PcInstance player = (L1PcInstance) attacker;
				player.setPetTarget(this);
			}

			if (hasSkillEffect(JOY_OF_PAIN)
					&& getId() != attacker.getId()) {
				int nowDamage = getMaxHp() - getCurrentHp();
				if (nowDamage > 0) {
					if (attacker instanceof L1PcInstance) {
						L1PcInstance attackPc = (L1PcInstance) attacker;
						attackPc.sendPackets(new S_DoActionGFX(attackPc
								.getId(), ActionCodes.ACTION_Damage));
						attackPc.broadcastPacket(new S_DoActionGFX(attackPc
								.getId(), ActionCodes.ACTION_Damage));
						attackPc.receiveDamage(this, (int) (nowDamage / 5));
					} else if (attacker instanceof L1NpcInstance) {
						L1NpcInstance attackNpc = (L1NpcInstance) attacker;
						attackNpc.broadcastPacket(new S_DoActionGFX(attackNpc
								.getId(), ActionCodes.ACTION_Damage));
						attackNpc.receiveDamage(this, (int) (nowDamage / 5));
					}
				}
			}
			int newHp = getCurrentHp() - damage;
			if (newHp <= 0) {
				death(attacker);
			} else {
				setCurrentHp(newHp);
			}
		} else if (!isDead()) { // 念のため
			death(attacker);
		}
	}

	public synchronized void death(L1Character lastAttacker) {
		if (!isDead()) {
			setDead(true);
			setStatus(ActionCodes.ACTION_Die);
			setCurrentHp(0);

			getMap().setPassable(getLocation(), true);
			broadcastPacket(new S_DoActionGFX(getId(), ActionCodes.ACTION_Die));
		}
	}

	public void evolvePet(int new_itemobjid) {

		L1Pet l1pet = PetTable.getInstance().getTemplate(_itemObjId);
		if (l1pet == null) {
			return;
		}

		int newNpcId = _type.getNpcIdForEvolving();
		// 進化前のmaxHp,maxMpを退避
		int tmpMaxHp = getMaxHp();
		int tmpMaxMp = getMaxMp();

		transform(newNpcId);
		_type = PetTypeTable.getInstance().get(newNpcId);

		//TODO 寵物進化特效 by 阿傑
		broadcastPacket(new S_SkillSound(getId(), 2127));
		//寵物進化特效  end
		
		setLevel(1);
		// HPMPを元の半分にする
		setMaxHp(tmpMaxHp / 2);
		setMaxMp(tmpMaxMp / 2);
		setCurrentHpDirect(getMaxHp());
		setCurrentMpDirect(getMaxMp());
		setExp(0);
		setExpPercent(0);

		// インベントリを空にする
		getInventory().clearItems();

		// 古いペットをDBから消す
		PetTable.getInstance().deletePet(_itemObjId);

		// 新しいペットをDBに書き込む
		l1pet.set_itemobjid(new_itemobjid);
		l1pet.set_npcid(newNpcId);
		l1pet.set_name(getName());
		l1pet.set_level(getLevel());
		l1pet.set_hp(getMaxHp());
		l1pet.set_mp(getMaxMp());
		l1pet.set_exp(getExp());
		PetTable.getInstance().storeNewPet(this, getId(), new_itemobjid);

		_itemObjId = new_itemobjid;
	}
	//所有怪物都可抓進化
	
	public void setPetTypeByNpcId(int npcId) {
		L1NpcInstance npc = NpcTable.getInstance().newNpcInstance(npcId);
		IntRange hpRange = new IntRange(npc.getMaxHp()*Config.MIN_CE_LEVEL_UP_HP/100,npc.getMaxHp()*Config.MAX_CE_LEVEL_UP_HP/100);
		IntRange mpRange = new IntRange(npc.getMaxMp()*Config.MIN_CE_LEVEL_UP_MP/100,npc.getMaxMp()*Config.MAX_CE_LEVEL_UP_MP/100);
		int msgIds[] = {1088,1089,1090,1091,1092};//哈士奇
		_type= new L1PetType(npcId,npc.getName(),0,hpRange,mpRange,0,
				msgIds,1088);
	}
	
	public int evolvePetAll(int new_itemobjid) {
		L1Pet l1pet = PetTable.getInstance().getTemplate(_itemObjId);
		if (l1pet == null) {
			return 0;
		}
		int oldNpcId = l1pet.get_npcid();
		L1NpcInstance npc = NpcTable.getInstance().newNpcInstance(oldNpcId);
		Random random = new Random();
		int newNpcLevel= npc.getLevel()+random.nextInt(Config.RANDOM_CE_PET_LEVEL)+1;
		int newNpcId = NpcTable.getInstance().findNpcIdByLevel(newNpcLevel);
		if (newNpcId==0) {
			return 0;
		}
		//TODO 寵物進化特效 by 阿傑
		broadcastPacket(new S_SkillSound(getId(), 2127));
		//寵物進化特效  end
		// 進化前のmaxHp,maxMpを退避
		int tmpMaxHp = getMaxHp();
		int tmpMaxMp = getMaxMp();
		transform(newNpcId);
		setLevel(1);
		// HPMPを元の半分にする
		setMaxHp(tmpMaxHp * Config.EVOLVE_HPMP_SET / 100);
		setMaxMp(tmpMaxMp * Config.EVOLVE_HPMP_SET / 100);
		setCurrentHpDirect(getMaxHp());
		setCurrentMpDirect(getMaxMp());
		setExp(0);
		setExpPercent(0);
		// インベントリを空にする
		getInventory().clearItems();

		// 古いペットをDBから消す
		PetTable.getInstance().deletePet(_itemObjId);
		// 新しいペットをDBに書き込む
		l1pet.set_itemobjid(new_itemobjid);
		l1pet.set_npcid(newNpcId);
		l1pet.set_name(getName());
		l1pet.set_level(getLevel());
		l1pet.set_hp(getMaxHp());
		l1pet.set_mp(getMaxMp());
		l1pet.set_exp(getExp());
		PetTable.getInstance().storeNewPet(this, getId(), new_itemobjid);
		_itemObjId = new_itemobjid;
		return newNpcId;
	}
	//～所有怪物都可抓進化
	// 解放処理
	public void liberate() {
		L1MonsterInstance monster = new L1MonsterInstance(getNpcTemplate());
		monster.setId(IdFactory.getInstance().nextId());

		monster.setX(getX());
		monster.setY(getY());
		monster.setMap(getMapId());
		monster.setHeading(getHeading());
		monster.set_storeDroped(true);
		monster.setInventory(getInventory());
		setInventory(null);
		monster.setLevel(getLevel());
		monster.setMaxHp(getMaxHp());
		monster.setCurrentHpDirect(getCurrentHp());
		monster.setMaxMp(getMaxMp());
		monster.setCurrentMpDirect(getCurrentMp());

		_petMaster.getPetList().remove(getId());
		deleteMe();

		// DBとPetTableから削除し、ペットアミュも破棄
		_petMaster.getInventory().removeItem(_itemObjId, 1);
		PetTable.getInstance().deletePet(_itemObjId);

		L1World.getInstance().storeObject(monster);
		L1World.getInstance().addVisibleObject(monster);
		for (L1PcInstance pc : L1World.getInstance()
				.getRecognizePlayer(monster)) {
			onPerceive(pc);
		}
	}

	// ペットの持ち物を収集
	public void collect() {
		L1Inventory targetInventory = _petMaster.getInventory();
		List<L1ItemInstance> items = _inventory.getItems();
		int size = _inventory.getSize();
		for (int i = 0; i < size; i++) {
			L1ItemInstance item = items.get(0);
			if (item.isEquipped()) { // 装備中のペットアイテム
				continue;
			}
			if (_petMaster.getInventory().checkAddItem( // 容量重量確認及びメッセージ送信
					item, item.getCount()) == L1Inventory.OK) {
				_inventory.tradeItem(item, item.getCount(), targetInventory);
				_petMaster.sendPackets(new S_ServerMessage(143, getName(), item
						.getLogName())); // \f1%0が%1をくれました。
			} else { // 持てないので足元に落とす
				targetInventory = L1World.getInstance().getInventory(getX(),
						getY(), getMapId());
				_inventory.tradeItem(item, item.getCount(), targetInventory);
			}
		}
	}

	// リスタート時にDROPを地面に落とす
	public void dropItem() {
		L1Inventory targetInventory = L1World.getInstance().getInventory(
				getX(), getY(), getMapId());
		List<L1ItemInstance> items = _inventory.getItems();
		int size = _inventory.getSize();
		for (int i = 0; i < size; i++) {
			L1ItemInstance item = items.get(0);
			item.setEquipped(false);
			_inventory.tradeItem(item, item.getCount(), targetInventory);
		}
	}

	// ペットの笛を使った
	public void call() {
		int id = _type.getMessageId(L1PetType.getMessageNumber(getLevel()));
		if (id != 0) {
			broadcastPacket(new S_NpcChatPacket(this, "$" + id, 0));
		}

		setCurrentPetStatus(7); // 主人の近くで休憩状態
	}

	public void setTarget(L1Character target) {
		if (target != null
				&& (_currentPetStatus == 1 || _currentPetStatus == 2 || _currentPetStatus == 5)) {
			setHate(target, 0);
			if (!isAiRunning()) {
				startAI();
			}
		}
	}

	public void setMasterTarget(L1Character target) {
		if (target != null
				&& (_currentPetStatus == 1 || _currentPetStatus == 5)) {
			setHate(target, 0);
			if (!isAiRunning()) {
				startAI();
			}
		}
	}

	@Override
	public void onPerceive(L1PcInstance perceivedFrom) {
		perceivedFrom.addKnownObject(this);
		perceivedFrom.sendPackets(new S_PetPack(this, perceivedFrom)); // ペット系オブジェクト認識
		if (isDead()) {
			perceivedFrom.sendPackets(new S_DoActionGFX(getId(),
					ActionCodes.ACTION_Die));
		}
	}

	@Override
	public void onAction(L1PcInstance player) {
		L1Character cha = this.getMaster();
		L1PcInstance master = (L1PcInstance) cha;
		if (master.isTeleport()) { // テレポート処理中
			return;
		}
		if (getZoneType() == 1) { // 攻撃される側がセーフティーゾーン
			L1Attack attack_mortion = new L1Attack(player, this); // 攻撃モーション送信
			attack_mortion.action();
			return;
		}

		if (player.checkNonPvP(player, this)) {
			return;
		}

		L1Attack attack = new L1Attack(player, this);
		if (attack.calcHit()) {
			attack.calcDamage();
		}
		attack.action();
		attack.commit();
	}

	@Override
	public void onTalkAction(L1PcInstance player) {
		if (isDead()) {
			return;
		}
		if (_petMaster.equals(player)) {
			player.sendPackets(new S_PetMenuPacket(this, getExpPercent()));
			L1Pet l1pet = PetTable.getInstance().getTemplate(_itemObjId);
			// XXX ペットに話しかけるたびにDBに書き込む必要はない
			if (l1pet != null) {
				l1pet.set_exp(getExp());
				l1pet.set_level(getLevel());
				l1pet.set_hp(getMaxHp());
				l1pet.set_mp(getMaxMp());
				PetTable.getInstance().storePet(l1pet); // DBに書き込み
			}
		}
	}

	@Override
	public void onFinalAction(L1PcInstance player, String action) {
		int status = actionType(action);
		if (status == 0) {
			return;
		}
		if (status == 6) {
			liberate(); // ペットの解放
		} else {
			// 同じ主人のペットの状態をすべて更新
			Object[] petList = _petMaster.getPetList().values().toArray();
			for (Object petObject : petList) {
				if (petObject instanceof L1PetInstance) { // ペット
					L1PetInstance pet = (L1PetInstance) petObject;
					if (_petMaster != null && _petMaster.getLevel() >= pet
							.getLevel()) {
						pet.setCurrentPetStatus(status);
					} else {
						L1PetType type = PetTypeTable.getInstance().get(
								pet.getNpcTemplate().get_npcId());
						int id = type.getDefyMessageId();
						if (id != 0) {
							broadcastPacket(new S_NpcChatPacket(pet,
									"$" + id, 0));
						}
					}
				}
			}
		}
	}

	@Override
	public void onItemUse() {
		/*刪除if (!isActived()) {
		useItem(USEITEM_HASTE, 100); // １００％の確率でヘイストポーション使用
		}刪除*/
		//TODO 喝綠判斷變更 by 阿傑
		if (!hasSkillEffect(1001) && !hasSkillEffect(43)) {//補上加速術判斷 
		useItem(USEITEM_HASTE, 100); // １００％の確率でヘイストポーション使用
		}
		//喝綠判斷變更  end
		if (getCurrentHp() * 100 / getMaxHp() < 40) { // ＨＰが４０％きったら
			useItem(USEITEM_HEAL, 100); // １００％の確率で回復ポーション使用
		}
	}

	@Override
	public void onGetItem(L1ItemInstance item) {
		if (getNpcTemplate().get_digestitem() > 0) {
			setDigestItem(item);
		}
		Arrays.sort(healPotions);
		Arrays.sort(haestPotions);
		if (Arrays.binarySearch(healPotions, item.getItem().getItemId()) >= 0) {
			if (getCurrentHp() != getMaxHp()) {
				useItem(USEITEM_HEAL, 100);
			}
		} else if (Arrays
				.binarySearch(haestPotions, item.getItem().getItemId()) >= 0) {
			useItem(USEITEM_HASTE, 100);
		}
	}

	private int actionType(String action) {
		int status = 0;
		if (action.equalsIgnoreCase("aggressive")) { // 攻撃態勢
			status = 1;
		} else if (action.equalsIgnoreCase("defensive")) { // 防御態勢
			status = 2;
		} else if (action.equalsIgnoreCase("stay")) { // 休憩
			status = 3;
		} else if (action.equalsIgnoreCase("extend")) { // 配備
			status = 4;
		} else if (action.equalsIgnoreCase("alert")) { // 警戒
			status = 5;
		} else if (action.equalsIgnoreCase("dismiss")) { // 解散
			status = 6;
		} else if (action.equalsIgnoreCase("getitem")) { // 収集
			collect();
		}
		return status;
	}

	@Override
	public void setCurrentHp(int i) {
		int currentHp = i;
		if (currentHp >= getMaxHp()) {
			currentHp = getMaxHp();
		}
		setCurrentHpDirect(currentHp);

		if (getMaxHp() > getCurrentHp()) {
			startHpRegeneration();
		}

		if (_petMaster != null) {
			int HpRatio = 100 * currentHp / getMaxHp();
			L1PcInstance Master = _petMaster;
			Master.sendPackets(new S_HPMeter(getId(), HpRatio));
		}
	}

	@Override
	public void setCurrentMp(int i) {
		int currentMp = i;
		if (currentMp >= getMaxMp()) {
			currentMp = getMaxMp();
		}
		setCurrentMpDirect(currentMp);

		if (getMaxMp() > getCurrentMp()) {
			startMpRegeneration();
		}
	}

	public void setCurrentPetStatus(int i) {
		_currentPetStatus = i;
		if (_currentPetStatus == 5) {
			setHomeX(getX());
			setHomeY(getY());
		}
		if (_currentPetStatus == 7) {
			allTargetClear();
		}

		if (_currentPetStatus == 3) {
			allTargetClear();
		} else {
			if (!isAiRunning()) {
				startAI();
			}
		}
	}

	public int getCurrentPetStatus() {
		return _currentPetStatus;
	}

	public int getItemObjId() {
		return _itemObjId;
	}

	public void setExpPercent(int expPercent) {
		_expPercent = expPercent;
	}

	public int getExpPercent() {
		return _expPercent;
	}

	private L1ItemInstance _weapon;

	public void setWeapon(L1ItemInstance weapon) {
		_weapon = weapon;
	}

	public L1ItemInstance getWeapon() {
		return _weapon;
	}

	private L1ItemInstance _armor;

	public void setArmor(L1ItemInstance armor) {
		_armor = armor;
	}

	public L1ItemInstance getArmor() {
		return _armor;
	}

	private int _hitByWeapon;

	public void setHitByWeapon(int i) {
		_hitByWeapon = i;
	}

	public int getHitByWeapon() {
		return _hitByWeapon;
	}

	private int _damageByWeapon;

	public void setDamageByWeapon(int i) {
		_damageByWeapon = i;
	}

	public int getDamageByWeapon() {
		return _damageByWeapon;
	}

	private static Logger _log = Logger
			.getLogger(L1PetInstance.class.getName());
	private int _currentPetStatus;
	private L1PcInstance _petMaster;
	private int _itemObjId;
	private L1PetType _type;
	private int _expPercent;

	public L1PetType getPetType() {
		return _type;
	}
}
