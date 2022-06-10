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

import java.util.Random;

import l1j.server.server.ActionCodes;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.datatables.WeaponSkillTable;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_EffectLocation;
import l1j.server.server.serverpackets.S_Paralysis;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_UseAttackSkill;
import l1j.server.server.templates.L1Skills;
import static l1j.server.server.model.skill.L1SkillId.*;

// Referenced classes of package l1j.server.server.model:
// L1PcInstance

public class L1WeaponSkill {

	private static Random _random = new Random();

	private int _weaponId;

	private int _probability;

	private int _fixDamage;

	private int _randomDamage;

	private int _area;

	private int _skillId;

	private int _skillTime;

	private int _effectId;

	private int _effectTarget; // エフェクトの対象 0:相手 1:自分

	private boolean _isArrowType;

	private int _attr;
	
	//修改weapon_skill table 增加顯示圖案
	private int _gfxId;
	private boolean _gfxIdTarget;

	public int getGfxId() {
		return _gfxId;
	}
	
	public boolean getGfxIdTarget() {
		return _gfxIdTarget;
	}
	
	//增加武器技能欄位新增 by eric1300460
	private int _refStr = 0;
	private int _refInt = 0;
	private int _refWis = 0;
	private int _refDex = 0;
	private int _refCon = 0;
	private int _refCha = 0;
	private int _drainHp = 0;
	private int _drainMp = 0;
	
	public void setRefStr(int int1) {
		_refStr=int1;
	}
	public int getRefStr() {
		return _refStr;
	}
	
	public void setRefInt(int int1) {
		_refInt=int1;
	}
	public int getRefInt() {
		return _refInt;
	}
	public void setRefWis(int int1) {
		_refWis=int1;
	}
	public int getRefWis() {
		return _refWis;
	}
	public void setRefDex(int int1) {
		_refDex=int1;
	}
	public int getRefDex() {
		return _refDex;
	}
	public void setRefCon(int int1) {
		_refCon=int1;
	}
	public int getRefCon() {
		return _refCon;
	}
	public void setRefCha(int int1) {
		_refCha=int1;
	}
	public int getRefCha() {
		return _refCha;
	}
	
	public void setDrainHp(int int1) {
		_drainHp=int1;
	}
	public int getDrainHp() {
		if(_drainHp==0){
			return 0;
		}
		return _random.nextInt(_drainHp)+1;
	}
	public void setDrainMp(int int1) {
		_drainMp=int1;
	}
	public int getDrainMp() {
		if(_drainMp==0){
			return 0;
		}
		return _random.nextInt(_drainMp)+1;
	}
	//~增加武器技能欄位新增  by eric1300460
	
	public L1WeaponSkill(int weaponId, int probability, int fixDamage,
			int randomDamage, int area, int skillId, int skillTime,
			int effectId, int effectTarget, boolean isArrowType, int attr,
			int gfxId,boolean gfxIdTarget,int refStr,int refInt,
			int refWis, int refDex,int refCon,int refCha,
			int drainHp, int drainMp) {
		
		_gfxId=gfxId;
		_gfxIdTarget=gfxIdTarget;
		//~修改weapon_skill table 增加顯示圖案
		//增加武器技能欄位新增  by eric1300460
		_refStr = refStr;
		_refInt = refInt;
		_refWis = refWis;
		_refDex = refDex;
		_refCon = refCon;
		_refCha = refCha;
		_drainHp = drainHp;
		_drainMp = drainMp;
		//~增加武器技能欄位新增  by eric1300460
		_weaponId = weaponId;
		_probability = probability;
		_fixDamage = fixDamage;
		_randomDamage = randomDamage;
		_area = area;
		_skillId = skillId;
		_skillTime = skillTime;
		_effectId = effectId;
		_effectTarget = effectTarget;
		_isArrowType = isArrowType;
		_attr = attr;
	}

	public int getWeaponId() {
		return _weaponId;
	}

	public int getProbability() {
		return _probability;
	}

	public int getFixDamage() {
		return _fixDamage;
	}

	public int getRandomDamage() {
		return _randomDamage;
	}

	public int getArea() {
		return _area;
	}

	public int getSkillId() {
		return _skillId;
	}

	public int getSkillTime() {
		return _skillTime;
	}

	public int getEffectId() {
		return _effectId;
	}

	public int getEffectTarget() {
		return _effectTarget;
	}

	public boolean isArrowType() {
		return _isArrowType;
	}

	public int getAttr() {
		return _attr;
	}

	public static double getWeaponSkillDamage(L1PcInstance pc, L1Character cha, int weaponId) {
		L1WeaponSkill weaponSkill = WeaponSkillTable.getInstance().getTemplate(weaponId);
		if (pc == null || cha == null || weaponSkill == null) {
			return 0;
		}

		int chance = _random.nextInt(100) + 1;
		if (weaponSkill.getProbability() < chance) {
			return 0;
		}

		int skillId = weaponSkill.getSkillId();
		if (skillId != 0) {
			L1Skills skill = SkillsTable.getInstance().getTemplate(skillId);
			if (skill != null && skill.getTarget().equals("buff")) {
				if (!isFreeze(cha)) { // 凍結状態orカウンターマジック中
					cha.setSkillEffect(skillId, weaponSkill.getSkillTime() * 1000);
				}
			}
		}

		int effectId = weaponSkill.getEffectId();
		if (effectId != 0) {
			int chaId = 0;
			if (weaponSkill.getEffectTarget() == 0) {
				chaId = cha.getId();
			} else {
				chaId = pc.getId();
			}
			boolean isArrowType = weaponSkill.isArrowType(); 
			if (!isArrowType) {
				pc.sendPackets(new S_SkillSound(chaId, effectId));
				pc.broadcastPacket(new S_SkillSound(chaId, effectId));
			} else {
				S_UseAttackSkill packet = new S_UseAttackSkill(pc, cha.getId(),
						effectId, cha.getX(), cha.getY(), ActionCodes
						.ACTION_Attack, false);
				pc.sendPackets(packet);
				pc.broadcastPacket(packet);
			}
		}

		double damage = 0;
		int randomDamage = weaponSkill.getRandomDamage();
		if (randomDamage != 0) {
			damage = _random.nextInt(randomDamage);
		}
		damage += weaponSkill.getFixDamage();
		
		if(pc.getInt()>12){//fix by sdcom 2009.12.11
			damage *= (pc.getInt()/12);
		}
		
		int area = weaponSkill.getArea();
		if (area > 0 || area == -1) { // 範囲の場合
			for (L1Object object : L1World.getInstance()
					.getVisibleObjects(cha, area)) {
				if (object == null) {
					continue;
				}
				if (!(object instanceof L1Character)) {
					continue;
				}
				if (object.getId() == pc.getId()) {
					continue;
				}
				if (object.getId() == cha.getId()) { // 攻撃対象はL1Attackで処理するため除外
					continue;
				}

				// 攻撃対象がMOBの場合は、範囲内のMOBにのみ当たる
				// 攻撃対象がPC,Summon,Petの場合は、範囲内のPC,Summon,Pet,MOBに当たる
				if (cha instanceof L1MonsterInstance) {
					if (!(object instanceof L1MonsterInstance)) {
						continue;
					}
				}
				if (cha instanceof L1PcInstance
						|| cha instanceof L1SummonInstance
						|| cha instanceof L1PetInstance) {
					if (!(object instanceof L1PcInstance
							|| object instanceof L1SummonInstance
							|| object instanceof L1PetInstance
							|| object instanceof L1MonsterInstance)) {
						continue;
					}
				}

				damage = calcDamageReduction((L1Character) object, damage, weaponSkill.getAttr());
				if (damage <= 0) {
					continue;
				}
				if (object instanceof L1PcInstance) {
					L1PcInstance targetPc = (L1PcInstance) object;
					targetPc.sendPackets(new S_DoActionGFX(targetPc.getId(), ActionCodes.ACTION_Damage));
					targetPc.broadcastPacket(new S_DoActionGFX(targetPc.getId(), ActionCodes.ACTION_Damage));
					targetPc.receiveDamage(pc, (int) damage);
				} else if (object instanceof L1SummonInstance
						|| object instanceof L1PetInstance
						|| object instanceof L1MonsterInstance) {
					L1NpcInstance targetNpc = (L1NpcInstance) object;
					targetNpc.broadcastPacket(new S_DoActionGFX(targetNpc.getId(), ActionCodes.ACTION_Damage));
					targetNpc.receiveDamage(pc, (int) damage);
				}
			}
		}
		
		//修改weapon_skill table 增加顯示圖案
		if (weaponSkill.getGfxId() != 0) {
			if (weaponSkill.getGfxIdTarget()) {// 目標顯示
				pc.sendPackets(new S_SkillSound(cha.getId(), weaponSkill.getGfxId()));
				pc.broadcastPacket(new S_SkillSound(cha.getId(), weaponSkill.getGfxId()));
			} else {// 自己身上顯示
				pc.sendPackets(new S_SkillSound(pc.getId(), weaponSkill.getGfxId()));
				pc.broadcastPacket(new S_SkillSound(pc.getId(), weaponSkill.getGfxId()));
			}
		}
		//~修改weapon_skill table 增加顯示圖案
		
		return calcDamageReduction(cha, damage, weaponSkill.getAttr());
	}

	public static double getBaphometStaffDamage(L1PcInstance pc,
			L1Character cha) {
		double dmg = 0;
		int chance = _random.nextInt(100) + 1;
		if (14 >= chance) {
			int locx = cha.getX();
			int locy = cha.getY();
			int sp = pc.getSp();
			int intel = pc.getInt();
			double bsk = 0;
			if (pc.hasSkillEffect(BERSERKERS)) {
				bsk = 0.2;
			}
			dmg = (intel + sp) * (1.8 + bsk) + _random.nextInt(intel + sp)
					* 1.8;
			S_EffectLocation packet = new S_EffectLocation(locx, locy, 129);
			pc.sendPackets(packet);
			pc.broadcastPacket(packet);
		}
		return calcDamageReduction(cha, dmg, L1Skills.ATTR_EARTH);
	}

	public static double getDiceDaggerDamage(L1PcInstance pc,
			L1PcInstance targetPc, L1ItemInstance weapon) {
		double dmg = 0;
		int chance = _random.nextInt(100) + 1;
		if (3 >= chance) {
			dmg = targetPc.getCurrentHp() * 2 / 3;
			if (targetPc.getCurrentHp() - dmg < 0) {
				dmg = 0;
			}
			String msg = weapon.getLogName();
			pc.sendPackets(new S_ServerMessage(158, msg));
			// \f1%0が蒸発してなくなりました。
			pc.getInventory().removeItem(weapon, 1);
		}
		return dmg;
	}

	public static int getKiringkuDamage(L1PcInstance pc, L1Character cha) {
		int dmg = 0;
		int dice = 5;
		int diceCount = 2;
		int value = 14;
		int kiringkuDamage = 0;
		int charaIntelligence = 0;

		for (int i = 0; i < diceCount; i++) {
			kiringkuDamage += (_random.nextInt(dice) + 1);
		}
		kiringkuDamage += value;

		int spByItem = pc.getSp() - pc.getTrueSp(); // アイテムによるSP変動
		charaIntelligence = pc.getInt() + spByItem - 12;
		if (charaIntelligence < 1) {
			charaIntelligence = 1;
		}
		double kiringkuCoefficientA = (1.0 + charaIntelligence * 3.0 / 32.0);

		kiringkuDamage *= kiringkuCoefficientA;

		double mrFloor = 0;
		if (cha.getMr() <= 100) {
			mrFloor = Math.floor((cha.getMr() - pc.getOriginalMagicHit()) / 2);
		} else if (cha.getMr() >= 100) {
			mrFloor = Math.floor((cha.getMr() - pc.getOriginalMagicHit()) / 10);
		}

		double kiringkuCoefficientB = 0;
		if (cha.getMr() <= 100) {
			kiringkuCoefficientB = 1 - 0.01 * mrFloor;
		} else if (cha.getMr() > 100) {
			kiringkuCoefficientB = 0.6 - 0.01 * mrFloor;
		}

		double kiringkuFloor = Math.floor(kiringkuDamage);

		dmg += kiringkuFloor + pc.getWeapon().getEnchantLevel();

		dmg *= kiringkuCoefficientB;

		if (pc.getWeapon().getItem().getItemId() == 270) {
			pc.sendPackets(new S_SkillSound(pc.getId(), 6983));
			pc.broadcastPacket(new S_SkillSound(pc.getId(), 6983));
		} else {
			pc.sendPackets(new S_SkillSound(pc.getId(), 7049));
			pc.broadcastPacket(new S_SkillSound(pc.getId(), 7049));
		}

		return dmg;
	}

	public static double getAreaSkillWeaponDamage(L1PcInstance pc, L1Character cha, int weaponId) {
		double dmg = 0;
		int probability = 0;
		int attr = 0;
		int chance = _random.nextInt(100) + 1;
		if (weaponId == 263) { // フリージングランサー
			probability = 5;
			attr = L1Skills.ATTR_WATER;
		} else if (weaponId == 260) { // レイジングウィンド
			probability = 4;
			attr = L1Skills.ATTR_WIND;
		}
		if (probability >= chance) {
			int sp = pc.getSp();
			int intel = pc.getInt();
			int area = 0;
			int effectTargetId = 0;
			int effectId = 0;
			L1Character areaBase = cha;
			double damageRate = 0;

			if (weaponId == 263) { // フリージングランサー
				area = 3;
				damageRate = 1.4D;
				effectTargetId = cha.getId();
				effectId = 1804;
				areaBase = cha;
			} else if (weaponId == 260) { // レイジングウィンド
				area = 4;
				damageRate = 1.5D;
				effectTargetId = pc.getId();
				effectId = 758;
				areaBase = pc;
			}
			double bsk = 0;
			if (pc.hasSkillEffect(BERSERKERS)) {
				bsk = 0.2;
			}
			dmg = (intel + sp) * (damageRate + bsk) + _random.nextInt(intel + sp) * damageRate;
			pc.sendPackets(new S_SkillSound(effectTargetId, effectId));
			pc.broadcastPacket(new S_SkillSound(effectTargetId, effectId));

			for (L1Object object : L1World.getInstance().getVisibleObjects(areaBase, area)) {
				if (object == null) {
					continue;
				}
				if (!(object instanceof L1Character)) {
					continue;
				}
				if (object.getId() == pc.getId()) {
					continue;
				}
				if (object.getId() == cha.getId()) { // 攻撃対象は除外
					continue;
				}

				// 攻撃対象がMOBの場合は、範囲内のMOBにのみ当たる
				// 攻撃対象がPC,Summon,Petの場合は、範囲内のPC,Summon,Pet,MOBに当たる
				if (cha instanceof L1MonsterInstance) {
					if (!(object instanceof L1MonsterInstance)) {
						continue;
					}
				}
				if (cha instanceof L1PcInstance
						|| cha instanceof L1SummonInstance
						|| cha instanceof L1PetInstance) {
					if (!(object instanceof L1PcInstance
							|| object instanceof L1SummonInstance
							|| object instanceof L1PetInstance
							|| object instanceof L1MonsterInstance)) {
						continue;
					}
				}

				dmg = calcDamageReduction((L1Character) object, dmg, attr);
				if (dmg <= 0) {
					continue;
				}
				if (object instanceof L1PcInstance) {
					L1PcInstance targetPc = (L1PcInstance) object;
					targetPc.sendPackets(new S_DoActionGFX(targetPc.getId(), ActionCodes.ACTION_Damage));
				   targetPc.broadcastPacket(new S_DoActionGFX(targetPc.getId(), ActionCodes.ACTION_Damage));
				   targetPc.receiveDamage(pc, (int) dmg);
				} else if (object instanceof L1SummonInstance
						|| object instanceof L1PetInstance
						|| object instanceof L1MonsterInstance) {
					L1NpcInstance targetNpc = (L1NpcInstance) object;
					targetNpc.broadcastPacket(new S_DoActionGFX(targetNpc.getId(), ActionCodes.ACTION_Damage));
					targetNpc.receiveDamage(pc, (int) dmg);
				}
			}
		}
		return calcDamageReduction(cha, dmg, attr);
	}

	public static double getLightningEdgeDamage(L1PcInstance pc,
			L1Character cha) {
		double dmg = 0;
		int chance = _random.nextInt(100) + 1;
		if (4 >= chance) {
			int sp = pc.getSp();
			int intel = pc.getInt();
			double bsk = 0;
			if (pc.hasSkillEffect(BERSERKERS)) {
				bsk = 0.2;
			}
			dmg = (intel + sp) * (2 + bsk) + _random.nextInt(intel + sp) * 2;

			pc.sendPackets(new S_SkillSound(cha.getId(), 10));
			pc.broadcastPacket(new S_SkillSound(cha.getId(), 10));
		}
		return calcDamageReduction(cha, dmg, L1Skills.ATTR_WIND);
	}

	public static void giveArkMageDiseaseEffect(L1PcInstance pc,
			L1Character cha) {
		int chance = _random.nextInt(1000) + 1;
		int probability = (5 - ((cha.getMr() / 10) * 5)) * 10;
		if (probability == 0) {
			probability = 10;
		}
		if (probability >= chance) {
			L1SkillUse l1skilluse = new L1SkillUse();
			l1skilluse.handleCommands(pc, 56,
					cha.getId(), cha.getX(), cha.getY(), null, 0,
					L1SkillUse.TYPE_GMBUFF);
	   }
	}

	public static void giveFettersEffect(L1PcInstance pc, L1Character cha) {
		int fettersTime = 8000;
		if (isFreeze(cha)) { // 凍結状態orカウンターマジック中
			return;
		}
		if (_random.nextInt(100) + 1 <= 2) {
			L1EffectSpawn.getInstance().spawnEffect(81182, fettersTime, cha.getX(), cha.getY(), cha.getMapId());
			if (cha instanceof L1PcInstance) {
				L1PcInstance targetPc = (L1PcInstance) cha;
				targetPc.setSkillEffect(STATUS_FREEZE, fettersTime);
				targetPc.sendPackets(new S_SkillSound(targetPc.getId(), 4184));
				targetPc.broadcastPacket(new S_SkillSound(targetPc.getId(),
						4184));
				targetPc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_BIND,
						true));
			} else if (cha instanceof L1MonsterInstance
					|| cha instanceof L1SummonInstance
					|| cha instanceof L1PetInstance) {
				L1NpcInstance npc = (L1NpcInstance) cha;
				npc.setSkillEffect(STATUS_FREEZE, fettersTime);
				npc.broadcastPacket(new S_SkillSound(npc.getId(), 4184));
				npc.setParalyzed(true);
			}
		}
	}

	private static double calcDamageReduction(L1Character cha, double dmg, int attr) {
		// 凍結状態orカウンターマジック中
		if (isFreeze(cha)) {
			return 0;
		}

		// MRによるダメージ軽減
		int mr = cha.getMr();
		int rnd = _random.nextInt(100) + 1;
		if (mr >= rnd) {
			dmg /= 2;
		}

		// 属性によるダメージ軽減
		int resist = 0;
		if (attr == L1Skills.ATTR_EARTH) {
			resist = cha.getEarth();
		} else if (attr == L1Skills.ATTR_FIRE) {
			resist = cha.getFire();
		} else if (attr == L1Skills.ATTR_WATER) {
			resist = cha.getWater();
		} else if (attr == L1Skills.ATTR_WIND) {
			resist = cha.getWind();
		}
		int resistFloor = (int) (0.32 * Math.abs(resist));
		if (resist >= 0) {
			resistFloor *= 1;
		} else {
			resistFloor *= -1;
		}
		double attrDeffence = resistFloor / 32.0;
		dmg = (1.0 - attrDeffence) * dmg;

		return dmg;
	}

	private static boolean isFreeze(L1Character cha) {
		if (cha.hasSkillEffect(STATUS_FREEZE)) {
			return true;
		}
		if (cha.hasSkillEffect(ABSOLUTE_BARRIER)) {
			return true;
		}
		if (cha.hasSkillEffect(ICE_LANCE)) {
			return true;
		}
		if (cha.hasSkillEffect(FREEZING_BLIZZARD)) {
			return true;
		}
		if (cha.hasSkillEffect(FREEZING_BREATH)) {
			return true;
		}
		if (cha.hasSkillEffect(EARTH_BIND)) {
			return true;
		}

		// カウンターマジック判定
		if (cha.hasSkillEffect(COUNTER_MAGIC)) {
			cha.removeSkillEffect(COUNTER_MAGIC);
			int castgfx = SkillsTable.getInstance().getTemplate(
					COUNTER_MAGIC).getCastGfx();
			cha.broadcastPacket(new S_SkillSound(cha.getId(), castgfx));
			if (cha instanceof L1PcInstance) {
				L1PcInstance pc = (L1PcInstance) cha;
				pc.sendPackets(new S_SkillSound(pc.getId(), castgfx));
			}
			return true;
		}
		return false;
	}

}
