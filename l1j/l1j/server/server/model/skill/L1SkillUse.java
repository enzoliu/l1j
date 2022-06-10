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

package l1j.server.server.model.skill;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.ActionCodes;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.datatables.PolyTable;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.model.L1Awake;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1CurseParalysis;
import l1j.server.server.model.L1EffectSpawn;
import l1j.server.server.model.L1Location;
import l1j.server.server.model.L1Magic;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1PcInventory;
import l1j.server.server.model.L1PinkName;
import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1War;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.*;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.model.poison.L1DamagePoison;
import l1j.server.server.model.trap.L1WorldTraps;
import l1j.server.server.serverpackets.S_AttackPacket;
import l1j.server.server.serverpackets.S_ChangeHeading;
import l1j.server.server.serverpackets.S_ChangeName;
import l1j.server.server.serverpackets.S_ChangeShape;
import l1j.server.server.serverpackets.S_CharVisualUpdate;
import l1j.server.server.serverpackets.S_ChatPacket;
import l1j.server.server.serverpackets.S_CurseBlind;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.serverpackets.S_Dexup;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_DoActionShop;
import l1j.server.server.serverpackets.S_EffectLocation;
import l1j.server.server.serverpackets.S_HPUpdate;
import l1j.server.server.serverpackets.S_Invis;
import l1j.server.server.serverpackets.S_MPUpdate;
import l1j.server.server.serverpackets.S_Message_YN;
import l1j.server.server.serverpackets.S_NpcChatPacket;
import l1j.server.server.serverpackets.S_OwnCharAttrDef;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_Paralysis;
import l1j.server.server.serverpackets.S_Poison;
import l1j.server.server.serverpackets.S_RangeSkill;
import l1j.server.server.serverpackets.S_SPMR;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_ShowPolyList;
import l1j.server.server.serverpackets.S_ShowSummonList;
import l1j.server.server.serverpackets.S_SkillBrave;
import l1j.server.server.serverpackets.S_SkillHaste;
import l1j.server.server.serverpackets.S_SkillIconAura;
import l1j.server.server.serverpackets.S_SkillIconGFX;
import l1j.server.server.serverpackets.S_SkillIconShield;
import l1j.server.server.serverpackets.S_SkillIconWindShackle;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_Sound;
import l1j.server.server.serverpackets.S_Strup;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_TrueTarget;
import l1j.server.server.serverpackets.S_UseAttackSkill;
import l1j.server.server.templates.L1BookMark;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.templates.L1Skills;
import static l1j.server.server.model.skill.L1SkillId.*;

public class L1SkillUse {
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_LOGIN = 1;
	public static final int TYPE_SPELLSC = 2;
	public static final int TYPE_NPCBUFF = 3;
	public static final int TYPE_GMBUFF = 4;

	private L1Skills _skill;
	private int _skillId;
	private int _getBuffDuration;
	private int _shockStunDuration;
	private int _getBuffIconDuration;
	private int _targetID;
	private int _mpConsume = 0;
	private int _hpConsume = 0;
	private int _targetX = 0;
	private int _targetY = 0;
	private String _message = null;
	private int _skillTime = 0;
	private int _type = 0;
	private boolean _isPK = false;
	private int _bookmarkId = 0;
	private int _itemobjid = 0;
	private boolean _checkedUseSkill = false; // 事前チェック済みか
	private int _leverage = 10; // 1/10倍なので10で1倍
	private boolean _isFreeze = false;
	private boolean _isCounterMagic = true;
	private boolean _isGlanceCheckFail = false;
	private int _actid = 0;
	private int _gfxid = 0;

	private L1Character _user = null;
	private L1Character _target = null;

	private L1PcInstance _player = null;
	private L1NpcInstance _npc = null;
	private L1NpcInstance _targetNpc = null;

	private int _calcType;
	private static final int PC_PC = 1;
	private static final int PC_NPC = 2;
	private static final int NPC_PC = 3;
	private static final int NPC_NPC = 4;

	private ArrayList<TargetStatus> _targetList;

	private static Logger _log = Logger.getLogger(L1SkillUse.class.getName());

	private static final int[] CAST_WITH_INVIS = { 
			HEAL, LIGHT, SHIELD, TELEPORT, HOLY_WEAPON, CURE_POISON, ENCHANT_WEAPON, 
			DETECTION, DECREASE_WEIGHT, EXTRA_HEAL, BLESSED_ARMOR, PHYSICAL_ENCHANT_DEX, 
			COUNTER_MAGIC, MEDITATION, GREATER_HEAL, REMOVE_CURSE, PHYSICAL_ENCHANT_STR, 
			HASTE, CANCELLATION, BLESS_WEAPON, HEAL_ALL, HOLY_WALK, GREATER_HASTE, 
			BERSERKERS, FULL_HEAL, INVISIBILITY, RESURRECTION, LIFE_STREAM, SHAPE_CHANGE, 
			IMMUNE_TO_HARM, MASS_TELEPORT, COUNTER_DETECTION, CREATE_MAGICAL_WEAPON, 
			GREATER_RESURRECTION, ABSOLUTE_BARRIER, ADVANCE_SPIRIT, REDUCTION_ARMOR,
			BOUNCE_ATTACK, SOLID_CARRIAGE, COUNTER_BARRIER, BLIND_HIDING, ENCHANT_VENOM, 
			SHADOW_ARMOR, BRING_STONE, MOVING_ACCELERATION, BURNING_SPIRIT, VENOM_RESIST, 
			DOUBLE_BRAKE, UNCANNY_DODGE, SHADOW_FANG, DRESS_MIGHTY, DRESS_DEXTERITY, 
			DRESS_EVASION, TRUE_TARGET, GLOWING_AURA, SHINING_AURA, CALL_CLAN, BRAVE_AURA, 
			RUN_CLAN, RESIST_MAGIC, BODY_TO_MIND, TELEPORT_TO_MATHER, ELEMENTAL_FALL_DOWN, 
			COUNTER_MIRROR, CLEAR_MIND, RESIST_ELEMENTAL, BLOODY_SOUL, ELEMENTAL_PROTECTION, 
			FIRE_WEAPON, WIND_SHOT,	WIND_WALK, EARTH_SKIN, FIRE_BLESS, STORM_EYE, 
			NATURES_TOUCH, EARTH_BLESS, BURNING_WEAPON, NATURES_BLESSING, CALL_OF_NATURE, 
			STORM_SHOT, IRON_SKIN, EXOTIC_VITALIZE, WATER_LIFE,	ELEMENTAL_FIRE, 
			SOUL_OF_FLAME, ADDITIONAL_FIRE, DRAGON_SKIN, AWAKEN_ANTHARAS, AWAKEN_FAFURION, 
			AWAKEN_VALAKAS, MIRROR_IMAGE, ILLUSION_OGRE, ILLUSION_LICH, PATIENCE, 
			ILLUSION_DIA_GOLEM, INSIGHT, ILLUSION_AVATAR
			};
			/*
			 * 1:	初級治癒術	
			 * 2:	日光術		
			 * 3:	保護罩
			 * 5:	指定傳送
			 * 8:	神聖武器
			 * 9:	解毒術
			 * 12:	擬似魔法武器
			 * 13:	無所遁形術
			 * 14:	負重強化
			 * 19:	中級治癒術
			 * 21:	鎧甲護持
			 * 26:	通暢氣脈術
			 * 31:	魔法屏障
			 * 32:	冥想術
			 * 35:	高級治癒術
			 * 37:	聖潔之光
			 * 42:	體魄強健術
			 * 43:	加速術
			 * 44:	魔法相消術
			 * 48:	祝福魔法武器
			 * 49:	體力回復術
			 * 52:	神聖疾走
			 * 54:	強力加速術
			 * 55:	狂暴術
			 * 57:	全部治癒術
			 * 60:	隱身術
			 * 61:	返生術
			 * 67:	變形術
			 * 68:	聖結界
			 * 69:	集體傳送術
			 * 72:	強力無所遁形術
			 * 73:	創造魔法武器
			 * 75:	終極返生術
			 * 78:	絕對屏障
			 * 79:	靈魂昇華
			 * 87:	衝擊之暈
			 * 88:	增幅防禦
			 * 89:	尖刺盔甲
			 * 90:	堅固防護
			 * 91:	反擊屏障
			 * 97:	暗隱術
			 * 98:	附加劇毒
			 * 99:	影之防護
			 * 100:	提煉魔石
			 * 101:	行走加速
			 * 102:	燃燒鬥志
			 * 104:	毒性抵抗
			 * 105:	雙重破壞
			 * 106:	暗影閃避
			 * 107:	暗影之牙
			 * 109:	力量提升
			 * 110:	敏捷提升
			 * 111:	閃避提升
			 * 113:	精準目標
			 * 114:	激勵士氣
			 * 115:	鋼鐵士氣
			 * 116:	呼喚盟友
			 * 117:	衝擊士氣
			 * 118:	援護盟友
			 * 129:	魔法防禦
			 * 130:	心靈轉換
			 * 131:	世界樹的呼喚
			 * 132:	三重矢
			 * 134:	鏡反射
			 * 137:	淨化精神
			 * 138:	屬性防禦
			 * 146:	魂體轉換
			 * 147:	魂體轉換
			 * 148:火焰武器
			 * 149:風之神射
			 * 150:風之疾走
			 * 151:大地防護
			 * 155:烈炎氣息
			 * 156:暴風之眼
			 * 158:生命之泉
			 * 159:大地的祝福
			 * 161:封印禁地
			 * 163:烈炎武器
			 * 164:生命的祝福
			 * 165:生命呼喚
			 * 166:暴風神射
			 * 168:鋼鐵防護
			 * 169:體能激發
			 * 170:水之元氣
			 * 171:屬性之火
			 * 175:烈焰之魂
			 * 176:能量激發
			 * 181:龍之護鎧
			 * 185:覺醒安塔瑞斯
			 * 190:覺醒法利昂
			 * 195:覺醒巴拉卡斯
			 * 201:鏡像
			 * 204:幻覺歐吉
			 * 209:幻覺巫妖
			 * 211:耐力
			 * 214:幻覺鑽石高崙
			 * 216:立方衝擊
			 * 219:幻覺化身
			 * 10026:
			 * 10027:
			 * 10028:
			 * 10029:
			 * 
			 * 
			 */
	private static final int[] EXCEPT_COUNTER_MAGIC = { 1, 2, 3, 5, 8, 9, 12,
			13, 14, 19, 21, 26, 31, 32, 35, 37, 42, 43, 44, 48, 49, 52, 54, 55,
			57, 60, 61, 67, 68, 69, 72, 73, 75, 78, 79, 87,
			88, 89, 90, 91,	97, 98, 99, 100, 101, 102, 104, 105, 106, 107, 109, 110, 111, 113,
			114, 115, 116, 117, 118, 129, 130, 131, 132, 134, 137, 138, 146,
			147, 148, 149, 150, 151, 155, 156, 158, 159, 161, 163, 164, 165,
			166, 168, 169, 170, 171, 175, 176, 181, 185, 190, 195,
			201, 204, 209, 211, 214, 216, 219, 10026, 10027, 10028, 10029,
			};

	public L1SkillUse() {
	}

	private static class TargetStatus {
		private L1Character _target = null;
		private boolean _isAction = false; // ダメージモーションが発生するか？
		private boolean _isSendStatus = false; // キャラクターステータスを送信するか？（ヒール、スローなど状態が変わるとき送る）
		private boolean _isCalc = true; // ダメージや確率魔法の計算をする必要があるか？

		public TargetStatus(L1Character _cha) {
			_target = _cha;
		}

		public TargetStatus(L1Character _cha, boolean _flg) {
			_isCalc = _flg;
		}

		public L1Character getTarget() {
			return _target;
		}

		public boolean isCalc() {
			return _isCalc;
		}

		public void isAction(boolean _flg) {
			_isAction = _flg;
		}

		public boolean isAction() {
			return _isAction;
		}

		public void isSendStatus(boolean _flg) {
			_isSendStatus = _flg;
		}

		public boolean isSendStatus() {
			return _isSendStatus;
		}
	}

	/*
	 * 1/10倍で表現する。
	 */
	public void setLeverage(int i) {
		_leverage = i;
	}

	public int getLeverage() {
		return _leverage;
	}

	private boolean isCheckedUseSkill() {
		return _checkedUseSkill;
	}

	private void setCheckedUseSkill(boolean flg) {
		_checkedUseSkill = flg;
	}
	
	public boolean checkUseSkill(L1PcInstance player, int skillid, int target_id, int x, int y, String message, int time, int type,
			L1Character attacker, int actid, int gfxid, int mpConsume) {
		// 初期設定ここから
		setCheckedUseSkill(true);
		_targetList = new ArrayList<TargetStatus>(); // ターゲットリストの初期化

		_skill = SkillsTable.getInstance().getTemplate(skillid);
		_skillId = skillid;
		_targetX = x;
		_targetY = y;
		_message = message;
		_skillTime = time;
		_type = type;
		_actid = actid;
		_gfxid = gfxid;
		_mpConsume = mpConsume;
		boolean checkedResult = true;

		if (attacker == null) {
			// pc
			_player = player;
			_user = _player;
		} else {
			// npc
			_npc = (L1NpcInstance) attacker;
			_user = _npc;
		}

		if (_skill.getTarget().equals("none")) {
			_targetID = _user.getId();
			_targetX = _user.getX();
			_targetY = _user.getY();
		} else {
			_targetID = target_id;
		}

		if (type == TYPE_NORMAL) { // 通常の魔法使用時
			checkedResult = isNormalSkillUsable();
		} else if (type == TYPE_SPELLSC) { // スペルスクロール使用時
			checkedResult = isSpellScrollUsable();
		} else if (type == TYPE_NPCBUFF) {
			checkedResult = true;
		}
		if (!checkedResult) {
			return false;
		}

		// ファイアーウォール、ライフストリームは詠唱対象が座標
		// キューブは詠唱者の座標に配置されるため例外
		if ((_skillId == FIRE_WALL) || (_skillId == LIFE_STREAM) || (_skillId == TRUE_TARGET)) {
			return true;
		}

		L1Object l1object = L1World.getInstance().findObject(_targetID);
		if (l1object instanceof L1ItemInstance) {
			_log.fine("skill target item name: " + ((L1ItemInstance) l1object).getViewName());
			// スキルターゲットが精霊の石になることがある。
			// Linux環境で確認（Windowsでは未確認）
			// 2008.5.4追記：地面のアイテムに魔法を使うとなる。継続してもエラーになるだけなのでreturn
			return false;
		}
		if (_user instanceof L1PcInstance) {
			if (l1object instanceof L1PcInstance) {
				_calcType = PC_PC;
			} else {
				_calcType = PC_NPC;
				_targetNpc = (L1NpcInstance) l1object;
			}
		} else if (_user instanceof L1NpcInstance) {
			if (l1object instanceof L1PcInstance) {
				_calcType = NPC_PC;
			} else if (_skill.getTarget().equals("none")) {
				_calcType = NPC_PC;
			} else {
				_calcType = NPC_NPC;
				_targetNpc = (L1NpcInstance) l1object;
			}
		}

		// テレポート、マステレポートは対象がブックマークID
		if (_skillId == TELEPORT || _skillId == MASS_TELEPORT) {
			_bookmarkId = target_id;
		}
		// 対象がアイテムのスキル
		if (_skillId == CREATE_MAGICAL_WEAPON || _skillId == BRING_STONE || _skillId == BLESSED_ARMOR
				|| _skillId == ENCHANT_WEAPON || _skillId == SHADOW_FANG) {
			_itemobjid = target_id;
		}
		_target = (L1Character) l1object;

		if (!(_target instanceof L1MonsterInstance) && _skill.getTarget().equals("attack")
				&& _user.getId() != target_id) {
			_isPK = true; // ターゲットがモンスター以外で攻撃系スキルで、自分以外の場合PKモードとする。
		}

		// 初期設定ここまで

		// 事前チェック
		if (!(l1object instanceof L1Character)) { // ターゲットがキャラクター以外の場合何もしない。
			checkedResult = false;
		}
		makeTargetList(); // ターゲットの一覧を作成
		if (_targetList.size() == 0 && (_user instanceof L1NpcInstance)) {
			checkedResult = false;
		}
		// 事前チェックここまで
		return checkedResult;
	}

	public boolean checkUseSkill(L1PcInstance player, int skillid,
			int target_id, int x, int y, String message, int time, int type,
			L1Character attacker) {
		
		return checkUseSkill(player, skillid, target_id, x, y, message, time, type, attacker, 0, 0, 0);
	}

	/**
	 * 通常のスキル使用時に使用者の状態からスキルが使用可能であるか判断する
	 * 
	 * @return false スキルが使用不可能な状態である場合
	 */
	private boolean isNormalSkillUsable() {
		// スキル使用者がPCの場合のチェック
		if (_user instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) _user;

			if (pc.isParalyzed()) { // 麻痺・凍結状態か
				return false;
			}
			if ((pc.isInvisble() || pc.isInvisDelay()) && !isInvisUsableSkill()) { // インビジ中に使用不可のスキル
				return false;
			}
			if (pc.getInventory().getWeight240() >= 192) { // 重量オーバーならスキルを使用できない
				pc.sendPackets(new S_ServerMessage(316));
				return false;
			}
			int polyId = pc.getTempCharGfx();
			L1PolyMorph poly = PolyTable.getInstance().getTemplate(polyId);
			// 魔法が使えない変身
			if (poly != null && !poly.canUseSkill()) {
				pc.sendPackets(new S_ServerMessage(285)); // \f1その状態では魔法を使えません。
				return false;
			}

			if (!isAttrAgrees()) { // 精霊魔法で、属性が一致しなければ何もしない。
				return false;
			}

			if (_skillId == ELEMENTAL_PROTECTION && pc.getElfAttr() == 0) {
				pc.sendPackets(new S_ServerMessage(280)); // \f1魔法が失敗しました。
				return false;
			}

			// スキルディレイ中使用不可
			if (pc.isSkillDelay()) {
				return false;
			}

			// サイレンス状態では使用不可
			if (pc.hasSkillEffect(SILENCE)
					|| pc.hasSkillEffect(AREA_OF_SILENCE)
					|| pc.hasSkillEffect(STATUS_POISON_SILENCE)) {
				pc.sendPackets(new S_ServerMessage(285)); // \f1その状態では魔法を使えません。
				return false;
			}

			// DIGはロウフルでのみ使用可
			if (_skillId == DISINTEGRATE && pc.getLawful() < 500) {
				// このメッセージであってるか未確認
				pc.sendPackets(new S_ServerMessage(352, "$967")); // この魔法を利用するには性向値が%0でなければなりません。
				return false;
			}

			// 同じキューブは効果範囲外であれば配置可能
			if (_skillId == CUBE_IGNITION || _skillId == CUBE_QUAKE
					|| _skillId == CUBE_SHOCK || _skillId == CUBE_BALANCE) {
				boolean isNearSameCube = false;
				int gfxId = 0;
				for (L1Object obj : L1World.getInstance()
						.getVisibleObjects(pc, 3)) {
					if (obj instanceof L1EffectInstance) {
						L1EffectInstance effect = (L1EffectInstance) obj;
						gfxId = effect.getGfxId();
						if (_skillId == CUBE_IGNITION && gfxId == 6706
								|| _skillId == CUBE_QUAKE && gfxId == 6712
								|| _skillId == CUBE_SHOCK && gfxId == 6718
								|| _skillId == CUBE_BALANCE && gfxId == 6724) {
							isNearSameCube = true;
							break;
						}
					}
				}
				if (isNearSameCube) {
					pc.sendPackets(new S_ServerMessage(1412)); // すでに床にキューブが召喚されています。
					return false;
				}
			}

			// 覚醒状態では覚醒スキル以外使用不可
			if (pc.getAwakeSkillId() == AWAKEN_ANTHARAS
					&& _skillId != AWAKEN_ANTHARAS && _skillId != MAGMA_BREATH
					&& _skillId != SHOCK_SKIN && _skillId != FREEZING_BREATH
					|| pc.getAwakeSkillId() == AWAKEN_FAFURION
					&& _skillId != AWAKEN_FAFURION && _skillId != MAGMA_BREATH
					&& _skillId != SHOCK_SKIN && _skillId != FREEZING_BREATH
					|| pc.getAwakeSkillId() == AWAKEN_VALAKAS
					&& _skillId != AWAKEN_VALAKAS && _skillId != MAGMA_BREATH
					&& _skillId != SHOCK_SKIN && _skillId != FREEZING_BREATH) {
				pc.sendPackets(new S_ServerMessage(1385)); // 現在の状態では覚醒魔法が使えません。
				return false;
			}

			if (isItemConsume() == false && !_player.isGm()) { // 消費アイテムはあるか
				_player.sendPackets(new S_ServerMessage(299)); // 詠唱する材料がありません。
				return false;
			}
		}
		// スキル使用者がNPCの場合のチェック
		else if (_user instanceof L1NpcInstance) {

			// サイレンス状態では使用不可
			if (_user.hasSkillEffect(SILENCE)) {
				// NPCにサイレンスが掛かっている場合は1回だけ使用をキャンセルさせる効果。
				_user.removeSkillEffect(SILENCE);
				return false;
			}
		}

		// PC、NPC共通のチェック
		if (!isHPMPConsume()) { // 消費HP、MPはあるか
			return false;
		}
		return true;
	}

	/**
	 * スペルスクロール使用時に使用者の状態からスキルが使用可能であるか判断する
	 * 
	 * @return false スキルが使用不可能な状態である場合
	 */
	private boolean isSpellScrollUsable() {
		// スペルスクロールを使用するのはPCのみ
		L1PcInstance pc = (L1PcInstance) _user;

		if (pc.isParalyzed()) { // 麻痺・凍結状態か
			return false;
		}

		// インビジ中に使用不可のスキル
		if ((pc.isInvisble() || pc.isInvisDelay()) && !isInvisUsableSkill()) {
			return false;
		}

		return true;
	}

	// インビジ中に使用可能なスキルかを返す
	private boolean isInvisUsableSkill() {
		for (int skillId : CAST_WITH_INVIS) {
			if (skillId == _skillId) {
				return true;
			}
		}
		return false;
	}

	public void handleCommands(L1PcInstance player, int skillId, int targetId, int x, int y, String message, int timeSecs, int type) {
		L1Character attacker = null;
		handleCommands(player, skillId, targetId, x, y, message, timeSecs, type, attacker);
	}

	public void handleCommands(L1PcInstance player, int skillId, int targetId, int x, int y, String message, int timeSecs, int type, L1Character attacker) {
		try {
			// 事前チェックをしているか？
			if (!isCheckedUseSkill()) {
				boolean isUseSkill = checkUseSkill(player, skillId, targetId, x, y, message, timeSecs, type, attacker);

				if (!isUseSkill) {
					failSkill();
					return;
				}
			}

			if (type == TYPE_NORMAL) { // 魔法詠唱時
				if (!_isGlanceCheckFail || _skill.getArea() > 0 || _skill.getTarget().equals("none")) {
					runSkill();
					useConsume();
					sendGrfx(true);
					sendFailMessageHandle();
					setDelay();
				}
			} else if (type == TYPE_LOGIN) { // ログイン時（HPMP材料消費なし、グラフィックなし）
				runSkill();
			} else if (type == TYPE_SPELLSC) { // スペルスクロール使用時（HPMP材料消費なし）
				runSkill();
				sendGrfx(true);
			} else if (type == TYPE_GMBUFF) { // GMBUFF使用時（HPMP材料消費なし、魔法モーションなし）
				runSkill();
				sendGrfx(false);
			} else if (type == TYPE_NPCBUFF) { // NPCBUFF使用時（HPMP材料消費なし）
				runSkill();
				sendGrfx(true);
			}
			setCheckedUseSkill(false);
		} catch (Exception e) {
			_log.log(Level.SEVERE, "", e);
		}
	}

	/**
	 * スキルの失敗処理(PCのみ）
	 */
	private void failSkill() {
		// HPが足りなくてスキルが使用できない場合のみ、MPのみ消費したいが未実装（必要ない？）
		// その他の場合は何も消費されない。
		// useConsume(); // HP、MPは減らす
		setCheckedUseSkill(false);
		// テレポートスキル
		if (_skillId == TELEPORT || _skillId == MASS_TELEPORT
				|| _skillId == TELEPORT_TO_MATHER) {
			// テレポートできない場合でも、クライアント側は応答を待っている
			// テレポート待ち状態の解除（第2引数に意味はない）
			// lock teleport
			_player.sendPackets(new S_Paralysis(S_Paralysis.TYPE_TELEPORT, false));
		}
	}

	// ターゲットか？
	private boolean isTarget(L1Character cha) throws Exception {
		boolean _flg = false;

		if (cha instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) cha;
			if (pc.isGhost() || pc.isGmInvis()) {
				return false;
			}
		}
		if (_calcType == NPC_PC
				&& (cha instanceof L1PcInstance || cha instanceof L1PetInstance || cha instanceof L1SummonInstance)) {
			_flg = true;
		}

		// 破壊不可能なドアは対象外
		if (cha instanceof L1DoorInstance) {
			if (cha.getMaxHp() == 0 || cha.getMaxHp() == 1) {
				return false;
			}
		}

		// マジックドールは対象外
		if (cha instanceof L1DollInstance && _skillId != HASTE) {
			return false;
		}

		// 元のターゲットがPet、Summon以外のNPCの場合、PC、Pet、Summonは対象外
		if (_calcType == PC_NPC
				&& _target instanceof L1NpcInstance
				&& !(_target instanceof L1PetInstance)
				&& !(_target instanceof L1SummonInstance)
				&& (cha instanceof L1PetInstance
						|| cha instanceof L1SummonInstance || cha instanceof L1PcInstance)) {
			return false;
		}

		// 元のターゲットがガード以外のNPCの場合、ガードは対象外
		if (_calcType == PC_NPC
				&& _target instanceof L1NpcInstance
				&& !(_target instanceof L1GuardInstance)
				&& cha instanceof L1GuardInstance) {
			return false;
		}

		// NPC対PCでターゲットがモンスターの場合ターゲットではない。
		if ((_skill.getTarget().equals("attack") || _skill.getType() == L1Skills.TYPE_ATTACK)
				&& _calcType == NPC_PC
				&& !(cha instanceof L1PetInstance)
				&& !(cha instanceof L1SummonInstance)
				&& !(cha instanceof L1PcInstance)) {
			return false;
		}

		// NPC対NPCで使用者がMOBで、ターゲットがMOBの場合ターゲットではない。
		if ((_skill.getTarget().equals("attack")
				|| _skill.getType() == L1Skills.TYPE_ATTACK)
				&& _calcType == NPC_NPC
				&& _user instanceof L1MonsterInstance
				&& cha instanceof L1MonsterInstance) {
			return false;
		}

		// 無方向範囲攻撃魔法で攻撃できないNPCは対象外
		if (_skill.getTarget().equals("none")
				&& _skill.getType() == L1Skills.TYPE_ATTACK
				&& (cha instanceof L1AuctionBoardInstance
						|| cha instanceof L1BoardInstance
						|| cha instanceof L1CrownInstance
						|| cha instanceof L1DwarfInstance
						|| cha instanceof L1EffectInstance
						|| cha instanceof L1FieldObjectInstance
						|| cha instanceof L1FurnitureInstance
						|| cha instanceof L1HousekeeperInstance
						|| cha instanceof L1MerchantInstance
						|| cha instanceof L1TeleporterInstance)) {
			return false;
		}

		// 攻撃系スキルで対象が自分は対象外
		if (_skill.getType() == L1Skills.TYPE_ATTACK
				&& cha.getId() == _user.getId()) {
			return false;
		}

		// ターゲットが自分でH-Aの場合効果無し
		if (cha.getId() == _user.getId() && _skillId == HEAL_ALL) {
			return false;
		}

		if (((_skill.getTargetTo() & L1Skills.TARGET_TO_PC) == L1Skills.TARGET_TO_PC
				|| (_skill.getTargetTo() & L1Skills.TARGET_TO_CLAN) == L1Skills.TARGET_TO_CLAN || (_skill
				.getTargetTo() & L1Skills.TARGET_TO_PARTY) == L1Skills.TARGET_TO_PARTY)
				&& cha.getId() == _user.getId() && _skillId != HEAL_ALL) {
			return true; // ターゲットがパーティーかクラン員のものは自分に効果がある。（ただし、ヒールオールは除外）
		}

		// スキル使用者がPCで、PKモードではない場合、自分のサモン・ペットは対象外
		if (_user instanceof L1PcInstance
				&& (_skill.getTarget().equals("attack") || _skill.getType() == L1Skills.TYPE_ATTACK)
				&& _isPK == false) {
			if (cha instanceof L1SummonInstance) {
				L1SummonInstance summon = (L1SummonInstance) cha;
				if (_player.getId() == summon.getMaster().getId()) {
					return false;
				}
			} else if (cha instanceof L1PetInstance) {
				L1PetInstance pet = (L1PetInstance) cha;
				if (_player.getId() == pet.getMaster().getId()) {
					return false;
				}
			}
		}

		if ((_skill.getTarget().equals("attack")
				|| _skill.getType() == L1Skills.TYPE_ATTACK)
				&& !(cha instanceof L1MonsterInstance)
				&& _isPK == false
				&& _target instanceof L1PcInstance) {
			L1PcInstance enemy = (L1PcInstance) cha;
			// カウンターディテクション
			if (_skillId == COUNTER_DETECTION && enemy.getZoneType() != 1
					&& (cha.hasSkillEffect(INVISIBILITY)
							|| cha.hasSkillEffect(BLIND_HIDING))) {
				return true; // インビジかブラインドハイディング中
			}
			if (_player.getClanid() != 0 && enemy.getClanid() != 0) { // クラン所属中
				// 全戦争リストを取得
				for (L1War war : L1World.getInstance().getWarList()) {
					if (war.CheckClanInWar(_player.getClanname())) { // 自クランが戦争に参加中
						if (war.CheckClanInSameWar( // 同じ戦争に参加中
								_player.getClanname(), enemy.getClanname())) {
							if (L1CastleLocation.checkInAllWarArea(enemy.getX(),
									enemy.getY(), enemy.getMapId())) {
								return true;
							}
						}
					}
				}
			}
			return false; // 攻撃スキルでPKモードじゃない場合
		}

		if (_user.glanceCheck(cha.getX(), cha.getY()) == false
				&& _skill.isThrough() == false) {
			// エンチャント、復活スキルは障害物の判定をしない
			if (!(_skill.getType() == L1Skills.TYPE_CHANGE
					|| _skill.getType() == L1Skills.TYPE_RESTORE)) {
				_isGlanceCheckFail = true;
				return false; // 直線上に障害物がある
			}
		}

		if (cha.hasSkillEffect(ICE_LANCE)
				&& (_skillId == ICE_LANCE || _skillId == FREEZING_BLIZZARD
						|| _skillId == FREEZING_BREATH)) {
			return false; // アイスランス中にアイスランス、フリージングブリザード、フリージングブレス
		}

		if (cha.hasSkillEffect(FREEZING_BLIZZARD)
				&& (_skillId == ICE_LANCE || _skillId == FREEZING_BLIZZARD
						|| _skillId == FREEZING_BREATH)) {
			return false; // フリージングブリザード中にアイスランス、フリージングブリザード、フリージングブレス
		}

		if (cha.hasSkillEffect(FREEZING_BREATH)
				&& (_skillId == ICE_LANCE || _skillId == FREEZING_BLIZZARD
						|| _skillId == FREEZING_BREATH)) {
			return false; // フリージングブレス中にアイスランス、フリージングブリザード、フリージングブレス
		}

		if (cha.hasSkillEffect(EARTH_BIND) && _skillId == EARTH_BIND) {
			return false; // アース バインド中にアース バインド
		}

		if (!(cha instanceof L1MonsterInstance)
				&& (_skillId == TAMING_MONSTER || _skillId == CREATE_ZOMBIE)) {
			return false; // ターゲットがモンスターじゃない（テイミングモンスター）
		}
		if (cha.isDead()
				&& (_skillId != CREATE_ZOMBIE
				&& _skillId != RESURRECTION
				&& _skillId != GREATER_RESURRECTION
				&& _skillId != CALL_OF_NATURE)) {
			return false; // ターゲットが死亡している
		}

		if (cha.isDead() == false
				&& (_skillId == CREATE_ZOMBIE
				|| _skillId == RESURRECTION
				|| _skillId == GREATER_RESURRECTION
				|| _skillId == CALL_OF_NATURE)) {
			return false; // ターゲットが死亡していない
		}

		if ((cha instanceof L1TowerInstance || cha instanceof L1DoorInstance)
				&& (_skillId == CREATE_ZOMBIE
				|| _skillId == RESURRECTION
				|| _skillId == GREATER_RESURRECTION
				|| _skillId == CALL_OF_NATURE)) {
			return false; // ターゲットがガーディアンタワー、ドア
		}

		if (cha instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) cha;
			if (pc.hasSkillEffect(ABSOLUTE_BARRIER)) { // アブソルートバリア中
				if (_skillId == CURSE_BLIND || _skillId == WEAPON_BREAK
						|| _skillId == DARKNESS || _skillId == WEAKNESS
						|| _skillId == DISEASE || _skillId == FOG_OF_SLEEPING
						|| _skillId == MASS_SLOW || _skillId == SLOW
						|| _skillId == CANCELLATION || _skillId == SILENCE
						|| _skillId == DECAY_POTION || _skillId == MASS_TELEPORT
						|| _skillId == DETECTION
						|| _skillId == COUNTER_DETECTION
						|| _skillId == ERASE_MAGIC || _skillId == ENTANGLE
						|| _skillId == PHYSICAL_ENCHANT_DEX
						|| _skillId == PHYSICAL_ENCHANT_STR
						|| _skillId == BLESS_WEAPON || _skillId == EARTH_SKIN
						|| _skillId == IMMUNE_TO_HARM
						|| _skillId == REMOVE_CURSE) {
					return true;
				} else {
					return false;
				}
			}
		}

		if (cha instanceof L1NpcInstance) {
			int hiddenStatus = ((L1NpcInstance) cha).getHiddenStatus();
			if (hiddenStatus == L1NpcInstance.HIDDEN_STATUS_SINK) {
				if (_skillId == DETECTION || _skillId == COUNTER_DETECTION) { // ディテク、Cディテク
					return true;
				} else {
					return false;
				}
			} else if (hiddenStatus == L1NpcInstance.HIDDEN_STATUS_FLY) {
				return false;
			}
		}

		if ((_skill.getTargetTo() & L1Skills.TARGET_TO_PC) == L1Skills.TARGET_TO_PC // ターゲットがPC
				&& cha instanceof L1PcInstance) {
			_flg = true;
		} else if ((_skill.getTargetTo() & L1Skills.TARGET_TO_NPC) == L1Skills.TARGET_TO_NPC // ターゲットがNPC
				&& (cha instanceof L1MonsterInstance
						|| cha instanceof L1NpcInstance
						|| cha instanceof L1SummonInstance || cha instanceof L1PetInstance)) {
			_flg = true;
		} else if ((_skill.getTargetTo() & L1Skills.TARGET_TO_PET) == L1Skills
				.TARGET_TO_PET && _user instanceof L1PcInstance) { // ターゲットがSummon,Pet
			if (cha instanceof L1SummonInstance) {
				L1SummonInstance summon = (L1SummonInstance) cha;
				if (summon.getMaster() != null) {
					if (_player.getId() == summon.getMaster().getId()) {
						_flg = true;
					}
				}
			}
			if (cha instanceof L1PetInstance) {
				L1PetInstance pet = (L1PetInstance) cha;
				if (pet.getMaster() != null) {
					if (_player.getId() == pet.getMaster().getId()) {
						_flg = true;
					}
				}
			}
		}

		if (_calcType == PC_PC && cha instanceof L1PcInstance) {
			if ((_skill.getTargetTo() & L1Skills.TARGET_TO_CLAN) == L1Skills.TARGET_TO_CLAN
					&& ((_player.getClanid() != 0 // ターゲットがクラン員
					&& _player.getClanid() == ((L1PcInstance) cha).getClanid()) || _player
							.isGm())) {
				return true;
			}
			if ((_skill.getTargetTo() & L1Skills.TARGET_TO_PARTY) == L1Skills.TARGET_TO_PARTY
					&& (_player.getParty() // ターゲットがパーティー
							.isMember((L1PcInstance) cha) || _player.isGm())) {
				return true;
			}
		}

		return _flg;
	}

	// ターゲットの一覧を作成
	private void makeTargetList() {
		try {
			if (_type == TYPE_LOGIN) { // ログイン時(死亡時、お化け屋敷のキャンセレーション含む)は使用者のみ
				_targetList.add(new TargetStatus(_user));
				return;
			}
			if (_skill.getTargetTo() == L1Skills.TARGET_TO_ME
					&& (_skill.getType() & L1Skills.TYPE_ATTACK) != L1Skills.TYPE_ATTACK) {
				_targetList.add(new TargetStatus(_user)); // ターゲットは使用者のみ
				return;
			}

			// 射程距離-1の場合は画面内のオブジェクトが対象
			if (_skill.getRanged() != -1) {
				if (_user.getLocation().getTileLineDistance(
						_target.getLocation()) > _skill.getRanged()) {
					return; // 射程範囲外
				}
			} else {
				if (!_user.getLocation().isInScreen(_target.getLocation())) {
					return; // 射程範囲外
				}
			}

			if (isTarget(_target) == false
					&& !(_skill.getTarget().equals("none"))) {
				// 対象が違うのでスキルが発動しない。
				return;
			}

			if (_skillId == LIGHTNING || _skillId == FREEZING_BREATH) { // ライトニング、フリージングブレス直線的に範囲を決める
				ArrayList<L1Object> al1object = L1World.getInstance()
						.getVisibleLineObjects(_user, _target);

				for (L1Object tgobj : al1object) {
					if (tgobj == null) {
						continue;
					}
					if (!(tgobj instanceof L1Character)) { // ターゲットがキャラクター以外の場合何もしない。
						continue;
					}
					L1Character cha = (L1Character) tgobj;
					if (isTarget(cha) == false) {
						continue;
					}
					_targetList.add(new TargetStatus(cha));
				}
				return;
			}

			if (_skill.getArea() == 0) { // 単体の場合
				if (!_user.glanceCheck(_target.getX(), _target.getY())) { // 直線上に障害物があるか
					if ((_skill.getType() & L1Skills.TYPE_ATTACK) == L1Skills
							.TYPE_ATTACK && _skillId != 10026
							&& _skillId != 10027 && _skillId != 10028
							&& _skillId != 10029) { // 安息攻撃以外の攻撃スキル
						_targetList.add(new TargetStatus(_target, false)); // ダメージも発生しないし、ダメージモーションも発生しないが、スキルは発動
						return;
					}
				}
				_targetList.add(new TargetStatus(_target));
			} else { // 範囲の場合
				if (!_skill.getTarget().equals("none")) {
					_targetList.add(new TargetStatus(_target));
				}

				if (_skillId != 49
						&& !(_skill.getTarget().equals("attack") || _skill
								.getType() == L1Skills.TYPE_ATTACK)) {
					// 攻撃系以外のスキルとH-A以外はターゲット自身を含める
					_targetList.add(new TargetStatus(_user));
				}

				List<L1Object> objects;
				if (_skill.getArea() == -1) {
					objects = L1World.getInstance()
							.getVisibleObjects(_user);
				} else {
					objects = L1World.getInstance()
							.getVisibleObjects(_target, _skill.getArea());
				}
				for (L1Object tgobj : objects) {
					if (tgobj == null) {
						continue;
					}
					if (!(tgobj instanceof L1Character)) { // ターゲットがキャラクター以外の場合何もしない。
						continue;
					}
					L1Character cha = (L1Character) tgobj;
					if (!isTarget(cha)) {
						continue;
					}

					_targetList.add(new TargetStatus(cha));
				}
				return;
			}

		} catch (Exception e) {
			_log.finest("exception in L1Skilluse makeTargetList" + e);
		}
	}

	// メッセージの表示（何か起こったとき）
	private void sendHappenMessage(L1PcInstance pc) {
		int msgID = _skill.getSysmsgIdHappen();
		if (msgID > 0) {
			pc.sendPackets(new S_ServerMessage(msgID));
		}
	}

	// 失敗メッセージ表示のハンドル
	private void sendFailMessageHandle() {
		// 攻撃スキル以外で対象を指定するスキルが失敗した場合は失敗したメッセージをクライアントに送信
		// ※攻撃スキルは障害物があっても成功時と同じアクションであるべき。
		if (_skill.getType() != L1Skills.TYPE_ATTACK
				&& !_skill.getTarget().equals("none")
				&& _targetList.size() == 0) {
			sendFailMessage();
		}
	}

	// メッセージの表示（失敗したとき）
	private void sendFailMessage() {
		int msgID = _skill.getSysmsgIdFail();
		if (msgID > 0 && (_user instanceof L1PcInstance)) {
			_player.sendPackets(new S_ServerMessage(msgID));
		}
	}

	// 精霊魔法の属性と使用者の属性は一致するか？（とりあえずの対処なので、対応できたら消去して下さい)
	private boolean isAttrAgrees() {
		int magicattr = _skill.getAttr();
		if (_user instanceof L1NpcInstance) { // NPCが使った場合なんでもOK
			return true;
		}

		if (_skill.getSkillLevel() >= 17 && _skill.getSkillLevel() <= 22
				&& magicattr != 0 // 精霊魔法で、無属性魔法ではなく、
				&& magicattr != _player.getElfAttr() // 使用者と魔法の属性が一致しない。
				&& !_player.isGm()) { // ただしGMは例外
			return false;
		}
		return true;
	}

	/**
	 * スキルを使用するために必要なHPがあるか返す。
	 * 
	 * @return HPが十分であればtrue
	 */
	private boolean isEnoughHp() {
		return false;
	}

	/**
	 * スキルを使用するために必要なMPがあるか返す。
	 * 
	 * @return MPが十分であればtrue
	 */
	private boolean isEnoughMp() {
		return false;
	}

	// 必要ＨＰ、ＭＰがあるか？
	private boolean isHPMPConsume() {
		_mpConsume = _skill.getMpConsume();
		_hpConsume = _skill.getHpConsume();
		int currentMp = 0;
		int currentHp = 0;

		if (_user instanceof L1NpcInstance) {
			currentMp = _npc.getCurrentMp();
			currentHp = _npc.getCurrentHp();
		} else {
			currentMp = _player.getCurrentMp();
			currentHp = _player.getCurrentHp();

			// MPのINT軽減
			if (_player.getInt() > 12
					&& _skillId > HOLY_WEAPON
					&& _skillId <= FREEZING_BLIZZARD) { // LV2以上
				_mpConsume--;
			}
			if (_player.getInt() > 13
					&& _skillId > STALAC
					&& _skillId <= FREEZING_BLIZZARD) { // LV3以上
				_mpConsume--;
			}
			if (_player.getInt() > 14
					&& _skillId > WEAK_ELEMENTAL
					&& _skillId <= FREEZING_BLIZZARD) { // LV4以上
				_mpConsume--;
			}
			if (_player.getInt() > 15
					&& _skillId > MEDITATION
					&& _skillId <= FREEZING_BLIZZARD) { // LV5以上
				_mpConsume--;
			}
			if (_player.getInt() > 16
					&& _skillId > DARKNESS
					&& _skillId <= FREEZING_BLIZZARD) { // LV6以上
				_mpConsume--;
			}
			if (_player.getInt() > 17
					&& _skillId > BLESS_WEAPON
					&& _skillId <= FREEZING_BLIZZARD) { // LV7以上
				_mpConsume--;
			}
			if (_player.getInt() > 18
					&& _skillId > DISEASE
					&& _skillId <= FREEZING_BLIZZARD) { // LV8以上
				_mpConsume--;
			}

			if (_player.getInt() > 12
					&& _skillId >= SHOCK_STUN && _skillId <= COUNTER_BARRIER) {
				_mpConsume -= (_player.getInt() - 12);
			}

			// MPの装備軽減
			if (_skillId == PHYSICAL_ENCHANT_DEX
					&& _player.getInventory().checkEquipped(20013)) { // 迅速ヘルム装備中にPE:DEX
				_mpConsume /= 2;
			}
			if (_skillId == HASTE
					&& _player.getInventory().checkEquipped(20013)) { // 迅速ヘルム装備中にヘイスト
				_mpConsume /= 2;
			}
			if (_skillId == HEAL
					&& _player.getInventory().checkEquipped(20014)) { // 治癒ヘルム装備中にヒール
				_mpConsume /= 2;
			}
			if (_skillId == EXTRA_HEAL
					&& _player.getInventory().checkEquipped(20014)) { // 治癒ヘルム装備中にエキストラヒール
				_mpConsume /= 2;
			}
			if (_skillId == ENCHANT_WEAPON
					&& _player.getInventory().checkEquipped(20015)) { // 力ヘルム装備中にエンチャントウエポン
				_mpConsume /= 2;
			}
			if (_skillId == DETECTION
					&& _player.getInventory().checkEquipped(20015)) { // 力ヘルム装備中にディテクション
				_mpConsume /= 2;
			}
			if (_skillId == PHYSICAL_ENCHANT_STR
					&& _player.getInventory().checkEquipped(20015)) { // 力ヘルム装備中にPE:STR
				_mpConsume /= 2;
			}
			if (_skillId == HASTE
					&& _player.getInventory().checkEquipped(20008)) { // マイナーウィンドヘルム装備中にヘイスト
				_mpConsume /= 2;
			}
			if (_skillId == GREATER_HASTE
					&& _player.getInventory().checkEquipped(20023)) { // ウィンドヘルム装備中にグレーターヘイスト
				_mpConsume /= 2;
			}

			if (0 < _skill.getMpConsume()) { // MPを消費するスキルであれば
				_mpConsume = Math.max(_mpConsume, 1); // 最低でも1消費する。
			}

			// MPのオリジナルINT軽減
			if (_player.getOriginalMagicConsumeReduction() > 0) {
				_mpConsume -= _player.getOriginalMagicConsumeReduction();
			}
		}

		if (currentHp < _hpConsume + 1) {
			if (_user instanceof L1PcInstance) {
				_player.sendPackets(new S_ServerMessage(279));
			}
			return false;
		} else if (currentMp < _mpConsume) {
			if (_user instanceof L1PcInstance) {
				_player.sendPackets(new S_ServerMessage(278));
			}
			return false;
		}

		return true;
	}

	// 必要材料があるか？
	private boolean isItemConsume() {

		int itemConsume = _skill.getItemConsumeId();
		int itemConsumeCount = _skill.getItemConsumeCount();

		if (itemConsume == 0) {
			return true; // 材料を必要としない魔法
		}

		if (!_player.getInventory().checkItem(itemConsume, itemConsumeCount)) {
			return false; // 必要材料が足りなかった。
		}

		return true;
	}

	// 使用材料、HP・MP、Lawfulをマイナスする。
	private void useConsume() {
		if (_user instanceof L1NpcInstance) {
			// NPCの場合、HP、MPのみマイナス
			int current_hp = _npc.getCurrentHp() - _hpConsume;
			_npc.setCurrentHp(current_hp);

			int current_mp = _npc.getCurrentMp() - _mpConsume;
			_npc.setCurrentMp(current_mp);
			return;
		}

		// HP・MPをマイナス
		if (isHPMPConsume()) {
			if (_skillId == FINAL_BURN) { // ファイナル バーン
				_player.setCurrentHp(1);
				_player.setCurrentMp(0);
			} else {
				int current_hp = _player.getCurrentHp() - _hpConsume;
				_player.setCurrentHp(current_hp);

				int current_mp = _player.getCurrentMp() - _mpConsume;
				_player.setCurrentMp(current_mp);
			}
		}

		// Lawfulをマイナス
		int lawful = _player.getLawful() + _skill.getLawful();
		if (lawful > 32767) {
			lawful = 32767;
		}
		if (lawful < -32767) {
			lawful = -32767;
		}
		_player.setLawful(lawful);

		int itemConsume = _skill.getItemConsumeId();
		int itemConsumeCount = _skill.getItemConsumeCount();

		if (itemConsume == 0) {
			return; // 材料を必要としない魔法
		}

		// 使用材料をマイナス
		_player.getInventory().consumeItem(itemConsume, itemConsumeCount);
	}

	// マジックリストに追加する。
	private void addMagicList(L1Character cha, boolean repetition) {
		if (_skillTime == 0) {
			_getBuffDuration = _skill.getBuffDuration() * 1000; // 効果時間
			if (_skill.getBuffDuration() == 0) {
				if (_skillId == INVISIBILITY) { // インビジビリティ
					cha.setSkillEffect(INVISIBILITY, 0);
				}
				return;
			}
		} else {
			_getBuffDuration = _skillTime * 1000; // パラメータのtimeが0以外なら、効果時間として設定する
		}

		if (_skillId == SHOCK_STUN) {
			_getBuffDuration = _shockStunDuration;
		}

		if (_skillId == CURSE_POISON) { // カーズポイズンの効果処理はL1Poisonに移譲。
			return;
		}
		if (_skillId == CURSE_PARALYZE
				|| _skillId == CURSE_PARALYZE2) { // カーズパラライズの効果処理はL1CurseParalysisに移譲。
			return;
		}
		if (_skillId == SHAPE_CHANGE) { // シェイプチェンジの効果処理はL1PolyMorphに移譲。
			return;
		}
		if (_skillId == BLESSED_ARMOR || _skillId == HOLY_WEAPON // 武器・防具に効果がある処理はL1ItemInstanceに移譲。
				|| _skillId == ENCHANT_WEAPON || _skillId == BLESS_WEAPON
				|| _skillId == SHADOW_FANG) {
			return;
		}
		if ((_skillId == ICE_LANCE || _skillId == FREEZING_BLIZZARD
				|| _skillId == FREEZING_BREATH) && !_isFreeze) { // 凍結失敗
			return;
		}
		if (_skillId == AWAKEN_ANTHARAS || _skillId == AWAKEN_FAFURION
				|| _skillId == AWAKEN_VALAKAS) { // 覚醒の効果処理はL1Awakeに移譲。
			return;
		}
		cha.setSkillEffect(_skillId, _getBuffDuration);

		if (cha instanceof L1PcInstance && repetition) { // 対象がPCで既にスキルが重複している場合
			L1PcInstance pc = (L1PcInstance) cha;
			sendIcon(pc);
		}
	}

	// アイコンの送信
	private void sendIcon(L1PcInstance pc) {
		if (_skillTime == 0) {
			_getBuffIconDuration = _skill.getBuffDuration(); // 効果時間
		} else {
			_getBuffIconDuration = _skillTime; // パラメータのtimeが0以外なら、効果時間として設定する
		}

		if (_skillId == SHIELD) { // シールド
			pc.sendPackets(new S_SkillIconShield(5, _getBuffIconDuration));
		} else if (_skillId == SHADOW_ARMOR) { // シャドウ アーマー
			pc.sendPackets(new S_SkillIconShield(3, _getBuffIconDuration));
		} else if (_skillId == DRESS_DEXTERITY) { // ドレス デクスタリティー
			pc.sendPackets(new S_Dexup(pc, 2, _getBuffIconDuration));
		} else if (_skillId == DRESS_MIGHTY) { // ドレス マイティー
			pc.sendPackets(new S_Strup(pc, 2, _getBuffIconDuration));
		} else if (_skillId == GLOWING_AURA) { // グローウィング オーラ
			pc.sendPackets(new S_SkillIconAura(113, _getBuffIconDuration));
		} else if (_skillId == SHINING_AURA) { // シャイニング オーラ
			pc.sendPackets(new S_SkillIconAura(114, _getBuffIconDuration));
		} else if (_skillId == BRAVE_AURA) { // ブレイブ オーラ
			pc.sendPackets(new S_SkillIconAura(116, _getBuffIconDuration));
		} else if (_skillId == FIRE_WEAPON) { // ファイアー ウェポン
			pc.sendPackets(new S_SkillIconAura(147, _getBuffIconDuration));
		} else if (_skillId == WIND_SHOT) { // ウィンド ショット
			pc.sendPackets(new S_SkillIconAura(148, _getBuffIconDuration));
		} else if (_skillId == FIRE_BLESS) { // ファイアー ブレス
			pc.sendPackets(new S_SkillIconAura(154, _getBuffIconDuration));
		} else if (_skillId == STORM_EYE) { // ストーム アイ
			pc.sendPackets(new S_SkillIconAura(155, _getBuffIconDuration));
		} else if (_skillId == EARTH_BLESS) { // アース ブレス
			pc.sendPackets(new S_SkillIconShield(7, _getBuffIconDuration));
		} else if (_skillId == BURNING_WEAPON) { // バーニング ウェポン
			pc.sendPackets(new S_SkillIconAura(162, _getBuffIconDuration));
		} else if (_skillId == STORM_SHOT) { // ストーム ショット
			pc.sendPackets(new S_SkillIconAura(165, _getBuffIconDuration));
		} else if (_skillId == IRON_SKIN) { // アイアン スキン
			pc.sendPackets(new S_SkillIconShield(10, _getBuffIconDuration));
		} else if (_skillId == EARTH_SKIN) { // アース スキン
			pc.sendPackets(new S_SkillIconShield(6, _getBuffIconDuration));
		} else if (_skillId == PHYSICAL_ENCHANT_STR) { // フィジカル エンチャント：STR
			pc.sendPackets(new S_Strup(pc, 5, _getBuffIconDuration));
		} else if (_skillId == PHYSICAL_ENCHANT_DEX) { // フィジカル エンチャント：DEX
			pc.sendPackets(new S_Dexup(pc, 5, _getBuffIconDuration));
		} else if (_skillId == HASTE || _skillId == GREATER_HASTE) { // グレーターヘイスト
			pc.sendPackets(new S_SkillHaste(pc.getId(), 1,
					_getBuffIconDuration));
			pc.broadcastPacket(new S_SkillHaste(pc.getId(), 1, 0));
		} else if (_skillId == HOLY_WALK
				|| _skillId == MOVING_ACCELERATION || _skillId == WIND_WALK) { // ホーリーウォーク、ムービングアクセレーション、ウィンドウォーク
			pc.sendPackets(new S_SkillBrave(pc.getId(), 4,
					_getBuffIconDuration));
			pc.broadcastPacket(new S_SkillBrave(pc.getId(), 4, 0));
		} else if (_skillId == SLOW
				|| _skillId == MASS_SLOW || _skillId == ENTANGLE) { // スロー、エンタングル、マススロー
			pc.sendPackets(new S_SkillHaste(pc.getId(), 2,
					_getBuffIconDuration));
			pc.broadcastPacket(new S_SkillHaste(pc.getId(), 2, 0));
		} else if (_skillId == IMMUNE_TO_HARM) {
			pc.sendPackets(new S_SkillIconGFX(40, _getBuffIconDuration));
		}
		pc.sendPackets(new S_OwnCharStatus(pc));
	}

	// グラフィックの送信
	private void sendGrfx(boolean isSkillAction) {
		if (_actid == 0) {
			_actid = _skill.getActionId();
		}
		if (_gfxid == 0) {
			_gfxid = _skill.getCastGfx();
		}
		if (_gfxid == 0) {
			return;
		}
		
		if (_user instanceof L1PcInstance) {
			
			int targetid = 0;
			if (_skillId != FIRE_WALL) {
				targetid = _target.getId();
			}
			L1PcInstance pc = (L1PcInstance) _user;
			
			switch(_skillId) {
				case FIRE_WALL: 
				case LIFE_STREAM: 
				case ELEMENTAL_FALL_DOWN: 
					if (_skillId == FIRE_WALL) {
						pc.setHeading(pc.targetDirection(_targetX, _targetY));
						pc.sendPackets(new S_ChangeHeading(pc));
						pc.broadcastPacket(new S_ChangeHeading(pc));
					}
					S_DoActionGFX gfx = new S_DoActionGFX(pc.getId(), _actid);
					pc.sendPackets(gfx);
					pc.broadcastPacket(gfx);
					return;
				case SHOCK_STUN: 
					if (_targetList.isEmpty()) { 
						return;
					} else {
						if (_target instanceof L1PcInstance) {
							L1PcInstance targetPc = (L1PcInstance) _target;
							targetPc.sendPackets(new S_SkillSound(targetid, 4434));
							targetPc.broadcastPacket(new S_SkillSound(targetid, 4434));
						} else if (_target instanceof L1NpcInstance) {
							_target.broadcastPacket(new S_SkillSound(targetid, 4434));
						}
						return;
					}
				case LIGHT:
					pc.sendPackets(new S_Sound(145));
					break;
				case MIND_BREAK:
				case JOY_OF_PAIN: 
					pc.sendPackets(new S_AttackPacket(pc, targetid, _actid));
					pc.broadcastPacket(new S_AttackPacket(pc, targetid, _actid));
					pc.sendPackets(new S_SkillSound(targetid, _gfxid));
					pc.broadcastPacket(new S_SkillSound(targetid, _gfxid));
					return;
				case CONFUSION: 
					pc.sendPackets(new S_AttackPacket(pc, targetid, _actid));
					pc.broadcastPacket(new S_AttackPacket(pc, targetid, _actid));
					return;
				case SMASH: 
					pc.sendPackets(new S_SkillSound(targetid, _gfxid));
					pc.broadcastPacket(new S_SkillSound(targetid, _gfxid));
					return;
				case TAMING_MONSTER:
					pc.sendPackets(new S_EffectLocation(_targetX, _targetY, _gfxid));
					pc.broadcastPacket(new S_EffectLocation(_targetX, _targetY, _gfxid));
					return;
				default:
					break;
			}
			
			if (_targetList.isEmpty() && !(_skill.getTarget().equals("none"))) {
				// ターゲット数が０で対象を指定するスキルの場合、魔法使用エフェクトだけ表示して終了
				int tempchargfx = _player.getTempCharGfx();
				if (tempchargfx == 5727 || tempchargfx == 5730) { // シャドウ系変身のモーション対応
					_actid = ActionCodes.ACTION_SkillBuff;
				} else if (tempchargfx == 5733 || tempchargfx == 5736) {
					_actid = ActionCodes.ACTION_Attack;
				}
				if (isSkillAction) {
					S_DoActionGFX gfx = new S_DoActionGFX(_player.getId(), _actid);
					_player.sendPackets(gfx);
					_player.broadcastPacket(gfx);
				}
				return;
			}
			if (_skill.getTarget().equals("attack") && _skillId != 18) {
				if (isPcSummonPet(_target)) { // 対象がPC、サモン、ペット
					if (_player.getZoneType() == 1
							|| _target.getZoneType() == 1 // 攻撃する側または攻撃される側がセーフティーゾーン
							|| _player.checkNonPvP(_player, _target)) { // Non-PvP設定
						_player.sendPackets(new S_UseAttackSkill(_player, 0, _gfxid, _targetX, _targetY, _actid)); // ターゲットへのモーションはなし
						_player.broadcastPacket(new S_UseAttackSkill(_player, 0, _gfxid, _targetX, _targetY, _actid));
						return;
					}
				}

				if (_skill.getArea() == 0) { // 単体攻撃魔法
					_player.sendPackets(new S_UseAttackSkill(_player, targetid, _gfxid, _targetX, _targetY, _actid));
					_player.broadcastPacket(new S_UseAttackSkill(_player, targetid,  _gfxid, _targetX, _targetY, _actid));
					_target.broadcastPacketExceptTargetSight(new S_DoActionGFX(targetid, ActionCodes.ACTION_Damage), _player);
				} else { // 有方向範囲攻撃魔法
					L1Character[] cha = new L1Character[_targetList.size()];
					int i = 0;
					for (TargetStatus ts : _targetList) {
						cha[i] = ts.getTarget();
						i++;
					}
					_player.sendPackets(new S_RangeSkill(_player, cha, _gfxid, _actid, S_RangeSkill.TYPE_DIR));
					_player.broadcastPacket(new S_RangeSkill(_player, cha, _gfxid, _actid, S_RangeSkill.TYPE_DIR));
				}
			} else if (_skill.getTarget().equals("none") && _skill.getType() ==
					L1Skills.TYPE_ATTACK) { // 無方向範囲攻撃魔法
				L1Character[] cha = new L1Character[_targetList.size()];
				int i = 0;
				for (TargetStatus ts : _targetList) {
					cha[i] = ts.getTarget();
					cha[i].broadcastPacketExceptTargetSight(new S_DoActionGFX(
							cha[i].getId(), ActionCodes.ACTION_Damage),
									_player);
					i++;
				}
				_player.sendPackets(new S_RangeSkill(_player, cha, _gfxid,
						_actid, S_RangeSkill.TYPE_NODIR));
				_player.broadcastPacket(new S_RangeSkill(_player, cha,
						_gfxid, _actid, S_RangeSkill.TYPE_NODIR));
			} else { // 補助魔法
				// テレポート、マステレ、テレポートトゥマザー以外
				if (_skillId != 5 && _skillId != 69 && _skillId != 131) {
					// 魔法を使う動作のエフェクトは使用者だけ
					if (isSkillAction) {
						S_DoActionGFX gfx = new S_DoActionGFX(_player.getId(),
								_skill.getActionId());
						_player.sendPackets(gfx);
						_player.broadcastPacket(gfx);
					}
					if (_skillId == COUNTER_MAGIC
							|| _skillId == COUNTER_BARRIER
							|| _skillId == COUNTER_MIRROR) {
						_player.sendPackets(new S_SkillSound(targetid,
								_gfxid));
					} else if (_skillId == TRUE_TARGET) { // トゥルーターゲットは個別処理で送信済
						return;
					} else if (_skillId == AWAKEN_ANTHARAS // 覚醒：アンタラス
							|| _skillId == AWAKEN_FAFURION // 覚醒：パプリオン
							|| _skillId == AWAKEN_VALAKAS) { // 覚醒：ヴァラカス
						if (_skillId == _player.getAwakeSkillId()) { // 再詠唱なら解除でエフェクトなし
							_player.sendPackets(new S_SkillSound(targetid,
									_gfxid));
							_player.broadcastPacket(new S_SkillSound(targetid,
									_gfxid));
						} else {
							return;
						}
					} else {
						_player.sendPackets(new S_SkillSound(targetid,
								_gfxid));
						_player.broadcastPacket(new S_SkillSound(targetid,
								_gfxid));
					}
				}
				// スキルのエフェクト表示はターゲット全員だが、あまり必要性がないので、ステータスのみ送信
				for (TargetStatus ts : _targetList) {
					L1Character cha = ts.getTarget();
					if (cha instanceof L1PcInstance) {
						L1PcInstance tpc = (L1PcInstance) cha;
						tpc.sendPackets(new S_OwnCharStatus(pc));
					}
				}
			}
		} else if (_user instanceof L1NpcInstance) { // NPCがスキルを使った場合
			int targetid = _target.getId();

			if (_user instanceof L1MerchantInstance) {
				_user.broadcastPacket(new S_SkillSound(targetid, _gfxid));
				return;
			}

			if (_targetList.size() == 0 && !(_skill.getTarget()
					.equals("none"))) {
				// ターゲット数が０で対象を指定するスキルの場合、魔法使用エフェクトだけ表示して終了
				S_DoActionGFX gfx = new S_DoActionGFX(_user.getId(), _skill
						.getActionId());
				_user.broadcastPacket(gfx);
				return;
			}

			if (_skill.getTarget().equals("attack") && _skillId != 18) {
				if (_skill.getArea() == 0) { // 単体攻撃魔法
					_user.broadcastPacket(new S_UseAttackSkill(_user, targetid,
							_gfxid, _targetX, _targetY, _actid));
					_target.broadcastPacketExceptTargetSight(new S_DoActionGFX(
							targetid, ActionCodes.ACTION_Damage), _user);
				} else { // 有方向範囲攻撃魔法
					L1Character[] cha = new L1Character[_targetList.size()];
					int i = 0;
					for (TargetStatus ts : _targetList) {
						cha[i] = ts.getTarget();
						cha[i].broadcastPacketExceptTargetSight(
								new S_DoActionGFX(cha[i].getId(), ActionCodes
										.ACTION_Damage), _user);
						i++;
					}
					_user.broadcastPacket(new S_RangeSkill(_user, cha,
							_gfxid, _actid, S_RangeSkill.TYPE_DIR));
				}
			} else if (_skill.getTarget().equals("none") && _skill.getType() ==
					L1Skills.TYPE_ATTACK) { // 無方向範囲攻撃魔法
				L1Character[] cha = new L1Character[_targetList.size()];
				int i = 0;
				for (TargetStatus ts : _targetList) {
					cha[i] = ts.getTarget();
					i++;
				}
				_user.broadcastPacket(new S_RangeSkill(_user, cha, _gfxid,
						_actid, S_RangeSkill.TYPE_NODIR));
			} else { // 補助魔法
				// テレポート、マステレ、テレポートトゥマザー以外
				if (_skillId != 5 && _skillId != 69 && _skillId != 131) {
					// 魔法を使う動作のエフェクトは使用者だけ
					S_DoActionGFX gfx = new S_DoActionGFX(_user.getId(), _skill
							.getActionId());
					_user.broadcastPacket(gfx);
					_user.broadcastPacket(new S_SkillSound(targetid, _gfxid));
				}
			}
		}
	}

	// 重複できないスキルの削除
	// 例：ファイア ウェポンとバーニングウェポンなど
	private void deleteRepeatedSkills(L1Character cha) {
		final int[][] repeatedSkills = {
				// ホーリー ウェポン、エンチャント ウェポン、ブレス ウェポン, シャドウ ファング
				// これらはL1ItemInstanceで管理
// { HOLY_WEAPON, ENCHANT_WEAPON, BLESS_WEAPON, SHADOW_FANG },
				// ファイアー ウェポン、ウィンド ショット、ファイアー ブレス、ストーム アイ、バーニング ウェポン、ストーム ショット
				{ FIRE_WEAPON, WIND_SHOT, FIRE_BLESS, STORM_EYE,
						BURNING_WEAPON, STORM_SHOT },
				// シールド、シャドウ アーマー、アース スキン、アースブレス、アイアン スキン
				{ SHIELD, SHADOW_ARMOR, EARTH_SKIN, EARTH_BLESS, IRON_SKIN },
				// ホーリー ウォーク、ムービング アクセレーション、ウィンド ウォーク、BP
				{ HOLY_WALK, MOVING_ACCELERATION, WIND_WALK, STATUS_BRAVE,
						STATUS_ELFBRAVE, STATUS_RIBRAVE },
				// ヘイスト、グレーター ヘイスト、GP
				{ HASTE, GREATER_HASTE, STATUS_HASTE },
				// フィジカル エンチャント：DEX、ドレス デクスタリティー
				{ PHYSICAL_ENCHANT_DEX, DRESS_DEXTERITY },
				// フィジカル エンチャント：STR、ドレス マイティー
				{ PHYSICAL_ENCHANT_STR, DRESS_MIGHTY },
				// グローウィングオーラ、シャイニングオーラ
				{ GLOWING_AURA, SHINING_AURA } };

		for (int[] skills : repeatedSkills) {
			for (int id : skills) {
				if (id == _skillId) {
					stopSkillList(cha, skills);
				}
			}
		}
	}

	// 重複しているスキルを一旦すべて削除
	private void stopSkillList(L1Character cha, int[] repeat_skill) {
		for (int skillId : repeat_skill) {
			if (skillId != _skillId) {
				cha.removeSkillEffect(skillId);
			}
		}
	}

	// ディレイの設定
	private void setDelay() {
		if (_skill.getReuseDelay() > 0) {
			L1SkillDelay.onSkillUse(_user, _skill.getReuseDelay());
		}
	}

	private void runSkill() {
		switch(_skillId){
			case LIFE_STREAM:
				//治癒能量風暴
				L1EffectSpawn.getInstance().spawnEffect(81169, _skill.getBuffDuration() * 1000, _targetX, _targetY, _user.getMapId());
				return;
			case CUBE_IGNITION:
				//立方燃燒
				L1EffectSpawn.getInstance().spawnEffect(80149, _skill.getBuffDuration() * 1000, _targetX, _targetY, _user.getMapId(), (L1PcInstance) _user, _skillId);
				return;
			case CUBE_QUAKE:
				//立方地裂
				L1EffectSpawn.getInstance().spawnEffect(80150, _skill.getBuffDuration() * 1000, _targetX, _targetY, _user.getMapId(), (L1PcInstance) _user, _skillId);
				return;
			case CUBE_SHOCK:
				//立方衝擊
				L1EffectSpawn.getInstance().spawnEffect(80151, _skill.getBuffDuration() * 1000, _targetX, _targetY, _user.getMapId(), (L1PcInstance) _user, _skillId);
				return;
			case CUBE_BALANCE:
				//立方和諧
				L1EffectSpawn.getInstance().spawnEffect(80152, _skill.getBuffDuration() * 1000, _targetX, _targetY, _user.getMapId(), (L1PcInstance) _user, _skillId);
				return;
		}

		if (_skillId == FIRE_WALL) { 
			//火牢
			L1EffectSpawn.getInstance().doSpawnFireWall(_user, _targetX, _targetY);
			return;
		}
		for (int skillId : EXCEPT_COUNTER_MAGIC) {
			if (_skillId == skillId) {
				_isCounterMagic = false;
				break;
			}
		}
		if (_skillId == SHOCK_STUN && _user instanceof L1PcInstance) {
			_target.onAction(_player);
		}

		if (!isTargetCalc(_target)) {
			return;
		}
		try {
			TargetStatus ts = null;
			L1Character cha = null;
			int dmg = 0;
			int drainMana = 0;
			int heal = 0;
			boolean isSuccess = false;
			int undeadType = 0;
			
			for (Iterator<TargetStatus> iter = _targetList.iterator(); iter.hasNext();) {
				ts = null;
				cha = null;
				dmg = 0;
				heal = 0;
				isSuccess = false;
				undeadType = 0;

				ts = iter.next();
				cha = ts.getTarget();

				if (!ts.isCalc() || !isTargetCalc(cha)) {
					continue; 
				}
				if (_user instanceof L1PetInstance || _user instanceof L1SummonInstance) {
					L1NpcInstance u = (L1NpcInstance)_user;
					if (u.getMaster() == cha) {
						continue;
					}
					if (cha instanceof L1PetInstance || cha instanceof L1SummonInstance) {
						if (u.getMaster() == ((L1NpcInstance)cha).getMaster()){
							continue;
						}
					}
				}
				L1Magic _magic = new L1Magic(_user, cha);
				_magic.setLeverage(getLeverage());

				if (cha instanceof L1MonsterInstance) { 
					undeadType = ((L1MonsterInstance) cha).getNpcTemplate().get_undead();
				}
				if ((_skill.getType() == L1Skills.TYPE_CURSE || _skill.getType() == L1Skills.TYPE_PROBABILITY) && isTargetFailure(cha)) {
					iter.remove();
					continue;
				}

				if (cha instanceof L1PcInstance) {
					if (_skillTime == 0) {
						_getBuffIconDuration = _skill.getBuffDuration(); 
					} else {
						_getBuffIconDuration = _skillTime; 
					}
				}
				deleteRepeatedSkills(cha); 
				if (_skill.getType() == L1Skills.TYPE_ATTACK && _user.getId() != cha.getId()) { 
					
					if (isUseCounterMagic(cha)) {
						iter.remove();
						continue;
					}
					if(_user instanceof L1PcInstance){
						//add by sdcom 2009.11.26
						L1PcInstance pc = (L1PcInstance) _user;
						dmg = _magic.calcMagicDamage(_skillId,pc);
						//add by sdcom 2010.8.30
						if(_skill.getSkillLevel()>=4 && _skill.getSkillLevel()<10){
							dmg *= (_skill.getSkillLevel()-4)+2;
						}else if(_skill.getSkillLevel()==10){
							dmg *= 10;
						}
					}else{
						dmg = _magic.calcMagicDamage(_skillId);
					}
					cha.removeSkillEffect(ERASE_MAGIC); 
				} else if (_skill.getType() == L1Skills.TYPE_CURSE || _skill.getType() == L1Skills.TYPE_PROBABILITY) { 
					isSuccess = _magic.calcProbabilityMagic(_skillId);
					if (_skillId != ERASE_MAGIC) {
						cha.removeSkillEffect(ERASE_MAGIC); 
					}
					if (_skillId != FOG_OF_SLEEPING) {
						cha.removeSkillEffect(FOG_OF_SLEEPING); 
					}
					if (isSuccess) { 
						if (isUseCounterMagic(cha)) { 
							iter.remove();
							continue;
						}
					} else {
						if (_skillId == FOG_OF_SLEEPING
								&& cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;
							pc.sendPackets(new S_ServerMessage(297)); 
						}
						iter.remove();
						continue;
					}
				} else if (_skill.getType() == L1Skills.TYPE_HEAL) {
					dmg = -1 * _magic.calcHealing(_skillId);
					if (cha.hasSkillEffect(WATER_LIFE)) {
						//水之元氣
						dmg *= 2;
					}
					if (cha.hasSkillEffect(POLLUTE_WATER)) {
						//污濁之水
						dmg /= 2;
					}
					if(_user instanceof L1PcInstance){
						//add by sdcom 2009.12.3
						dmg *= _user.getInt()/10;
					}
				}

				if (cha.hasSkillEffect(_skillId) && _skillId != SHOCK_STUN) {
					//衝擊之暈
					addMagicList(cha, true);
					if (_skillId != SHAPE_CHANGE) { 
						//變形術
						continue;
					}
				}
				//TODO start use skill id
				if (_skillId == HASTE) { 
					//加速術
					if (cha.getMoveSpeed() != 2) { 
						if (cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;
							if (pc.getHasteItemEquipped() > 0) {
								continue;
							}
							pc.setDrink(false);
							pc.sendPackets(new S_SkillHaste(pc.getId(), 1, _getBuffIconDuration));
						}
						cha.broadcastPacket(new S_SkillHaste(cha.getId(), 1, 0));
						cha.setMoveSpeed(1);
					} else {
						int skillNum = 0;
						if (cha.hasSkillEffect(SLOW)) {
							//緩速術
							skillNum = SLOW;
						} else if (cha.hasSkillEffect(MASS_SLOW)) {
							//集體緩速術
							skillNum = MASS_SLOW;
						} else if (cha.hasSkillEffect(ENTANGLE)) {
							//地面障礙
							skillNum = ENTANGLE;
						}
						if (skillNum != 0) {
							cha.removeSkillEffect(skillNum);
							cha.removeSkillEffect(HASTE);
							cha.setMoveSpeed(0);
							continue;
						}
					}
				} else if (_skillId == CURE_POISON) {
					//解毒術
					cha.curePoison();
				} else if (_skillId == REMOVE_CURSE) {
					//聖潔之光
					if (cha.hasSkillEffect(STATUS_CURSE_PARALYZED) //木乃尹狀態
						||cha.hasSkillEffect(SHOCK_STUN) //衝暈
						||cha.hasSkillEffect(FOG_OF_SLEEPING) //沉睡之霧
						||cha.hasSkillEffect(ICE_LANCE)){ //冰茅
						_player.sendPackets(new S_ServerMessage(285));
						//285 : \f1在此狀態下無法使用魔法。
						return;
					} 
					cha.curePoison();
					if (cha.hasSkillEffect(STATUS_CURSE_PARALYZING)	|| cha.hasSkillEffect(STATUS_CURSE_PARALYZED)) {
						cha.cureParalaysis();
					}
				} else if (_skillId == RESURRECTION || _skillId == GREATER_RESURRECTION) { 
					//返生術,終極返生術
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						if (_player.getId() != pc.getId()) {
							if (L1World.getInstance().getVisiblePlayer(pc,0).size() > 0) {
								for (L1PcInstance visiblePc : L1World.getInstance().getVisiblePlayer(pc,0)) {
									if (!visiblePc.isDead()) {
										_player.sendPackets(new S_ServerMessage(592));
										return;
									}
								}
							}
							if (pc.getCurrentHp() == 0 && pc.isDead()) {
								if (pc.getMap().isUseResurrection()) {
									if (_skillId == RESURRECTION) {
										//返生術
										pc.setGres(false);
									} else if (_skillId == GREATER_RESURRECTION) {
										//終極返生術
										pc.setGres(true);
									}
									pc.setTempID(_player.getId());
									pc.sendPackets(new S_Message_YN(322, ""));
								}
							}
						}
					}
					if (cha instanceof L1NpcInstance) {
						if (!(cha instanceof L1TowerInstance)) {
							L1NpcInstance npc = (L1NpcInstance) cha;
							if (npc.getNpcTemplate().isCantResurrect()) {
								return;
							}
							if (npc instanceof L1PetInstance && L1World.getInstance().getVisiblePlayer(npc, 0).size() > 0) {
								for (L1PcInstance visiblePc : L1World.getInstance().getVisiblePlayer(npc,0)) {
									if (!visiblePc.isDead()) {
										_player.sendPackets(new S_ServerMessage(592));
										return;
									}
								}
							}
							if (npc.getCurrentHp() == 0 && npc.isDead()) {
								npc.resurrect(npc.getMaxHp() / 4);
								npc.setResurrect(true);
							}
						}
					}
				} else if (_skillId == CALL_OF_NATURE) { 
					//生命呼喚
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						if (_player.getId() != pc.getId()) {
							if (L1World.getInstance().getVisiblePlayer(pc,0).size() > 0) {
								for (L1PcInstance visiblePc : L1World.getInstance().getVisiblePlayer(pc,0)) {
									if (!visiblePc.isDead()) {
										_player.sendPackets(new S_ServerMessage(592));
										return;
									}
								}
							}
							if (pc.getCurrentHp() == 0 && pc.isDead()) {
								pc.setTempID(_player.getId());
								pc.sendPackets(new S_Message_YN(322, ""));
							}
						}
					}
					if (cha instanceof L1NpcInstance) {
						if (!(cha instanceof L1TowerInstance)) {
							L1NpcInstance npc = (L1NpcInstance) cha;
							if (npc.getNpcTemplate().isCantResurrect()) {
								return;
							}
							if (npc instanceof L1PetInstance&& L1World.getInstance().getVisiblePlayer(npc, 0).size() > 0) {
								for (L1PcInstance visiblePc : L1World.getInstance().getVisiblePlayer(npc,0)) {
									if (!visiblePc.isDead()) {
										_player.sendPackets(new S_ServerMessage(592));
										return;
									}
								}
							}
							if (npc.getCurrentHp() == 0 && npc.isDead()) {
								npc.resurrect(cha.getMaxHp());
								npc.resurrect(cha.getMaxMp() / 100);
								npc.setResurrect(true);
							}
						}
					}
				} else if (_skillId == DETECTION) { 
					//無所遁形術
					if (cha instanceof L1NpcInstance) {
						L1NpcInstance npc = (L1NpcInstance) cha;
						int hiddenStatus = npc.getHiddenStatus();
						if (hiddenStatus == L1NpcInstance.HIDDEN_STATUS_SINK) {
							npc.appearOnGround(_player);
						}
					}
				} else if (_skillId == COUNTER_DETECTION) { 
					//強力無所遁形術
					if (_user instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance)_user;
						dmg = _magic.calcMagicDamage(_skillId, pc);
						//add by sdcom 2009.11.26
					} else if (cha instanceof L1NpcInstance) {
						L1NpcInstance npc = (L1NpcInstance) cha;
						int hiddenStatus = npc.getHiddenStatus();
						if (hiddenStatus == L1NpcInstance.HIDDEN_STATUS_SINK) {
							npc.appearOnGround(_player);
						} else {
							dmg = 0;
						}
					} else {
						dmg = 0;
					}
				} else if (_skillId == TRUE_TARGET) {
					//精準目標
					if (_user instanceof L1PcInstance) {
						L1PcInstance pri = (L1PcInstance) _user;
						pri.sendPackets(new S_TrueTarget(_targetID,pri.getId(), _message));
						L1PcInstance players[] = L1World.getInstance().getClan(pri.getClanname()).getOnlineClanMember();
						for (L1PcInstance pc : players) {
							pc.sendPackets(new S_TrueTarget(_targetID, pc.getId(), _message));
						}
					}
				} else if (_skillId == ELEMENTAL_FALL_DOWN) {
					//弱化屬性
					if (_user instanceof L1PcInstance) {
						int playerAttr = _player.getElfAttr();
						int i = -50;
						if (cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;
							switch (playerAttr) {
							case 0:
								_player.sendPackets(new S_ServerMessage(79));
								break;
							case 1:
								pc.addEarth(i);
								pc.setAddAttrKind(1);
								break;
							case 2:
								pc.addFire(i);
								pc.setAddAttrKind(2);
								break;
							case 4:
								pc.addWater(i);
								pc.setAddAttrKind(4);
								break;
							case 8:
								pc.addWind(i);
								pc.setAddAttrKind(8);
								break;
							default:
								break;
							}
						} else if (cha instanceof L1MonsterInstance) {
							L1MonsterInstance mob = (L1MonsterInstance) cha;
							switch (playerAttr) {
							case 0:
								_player.sendPackets(new S_ServerMessage(79));
								break;
							case 1:
								mob.addEarth(i);
								mob.setAddAttrKind(1);
								break;
							case 2:
								mob.addFire(i);
								mob.setAddAttrKind(2);
								break;
							case 4:
								mob.addWater(i);
								mob.setAddAttrKind(4);
								break;
							case 8:
								mob.addWind(i);
								mob.setAddAttrKind(8);
								break;
							default:
								break;
							}
						}
					}
				} else if ((_skillId == HEAL || 
							_skillId == EXTRA_HEAL	|| 
							_skillId == GREATER_HEAL || 
							_skillId == HEAL_ALL || 
							_skillId == FULL_HEAL || 
							_skillId == NATURES_TOUCH || 
							_skillId == NATURES_BLESSING) && (_user instanceof L1PcInstance)) {
					//初級治癒術,中級治癒術,高級治癒術,體力回復術,全部治癒術,生命之泉,生命的祝福
					if (cha.hasSkillEffect(STATUS_CURSE_PARALYZED) //木乃尹狀態
						||cha.hasSkillEffect(SHOCK_STUN) //衝暈
						||cha.hasSkillEffect(FOG_OF_SLEEPING) //沉睡之霧
						||cha.hasSkillEffect(ICE_LANCE)){ //冰茅
						_player.sendPackets(new S_ServerMessage(285));
						//285 : \f1在此狀態下無法使用魔法。
						return;
					} else if (_user instanceof L1PcInstance) {
						cha.removeSkillEffect(WATER_LIFE);
					}
				} else if (_skillId == CHILL_TOUCH || _skillId == VAMPIRIC_TOUCH) {
					//寒冷戰慄,吸血鬼之吻
					heal = dmg;
				} else if (_skillId == TRIPLE_ARROW) { 
					//三重矢
					boolean gfxcheck = false;
					int[] BowGFX = { 138, 37, 3860, 3126, 3420, 2284, 3105,
							3145, 3148, 3151, 3871, 4125, 2323, 3892, 3895,
							3898, 3901, 4917, 4918, 4919, 4950, 6087, 6140,
							6145, 6150, 6155, 6160, 6269, 6272, 6275, 6278,
							6826, 6827, 6836, 6837, 6846, 6847, 6856, 6857,
							6866, 6867, 6876, 6877, 6886, 6887 };
					int playerGFX = _player.getTempCharGfx();
					for (int gfx : BowGFX) {
						if (playerGFX == gfx) {
							gfxcheck = true;
							break;
						}
					}
					if (!gfxcheck) {
						return;
					}
					for (int i = 3; i > 0; i--) {
						_target.onAction(_player);
					}
 					_player.sendPackets(new S_SkillSound(_player.getId(),4394));
					_player.broadcastPacket(new S_SkillSound(_player.getId(),4394));
				} else if (_skillId == FOE_SLAYER) { 
					//屠宰者
					for (int i = 3; i > 0; i--) {
						_target.onAction(_player);
					}
					_player.sendPackets(new S_SkillSound(_target.getId(),6509));
 					_player.sendPackets(new S_SkillSound(_player.getId(),7020));
					_player.broadcastPacket(new S_SkillSound(_target.getId(),6509));
					_player.broadcastPacket(new S_SkillSound(_player.getId(),7020));
				} else if (_skillId == 10026 || _skillId == 10027 || _skillId == 10028 || _skillId == 10029) {
					// 安息攻撃
					if (_user instanceof L1NpcInstance) {
						_user.broadcastPacket(new S_NpcChatPacket(_npc,	"$3717", 0));
					} else {
						_player.broadcastPacket(new S_ChatPacket(_player, "$3717", 0, 0));
					}
				} else if (_skillId == 10057) {
					L1Teleport.teleportToTargetFront(cha, _user, 1);
				} else if (_skillId == SLOW || _skillId == MASS_SLOW || _skillId == ENTANGLE) { 
					//緩速術,集體緩速術,地面障礙
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						if (pc.getHasteItemEquipped() > 0) {
							continue;
						}
					}
					if (cha.getMoveSpeed() == 0) {
						if (cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;
							pc.sendPackets(new S_SkillHaste(pc.getId(), 2, _getBuffIconDuration));
						}
						cha.broadcastPacket(new S_SkillHaste(cha.getId(), 2, _getBuffIconDuration));
						cha.setMoveSpeed(2);
					} else if (cha.getMoveSpeed() == 1) {
						int skillNum = 0;
						if (cha.hasSkillEffect(HASTE)) {
							skillNum = HASTE;
						} else if (cha.hasSkillEffect(GREATER_HASTE)) {
							skillNum = GREATER_HASTE;
						} else if (cha.hasSkillEffect(STATUS_HASTE)) {
							skillNum = STATUS_HASTE;
						}
						if (skillNum != 0) {
							cha.removeSkillEffect(skillNum);
							cha.removeSkillEffect(_skillId);
							cha.setMoveSpeed(0);
							continue;
						}
					}
				} else if (_skillId == CURSE_BLIND || _skillId == DARKNESS) {
					//闇盲咒術,黑闇之影
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						if (pc.hasSkillEffect(STATUS_FLOATING_EYE)) {
							pc.sendPackets(new S_CurseBlind(2));
						} else {
							pc.sendPackets(new S_CurseBlind(1));
						}
					}
				} else if (_skillId == CURSE_POISON) {
					//毒咒
					L1DamagePoison.doInfection(_user, cha, 3000, 5);
				} else if (_skillId == CURSE_PARALYZE || _skillId == 10101) {
					//木乃伊的詛咒,??
					if (!cha.hasSkillEffect(EARTH_BIND) && 
						!cha.hasSkillEffect(ICE_LANCE) && 
						!cha.hasSkillEffect(FREEZING_BLIZZARD) && 
						!cha.hasSkillEffect(FREEZING_BREATH)) {
						//大地屏障,冰矛圍籬,冰雪颶風,寒冰噴吐
						if (cha instanceof L1PcInstance) {
							L1CurseParalysis.curse(cha, 8000, 16000);
						} else if (cha instanceof L1MonsterInstance) {
							L1CurseParalysis.curse(cha, 0, 16000);
						}
					}
				} else if (_skillId == WEAKNESS) { 
					//弱化術
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDmgup(-5);
						pc.addHitup(-1);
					}
				} else if (_skillId == DISEASE) {
					//疾病術
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDmgup(-6);
						pc.addAc(12);
					}
				} else if (_skillId == ICE_LANCE || _skillId == FREEZING_BLIZZARD || _skillId == FREEZING_BREATH) {
					//冰矛圍籬,冰雪颶風,寒冰噴吐
					_isFreeze = _magic.calcProbabilityMagic(_skillId);
					if (_isFreeze) {
						int time = _skill.getBuffDuration() * 1000;
						L1EffectSpawn.getInstance().spawnEffect(81168, time,cha.getX(), cha.getY(), cha.getMapId());
						if (cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;
							pc.sendPackets(new S_Poison(pc.getId(), 2));
							pc.broadcastPacket(new S_Poison(pc.getId(), 2));
							pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_FREEZE, true));
						} else if (cha instanceof L1MonsterInstance || cha instanceof L1SummonInstance || cha instanceof L1PetInstance) {
							L1NpcInstance npc = (L1NpcInstance) cha;
							npc.broadcastPacket(new S_Poison(npc.getId(), 2));
							npc.setParalyzed(true);
							npc.setParalysisTime(time);
						}
					}
				} else if (_skillId == EARTH_BIND) { 
					//大地屏障
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_Poison(pc.getId(), 2));
						pc.broadcastPacket(new S_Poison(pc.getId(), 2));
						pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_FREEZE, true));
					} else if (cha instanceof L1MonsterInstance || cha instanceof L1SummonInstance || cha instanceof L1PetInstance) {
						L1NpcInstance npc = (L1NpcInstance) cha;
						npc.broadcastPacket(new S_Poison(npc.getId(), 2));
						npc.setParalyzed(true);
						npc.setParalysisTime(_skill.getBuffDuration() * 1000);
					}
				} else if (_skillId == SHOCK_STUN) {
					//衝擊之暈
					int[] stunTimeArray = { 1000, 2000, 3000, 4000, 5000, 6000 };
					Random random = new Random();
					int rnd = random.nextInt(stunTimeArray.length);
					_shockStunDuration = stunTimeArray[rnd];
					if (cha instanceof L1PcInstance && cha.hasSkillEffect(SHOCK_STUN)) {
						_shockStunDuration += cha.getSkillEffectTimeSec(SHOCK_STUN) * 1000;
					}

					L1EffectSpawn.getInstance().spawnEffect(81162, _shockStunDuration,cha.getX(), cha.getY(), cha.getMapId());
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_STUN,true));
					} else if (cha instanceof L1MonsterInstance || cha instanceof L1SummonInstance || cha instanceof L1PetInstance) {
						L1NpcInstance npc = (L1NpcInstance) cha;
						npc.setParalyzed(true);
						npc.setParalysisTime(_shockStunDuration);
					}
				} else if (_skillId == WIND_SHACKLE) { 
					//風之枷鎖
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_SkillIconWindShackle(pc.getId(),_getBuffIconDuration));
					}
				} else if (_skillId == CANCELLATION) {
					//魔法相消術
					if (cha instanceof L1NpcInstance) {
						L1NpcInstance npc = (L1NpcInstance) cha;
						int npcId = npc.getNpcTemplate().get_npcId();
						if (npcId == 71092) {
							// 調査員
							if (npc.getGfxId() == npc.getTempCharGfx()) {
								npc.setTempCharGfx(1314);
								npc.broadcastPacket(new S_ChangeShape(npc.getId(), 1314));
								return;
							} else {
								return;
							}
						}
						if (npcId == 45640) { 
							//獨角獸
							if (npc.getGfxId() == npc.getTempCharGfx()) {
								npc.setCurrentHp(npc.getMaxHp());
								npc.setTempCharGfx(2332);
								npc.broadcastPacket(new S_ChangeShape(npc.getId(), 2332));
								npc.setName("$2103");
								npc.setNameId("$2103");
								npc.broadcastPacket(new S_ChangeName(npc.getId(), "$2103"));
							} else if (npc.getTempCharGfx() == 2332) {
								npc.setCurrentHp(npc.getMaxHp());
								npc.setTempCharGfx(2755);
								npc.broadcastPacket(new S_ChangeShape(npc.getId(), 2755));
								npc.setName("$2488");
								npc.setNameId("$2488");
								npc.broadcastPacket(new S_ChangeName(npc.getId(), "$2488"));
							}
						}
						if (npcId == 81209) { 
							//羅伊
							if (npc.getGfxId() == npc.getTempCharGfx()) {
								npc.setTempCharGfx(4310);
								npc.broadcastPacket(new S_ChangeShape(npc.getId(), 4310));
								return;
							} else {
								return;
							}
						}
					}
					if (_player != null && _player.isInvisble()) {
						_player.delInvis();
					}
					if (!(cha instanceof L1PcInstance)) {
						L1NpcInstance npc = (L1NpcInstance) cha;
						npc.setMoveSpeed(0);
						npc.setBraveSpeed(0);
						npc.broadcastPacket(new S_SkillHaste(cha.getId(), 0,0));
						npc.broadcastPacket(new S_SkillBrave(cha.getId(), 0,0));
						npc.setWeaponBreaked(false);
						npc.setParalyzed(false);
						npc.setParalysisTime(0);
					}
					for (int skillNum = SKILLS_BEGIN; skillNum <= SKILLS_END; skillNum++) {
						if (isNotCancelable(skillNum) && !cha.isDead()) {
							continue;
						}
						cha.removeSkillEffect(skillNum);
					}
					cha.curePoison();
					cha.cureParalaysis();
					for (int skillNum = STATUS_BEGIN; skillNum <= STATUS_END; skillNum++) {
						if (skillNum == STATUS_CHAT_PROHIBITED || skillNum == STATUS_CURSE_BARLOG || skillNum == STATUS_CURSE_YAHEE) { 
							continue;
						}
						cha.removeSkillEffect(skillNum);
					}
					for (int skillNum = COOKING_BEGIN; skillNum <= COOKING_END; skillNum++) {
						if (isNotCancelable(skillNum)) {
							continue;
						}
						cha.removeSkillEffect(skillNum);
					}
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						if (pc.getHasteItemEquipped() > 0) {
							pc.setMoveSpeed(0);
							pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
							pc.broadcastPacket(new S_SkillHaste(pc.getId(), 0,0));
						}
					}
					cha.removeSkillEffect(STATUS_FREEZE); 
					// Freeze解除
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_CharVisualUpdate(pc));
						pc.broadcastPacket(new S_CharVisualUpdate(pc));
						if (pc.isPrivateShop()) {
							pc.sendPackets(new S_DoActionShop(pc.getId(),ActionCodes.ACTION_Shop, pc.getShopChat()));
							pc.broadcastPacket(new S_DoActionShop(pc.getId(),ActionCodes.ACTION_Shop, pc.getShopChat()));
						}
						if (_user instanceof L1PcInstance) {
							L1PinkName.onAction(pc, _user);
						}
					}
				} else if (_skillId == TURN_UNDEAD && (undeadType == 1 || undeadType == 3)) {
					//起死回生術
					dmg = cha.getCurrentHp();
				} else if (_skillId == MANA_DRAIN) {
					//魔力奪取
					Random random = new Random();
					int chance = random.nextInt(10) + 5;
					drainMana = chance + (_user.getInt() / 2);
					if (cha.getCurrentMp() < drainMana) {
						drainMana = cha.getCurrentMp();
					}
				} else if (_skillId == WEAPON_BREAK) { 
					//壞物術
					if (_calcType == PC_PC || _calcType == NPC_PC) {
						if (cha instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) cha;
							L1ItemInstance weapon = pc.getWeapon();
							if (weapon != null) {
								Random random = new Random();
								int weaponDamage = random.nextInt(_user.getInt() / 3) + 1;
								pc.sendPackets(new S_ServerMessage(268, weapon.getLogName()));
								pc.getInventory().receiveDamage(weapon,weaponDamage);
							}
						}
					} else {
						((L1NpcInstance) cha).setWeaponBreaked(true);
					}
				} else if (_skillId == FOG_OF_SLEEPING) {
					//沉睡之霧
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_SLEEP,true));
					}
					cha.setSleeped(true);
				} else if (_skillId == STATUS_FREEZE) { 
					//??
					if (cha instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_BIND,true));
					}
				}
				//TODO pc to pc , npc to pc
				if (_calcType == PC_PC || _calcType == NPC_PC) {
					if (_skillId == TELEPORT || _skillId == MASS_TELEPORT) { 
						//指定傳送,集體傳送術
						L1PcInstance pc = (L1PcInstance) cha;
						L1BookMark bookm = pc.getBookMark(_bookmarkId);
						if (bookm != null) { 
							if (pc.getMap().isEscapable() || pc.isGm()) {
								int newX = bookm.getLocX();
								int newY = bookm.getLocY();
								short mapId = bookm.getMapId();
								if (_skillId == MASS_TELEPORT) { 
									List<L1PcInstance> clanMember = L1World.getInstance().getVisiblePlayer(pc);
									for (L1PcInstance member : clanMember) {
										if (pc.getLocation().getTileLineDistance(member.getLocation()) <= 3	&& member.getClanid() == pc.getClanid()	&& pc.getClanid() != 0 && member.getId() != pc.getId()) {
											L1Teleport.teleport(member, newX, newY, mapId, 5, true);
										}
									}
								}
								L1Teleport.teleport(pc, newX, newY, mapId, 5, true);
							} else { 
								L1Teleport.teleport(pc, pc.getX(), pc.getY(), pc.getMapId(), pc.getHeading(), false);
								pc.sendPackets(new S_ServerMessage(79));
							}
						} else { 
							if (pc.getMap().isTeleportable() || pc.isGm()) {
								L1Location newLocation = pc.getLocation().randomLocation(200, true);
								int newX = newLocation.getX();
								int newY = newLocation.getY();
								short mapId = (short) newLocation.getMapId();

								if (_skillId == MASS_TELEPORT) { 
									//集體傳送術
									List<L1PcInstance> clanMember = L1World.getInstance().getVisiblePlayer(pc);
									for (L1PcInstance member : clanMember) {
										if (pc.getLocation().getTileLineDistance(member.getLocation()) <= 3 && member.getClanid() == pc.getClanid() && pc.getClanid() != 0 && member.getId() != pc.getId()) {
											L1Teleport.teleport(member, newX, newY, mapId, 5, true);
										}
									}
								}
								L1Teleport.teleport(pc, newX, newY, mapId, 5, true);
							} else {
								pc.sendPackets(new S_ServerMessage(276));
								L1Teleport.teleport(pc, pc.getX(), pc.getY(), pc.getMapId(), pc.getHeading(), false);
							}
						}
					} else if (_skillId == TELEPORT_TO_MATHER) {
						//世界樹的呼喚
						L1PcInstance pc = (L1PcInstance) cha;
						if (pc.getMap().isEscapable() || pc.isGm()) {
							L1Teleport.teleport(pc, 33051, 32337, (short) 4, 5, true);
						} else {
							pc.sendPackets(new S_ServerMessage(647));
							L1Teleport.teleport(pc, pc.getX(), pc.getY(), pc.getMapId(), pc.getHeading(), false);
						}
					} else if (_skillId == CALL_CLAN) {
						//呼喚盟友
						L1PcInstance pc = (L1PcInstance) cha;
						L1PcInstance clanPc = (L1PcInstance) L1World.getInstance().findObject(_targetID);
						if (clanPc != null) {
							clanPc.setTempID(pc.getId()); 
							clanPc.sendPackets(new S_Message_YN(729, "")); 
						}
					} else if (_skillId == RUN_CLAN) { 
						//援護盟友
						L1PcInstance pc = (L1PcInstance) cha;
						L1PcInstance clanPc = (L1PcInstance) L1World.getInstance().findObject(_targetID);
						if (clanPc != null) {
							if (pc.getMap().isEscapable() || pc.isGm()) {
								boolean castle_area = L1CastleLocation.checkInAllWarArea(clanPc.getX(), clanPc.getY(),clanPc.getMapId());
								if ((clanPc.getMapId() == 0 || clanPc.getMapId() == 4 || clanPc.getMapId() == 304) && castle_area == false) {
									L1Teleport.teleport(pc, clanPc.getX(), clanPc.getY(), clanPc.getMapId(), 5, true);
								} else {
									pc.sendPackets(new S_ServerMessage(547));
								}
							} else {
								pc.sendPackets(new S_ServerMessage(647));
								L1Teleport.teleport(pc, pc.getX(), pc.getY(),pc.getMapId(), pc.getHeading(), false);
							}
						}
					} else if (_skillId == CREATE_MAGICAL_WEAPON) { 
						//創造魔法武器
						L1PcInstance pc = (L1PcInstance) cha;
						L1ItemInstance item = pc.getInventory().getItem(_itemobjid);
						if (item != null && item.getItem().getType2() == 1) {
							int item_type = item.getItem().getType2();
							int safe_enchant = item.getItem().get_safeenchant();
							int enchant_level = item.getEnchantLevel();
							String item_name = item.getName();
							if (safe_enchant < 0) { 
								pc.sendPackets(new S_ServerMessage(79));
							} else if (safe_enchant == 0) {
								pc.sendPackets(new S_ServerMessage(79));
							} else if (item_type == 1 && enchant_level == 0) {
								if (!item.isIdentified()) {
									pc.sendPackets(new S_ServerMessage(161, item_name,"$245", "$247"));
								} else {
									item_name = "+0 " + item_name;
									pc.sendPackets(new S_ServerMessage(161, "+0 " + item_name, "$245", "$247"));
								}
								item.setEnchantLevel(1);
								pc.getInventory().updateItem(item, L1PcInventory.COL_ENCHANTLVL);
							} else {
								pc.sendPackets(new S_ServerMessage(79));
							}
						} else {
							pc.sendPackets(new S_ServerMessage(79));
						}
					} else if (_skillId == BRING_STONE) { 
						//提煉魔石
						L1PcInstance pc = (L1PcInstance) cha;
						Random random = new Random();
						L1ItemInstance item = pc.getInventory().getItem(_itemobjid);
						if (item != null) {
							int dark = (int) (10 + (pc.getLevel() * 0.8) + (pc.getWis() - 6) * 1.2);
							int brave = (int) (dark / 2.1);
							int wise = (int) (brave / 2.0);
							int kayser = (int) (wise / 1.9);
							int chance = random.nextInt(100) + 1;
							if (item.getItem().getItemId() == 40320) {
								pc.getInventory().removeItem(item, 1);
								if (dark >= chance) {
									pc.getInventory().storeItem(40321, 1);
									pc.sendPackets(new S_ServerMessage(403, "$2475")); 
								} else {
									pc.sendPackets(new S_ServerMessage(280)); 
								}
							} else if (item.getItem().getItemId() == 40321) {
								pc.getInventory().removeItem(item, 1);
								if (brave >= chance) {
									pc.getInventory().storeItem(40322, 1);
									pc.sendPackets(new S_ServerMessage(403,"$2476")); 
								} else {
									pc.sendPackets(new S_ServerMessage(280)); 
								}
							} else if (item.getItem().getItemId() == 40322) {
								pc.getInventory().removeItem(item, 1);
								if (wise >= chance) {
									pc.getInventory().storeItem(40323, 1);
									pc.sendPackets(new S_ServerMessage(403,"$2477")); 
								} else {
									pc.sendPackets(new S_ServerMessage(280)); 
								}
							} else if (item.getItem().getItemId() == 40323) {
								pc.getInventory().removeItem(item, 1);
								if (kayser >= chance) {
									pc.getInventory().storeItem(40324, 1);
									pc.sendPackets(new S_ServerMessage(403, "$2478"));
								} else {
									pc.sendPackets(new S_ServerMessage(280)); 
								}
							}
						}
					} else if (_skillId == SUMMON_MONSTER) { 
						//召喚術
						L1PcInstance pc = (L1PcInstance) cha;
						int level = pc.getLevel();
						int[] summons;
						if (pc.getMap().isRecallPets() || pc.isGm()) {
							if (pc.getInventory().checkEquipped(20284)) {
								pc.sendPackets(new S_ShowSummonList(pc.getId()));
								if (!pc.isSummonMonster()) { 
									// 加入判斷是否施法(召戒清單、變身清單)
									pc.setSummonMonster(true);
								}  
							} else {
								summons = new int[] { 81210, 81213, 81216, 81219, 81222, 81225, 81228 };
								int summonid = 0;
								int summoncost = 6;
								int levelRange = 32;
								for (int i = 0; i < summons.length; i++) {
									if (level < levelRange || i == summons.length - 1) {
										summonid = summons[i];
										break;
									}
									levelRange += 4;
								}
								int petcost = 0;
								Object[] petlist = pc.getPetList().values().toArray();
								for (Object pet : petlist) {
									petcost += ((L1NpcInstance) pet).getPetcost();
								}
								int charisma = pc.getCha() + 6 - petcost;
								int summoncount = charisma / summoncost;
								L1Npc npcTemp = NpcTable.getInstance().getTemplate(summonid);
								for (int i = 0; i < summoncount; i++) {
									L1SummonInstance summon = new L1SummonInstance(npcTemp, pc);
									summon.setPetcost(summoncost);
								}
							}
						} else {
							pc.sendPackets(new S_ServerMessage(79));
						}
					} else if (_skillId == LESSER_ELEMENTAL || _skillId == GREATER_ELEMENTAL) { 
						//召喚屬性精靈,召喚強力屬性精靈
						L1PcInstance pc = (L1PcInstance) cha;
						int attr = pc.getElfAttr();
						if (attr != 0) { 
							if (pc.getMap().isRecallPets() || pc.isGm()) {
								int petcost = 0;
								Object[] petlist = pc.getPetList().values().toArray();
								for (Object pet : petlist) {
									petcost += ((L1NpcInstance) pet).getPetcost();
								}
								if (petcost == 0) {
									int summonid = 0;
									int summons[];
									if (_skillId == LESSER_ELEMENTAL) {
										//召喚屬性精靈
										summons = new int[] { 45306, 45303, 45304, 45305 };
									} else {
										summons = new int[] { 81053, 81050, 81051, 81052 };
									}
									int npcattr = 1;
									for (int i = 0; i < summons.length; i++) {
										if (npcattr == attr) {
											summonid = summons[i];
											i = summons.length;
										}
										npcattr *= 2;
									}
									if (summonid == 0) {
										Random random = new Random();
										int k3 = random.nextInt(4);
										summonid = summons[k3];
									}

									L1Npc npcTemp = NpcTable.getInstance().getTemplate(summonid);
									L1SummonInstance summon = new L1SummonInstance(npcTemp, pc);
									summon.setPetcost(pc.getCha() + 7);
								}
							} else {
								pc.sendPackets(new S_ServerMessage(79));
							}
						}
					} else if (_skillId == ABSOLUTE_BARRIER) { 
						//絕對屏障
						L1PcInstance pc = (L1PcInstance) cha;
						pc.stopHpRegeneration();
						pc.stopMpRegeneration();
						pc.stopMpRegenerationByDoll();
					} else if (_skillId == LIGHT) {
						//日光術
						// addMagicList()後に、turnOnOffLight()でパケット送信
					} else if (_skillId == GLOWING_AURA) { 
						//激勵士氣
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addHitup(5);
						pc.addBowHitup(5);
						pc.addMr(20);
						pc.sendPackets(new S_SPMR(pc));
						pc.sendPackets(new S_SkillIconAura(113,	_getBuffIconDuration));
					} else if (_skillId == SHINING_AURA) { 
						//鋼鐵士氣
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addAc(-8);
						pc.sendPackets(new S_SkillIconAura(114, _getBuffIconDuration));
					} else if (_skillId == BRAVE_AURA) { 
						//衝擊士氣
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDmgup(5);
						pc.sendPackets(new S_SkillIconAura(116, _getBuffIconDuration));
					} else if (_skillId == SHIELD) { 
						//保護罩
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addAc(-2);
						pc.sendPackets(new S_SkillIconShield(5, _getBuffIconDuration));
					} else if (_skillId == SHADOW_ARMOR) { 
						//影之防護
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addAc(-3);
						pc.sendPackets(new S_SkillIconShield(3, _getBuffIconDuration));
					} else if (_skillId == DRESS_DEXTERITY) { 
						//敏捷提升
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDex((byte) 2);
						pc.sendPackets(new S_Dexup(pc, 2, _getBuffIconDuration));
					} else if (_skillId == DRESS_MIGHTY) { 
						//力量提升
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addStr((byte) 2);
						pc.sendPackets(new S_Strup(pc, 2, _getBuffIconDuration));
					} else if (_skillId == SHADOW_FANG) { 
						//暗影之牙
						L1PcInstance pc = (L1PcInstance) cha;
						L1ItemInstance item = pc.getInventory().getItem(_itemobjid);
						if (item != null && item.getItem().getType2() == 1) {
							item.setSkillWeaponEnchant(pc, _skillId, _skill.getBuffDuration() * 1000);
						} else {
							pc.sendPackets(new S_ServerMessage(79));
						}
					} else if (_skillId == ENCHANT_WEAPON) { 
						//擬似魔法武器
						L1PcInstance pc = (L1PcInstance) cha;
						L1ItemInstance item = pc.getInventory().getItem(_itemobjid);
						if (item != null && item.getItem().getType2() == 1) {
							pc.sendPackets(new S_ServerMessage(161, item.getLogName(), "$245", "$247"));
							item.setSkillWeaponEnchant(pc, _skillId, _skill.getBuffDuration() * 1000);
						} else {
							pc.sendPackets(new S_ServerMessage(79));
						}
					} else if (_skillId == HOLY_WEAPON || _skillId == BLESS_WEAPON) { 
						//神聖武器,祝福魔法武器
						if (!(cha instanceof L1PcInstance)) {
							return;
						}
						L1PcInstance pc = (L1PcInstance) cha;
						if (pc.getWeapon() == null) {
							pc.sendPackets(new S_ServerMessage(79));
							return;
						}
						for (L1ItemInstance item : pc.getInventory().getItems()) {
							if (pc.getWeapon().equals(item)) {
								pc.sendPackets(new S_ServerMessage(161, item.getLogName(), "$245", "$247"));
								item.setSkillWeaponEnchant(pc, _skillId, _skill.getBuffDuration() * 1000);
								return;
							}
						}
					} else if (_skillId == BLESSED_ARMOR) { 
						//鎧甲護持
						L1PcInstance pc = (L1PcInstance) cha;
						L1ItemInstance item = pc.getInventory().getItem(_itemobjid);
						if (item != null && item.getItem().getType2() == 2 && item.getItem().getType() == 2) {
							pc.sendPackets(new S_ServerMessage(161, item.getLogName(), "$245", "$247"));
							item.setSkillArmorEnchant(pc, _skillId, _skill.getBuffDuration() * 1000);
						} else {
							pc.sendPackets(new S_ServerMessage(79));
						}
					} else if (_skillId == EARTH_BLESS) { 
						//大地的祝福
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addAc(-7);
						pc.sendPackets(new S_SkillIconShield(7, _getBuffIconDuration));
					} else if (_skillId == RESIST_MAGIC) { 
						//魔法防禦
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addMr(10);
						pc.sendPackets(new S_SPMR(pc));
					} else if (_skillId == CLEAR_MIND) { 
						//淨化精神
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addWis((byte) 3);
						pc.resetBaseMr();
					} else if (_skillId == RESIST_ELEMENTAL) { 
						//屬性防禦
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addWind(10);
						pc.addWater(10);
						pc.addFire(10);
						pc.addEarth(10);
						pc.sendPackets(new S_OwnCharAttrDef(pc));
					} else if (_skillId == BODY_TO_MIND) { 
						//心靈轉換
						L1PcInstance pc = (L1PcInstance) cha;
						pc.setCurrentMp(pc.getCurrentMp() + 2);
					} else if (_skillId == BLOODY_SOUL) { 
						//魂體轉換
						L1PcInstance pc = (L1PcInstance) cha;
						pc.setCurrentMp(pc.getCurrentMp() + 12);
					} else if (_skillId == ELEMENTAL_PROTECTION) { 
						//單屬性防禦
						L1PcInstance pc = (L1PcInstance) cha;
						int attr = pc.getElfAttr();
						if (attr == 1) {
							pc.addEarth(50);
						} else if (attr == 2) {
							pc.addFire(50);
						} else if (attr == 4) {
							pc.addWater(50);
						} else if (attr == 8) {
							pc.addWind(50);
						}
					} else if (_skillId == INVISIBILITY || _skillId == BLIND_HIDING) { 
						//隱身術,暗隱術
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_Invis(pc.getId(), 1));
						pc.broadcastPacket(new S_RemoveObject(pc));
					} else if (_skillId == IRON_SKIN) { 
						//鋼鐵防護
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addAc(-10);
						pc.sendPackets(new S_SkillIconShield(10, _getBuffIconDuration));
					} else if (_skillId == EARTH_SKIN) { 
						//大地防護
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addAc(-6);
						pc.sendPackets(new S_SkillIconShield(6, _getBuffIconDuration));
					} else if (_skillId == PHYSICAL_ENCHANT_STR) { 
						//體魄強健術
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addStr((byte) 5);
						pc.sendPackets(new S_Strup(pc, 5, _getBuffIconDuration));
					} else if (_skillId == PHYSICAL_ENCHANT_DEX) { 
						//通暢氣脈術
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDex((byte) 5);
						pc.sendPackets(new S_Dexup(pc, 5, _getBuffIconDuration));
					} else if (_skillId == FIRE_WEAPON) { 
						//火焰武器
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDmgup(4);
						pc.sendPackets(new S_SkillIconAura(147, _getBuffIconDuration));
					} else if (_skillId == FIRE_BLESS) { 
						//烈炎氣息
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDmgup(4);
						pc.sendPackets(new S_SkillIconAura(154, _getBuffIconDuration));
					} else if (_skillId == BURNING_WEAPON) { 
						//烈炎武器
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDmgup(6);
						pc.addHitup(3);
						pc.sendPackets(new S_SkillIconAura(162, _getBuffIconDuration));
					} else if (_skillId == WIND_SHOT) { 
						//風之神射
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addBowHitup(6);
						pc.sendPackets(new S_SkillIconAura(148, _getBuffIconDuration));
					} else if (_skillId == STORM_EYE) { 
						//暴風之眼
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addBowHitup(2);
						pc.addBowDmgup(3);
						pc.sendPackets(new S_SkillIconAura(155, _getBuffIconDuration));
					} else if (_skillId == STORM_SHOT) { 
						//暴風神射
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addBowDmgup(5);
						pc.addBowHitup(-1);
						pc.sendPackets(new S_SkillIconAura(165, _getBuffIconDuration));
					} else if (_skillId == BERSERKERS) { 
						//狂暴術
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addAc(10);
						pc.addDmgup(5);
						pc.addHitup(2);
					} else if (_skillId == SHAPE_CHANGE) { 
						//變形術
						L1PcInstance pc = (L1PcInstance) cha;
						pc.sendPackets(new S_ShowPolyList(pc.getId()));
						if (!pc.isShapeChange()) { 
							//加入判斷是否施法(召戒清單、變身清單)
							pc.setShapeChange(true);
						}
					} else if (_skillId == ADVANCE_SPIRIT) { 
						//靈魂昇華
						L1PcInstance pc = (L1PcInstance) cha;
						pc.setAdvenHp(pc.getBaseMaxHp() / 5);
						pc.setAdvenMp(pc.getBaseMaxMp() / 5);
						pc.addMaxHp(pc.getAdvenHp());
						pc.addMaxMp(pc.getAdvenMp());
						pc.sendPackets(new S_HPUpdate(pc.getCurrentHp(), pc.getMaxHp()));
						if (pc.isInParty()) {
							pc.getParty().updateMiniHP(pc);
						}
						pc.sendPackets(new S_MPUpdate(pc.getCurrentMp(), pc.getMaxMp()));
					} else if (_skillId == GREATER_HASTE) { 
						//強力加速術
						L1PcInstance pc = (L1PcInstance) cha;
						if (pc.getHasteItemEquipped() > 0) {
							continue;
						}
						if (pc.getMoveSpeed() != 2) { 
							pc.setDrink(false);
							pc.setMoveSpeed(1);
							pc.sendPackets(new S_SkillHaste(pc.getId(), 1, _getBuffIconDuration));
							pc.broadcastPacket(new S_SkillHaste(pc.getId(), 1, 0));
						} else { 
							int skillNum = 0;
							if (pc.hasSkillEffect(SLOW)) {
								skillNum = SLOW;
							} else if (pc.hasSkillEffect(MASS_SLOW)) {
								skillNum = MASS_SLOW;
							} else if (pc.hasSkillEffect(ENTANGLE)) {
								skillNum = ENTANGLE;
							}
							if (skillNum != 0) {
								pc.removeSkillEffect(skillNum);
								pc.removeSkillEffect(GREATER_HASTE);
								pc.setMoveSpeed(0);
								continue;
							}
						}
					} else if (_skillId == HOLY_WALK || _skillId == MOVING_ACCELERATION || _skillId == WIND_WALK) { 
						//神聖疾走,行走加速,風之疾走
						L1PcInstance pc = (L1PcInstance) cha;
						pc.setBraveSpeed(4);
						pc.sendPackets(new S_SkillBrave(pc.getId(), 4, _getBuffIconDuration));
						pc.broadcastPacket(new S_SkillBrave(pc.getId(), 4, 0));
					} else if (_skillId == AWAKEN_ANTHARAS) { 
						//覺醒安塔瑞斯
						L1PcInstance pc = (L1PcInstance) cha;
						L1Awake.start(pc, _skillId);
					} else if (_skillId == AWAKEN_FAFURION) { 
						//覺醒法利昂
						L1PcInstance pc = (L1PcInstance) cha;
						L1Awake.start(pc, _skillId);
					} else if (_skillId == AWAKEN_VALAKAS) { 
						//覺醒巴拉卡斯
						L1PcInstance pc = (L1PcInstance) cha;
						L1Awake.start(pc, _skillId);
					} else if (_skillId == ILLUSION_OGRE) { 
						//幻覺歐吉
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDmgup(4);
						pc.addHitup(4);
					} else if (_skillId == ILLUSION_LICH) { 
						//幻覺巫妖
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addSp(2);
						pc.sendPackets(new S_SPMR(pc));
					} else if (_skillId == ILLUSION_DIA_GOLEM) { 
						//幻覺鑽石高崙
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addAc(-20);
					} else if (_skillId == ILLUSION_AVATAR) { 
						//幻覺化身
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addDmgup(10);
					} else if (_skillId == INSIGHT) { 
						//洞察
						L1PcInstance pc = (L1PcInstance) cha;
						pc.addStr((byte) 1);
						pc.addCon((byte) 1);
						pc.addDex((byte) 1);
						pc.addWis((byte) 1);
						pc.addInt((byte) 1);
					} else if(_skillId == BLOODLUST){
						//血之渴望
						//add by sdcom 2009.12.4
						L1PcInstance pc = (L1PcInstance) cha;
						useDragonBraveSkill(pc,60);
					}
				}
				//TODO pc to npc , npc to npc
				if (_calcType == PC_NPC || _calcType == NPC_NPC) {
					if (_skillId == 36 && ((L1MonsterInstance) cha).getNpcTemplate().isTamable()) { 
						//迷魅術
						int petcost = 0;
						Object[] petlist = _user.getPetList().values().toArray();
						for (Object pet : petlist) {
							petcost += ((L1NpcInstance) pet).getPetcost();
						}
						int charisma = _user.getCha();
						if (_player.isElf()) {
							charisma += 12;
						} else if (_player.isWizard()) {
							charisma += 6;
						}
						charisma -= petcost;
						if (charisma >= 6) {
							L1SummonInstance summon = new L1SummonInstance(_targetNpc, _user, false);
							_target = summon; 
						} else {
							_player.sendPackets(new S_ServerMessage(319));
						}
					} else if (_skillId == 41) { 
						//造屍術
						int petcost = 0;
						Object[] petlist = _user.getPetList().values().toArray();
						for (Object pet : petlist) {
							petcost += ((L1NpcInstance) pet).getPetcost();
						}
						int charisma = _user.getCha();
						if (_player.isElf()) {
							charisma += 12;
						} else if (_player.isWizard()) { // ウィザード
							charisma += 6;
						}
						charisma -= petcost;
						if (charisma >= 6) {
							L1SummonInstance summon = new L1SummonInstance(_targetNpc, _user, true);
							_target = summon; 
						} else {
							_player.sendPackets(new S_ServerMessage(319));
						}
					} else if (_skillId == 23) { 
						//能量感測
						if (cha instanceof L1MonsterInstance) {
							L1Npc npcTemp = ((L1MonsterInstance) cha).getNpcTemplate();
							int weakAttr = npcTemp.get_weakAttr();
							if ((weakAttr & 1) == 1) { // 地
								cha.broadcastPacket(new S_SkillSound(cha.getId(), 2169));
							}
							if ((weakAttr & 2) == 2) { // 火
								cha.broadcastPacket(new S_SkillSound(cha.getId(), 2167));
							}
							if ((weakAttr & 4) == 4) { // 水
								cha.broadcastPacket(new S_SkillSound(cha.getId(), 2166));
							}
							if ((weakAttr & 8) == 8) { // 風
								cha.broadcastPacket(new S_SkillSound(cha.getId(), 2168));
							}
						}
					} else if (_skillId == 145) { 
						//釋放元素
						if (Config.RETURN_TO_NATURE && cha instanceof L1SummonInstance) {
							L1SummonInstance summon = (L1SummonInstance) cha;
							summon.broadcastPacket(new S_SkillSound(summon.getId(), 2245));
							summon.returnToNature();
						} else {
							if (_user instanceof L1PcInstance) {
								_player.sendPackets(new S_ServerMessage(79));
							}
						}
					}
				}
				if (_skill.getType() == L1Skills.TYPE_HEAL && _calcType == PC_NPC && undeadType == 1) {
					dmg *= -1;
				}
				if (_skill.getType() == L1Skills.TYPE_HEAL && _calcType == PC_NPC && undeadType == 3) {
					dmg = 0; 
				}
				if ((cha instanceof L1TowerInstance || cha instanceof L1DoorInstance) && dmg < 0) {
					dmg = 0;
				}
				
				if (dmg != 0 || drainMana != 0) {
					_magic.commit(dmg, drainMana); 
				}
				if (heal > 0) {
					if ((heal + _user.getCurrentHp()) > _user.getMaxHp()) {
						_user.setCurrentHp(_user.getMaxHp());
					} else {
						_user.setCurrentHp(heal + _user.getCurrentHp());
					}
				}
				if (cha instanceof L1PcInstance) {
					L1PcInstance pc = (L1PcInstance) cha;
					pc.turnOnOffLight();
					pc.sendPackets(new S_OwnCharAttrDef(pc));
					pc.sendPackets(new S_OwnCharStatus(pc));
					sendHappenMessage(pc); 
				}
				addMagicList(cha, false); 
				if (cha instanceof L1PcInstance) { 
					L1PcInstance pc = (L1PcInstance) cha;
					pc.turnOnOffLight();
				}
			}
			if (_skillId == 13 || _skillId == 72) { 
				//無所遁形術,強力無所遁形術
				detection(_player);
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	/**
	 * キャンセレーションで解除できないスキルかを返す。
	 */
	private boolean isNotCancelable(int skillNum) {
		return skillNum == ENCHANT_WEAPON || skillNum == BLESSED_ARMOR
				|| skillNum == ABSOLUTE_BARRIER || skillNum == ADVANCE_SPIRIT
				|| skillNum == SHOCK_STUN || skillNum == SHADOW_FANG
				|| skillNum == REDUCTION_ARMOR || skillNum == SOLID_CARRIAGE
				|| skillNum == COUNTER_BARRIER || skillNum == AWAKEN_ANTHARAS
				|| skillNum == AWAKEN_FAFURION || skillNum == AWAKEN_VALAKAS;
	}

	private void detection(L1PcInstance pc) {
		if (!pc.isGmInvis() && pc.isInvisble()) { // 自分
			pc.delInvis();
			pc.beginInvisTimer();
		}

		for (L1PcInstance tgt : L1World.getInstance().getVisiblePlayer(pc)) {
			if (!tgt.isGmInvis() && tgt.isInvisble()) {
				tgt.delInvis();
			}
		}
		L1WorldTraps.getInstance().onDetection(pc);
	}

	// ターゲットについて計算する必要があるか返す
	private boolean isTargetCalc(L1Character cha) {
		// 攻撃魔法のNon－PvP判定
		if (_skill.getTarget().equals("attack") && _skillId != 18) { // 攻撃魔法
			if (isPcSummonPet(cha)) { // 対象がPC、サモン、ペット
				if (_player.getZoneType() == 1 || cha.getZoneType() == 1 // 攻撃する側または攻撃される側がセーフティーゾーン
						|| _player.checkNonPvP(_player, cha)) { // Non-PvP設定
					return false;
				}
			}
		}

		// フォグオブスリーピングは自分自身は対象外
		if (_skillId == FOG_OF_SLEEPING && _user.getId() == cha.getId()) {
			return false;
		}

		// マススローは自分自身と自分のペットは対象外
		if (_skillId == MASS_SLOW) {
			if (_user.getId() == cha.getId()) {
				return false;
			}
			if (cha instanceof L1SummonInstance) {
				L1SummonInstance summon = (L1SummonInstance) cha;
				if (_user.getId() == summon.getMaster().getId()) {
					return false;
				}
			} else if (cha instanceof L1PetInstance) {
				L1PetInstance pet = (L1PetInstance) cha;
				if (_user.getId() == pet.getMaster().getId()) {
					return false;
				}
			}
		}

		// マステレポートは自分自身のみ対象（同時にクラン員もテレポートさせる）
		if (_skillId == MASS_TELEPORT) {
			if (_user.getId() != cha.getId()) {
				return false;
			}
		}

		return true;
	}

	// 対象がPC、サモン、ペットかを返す
	private boolean isPcSummonPet(L1Character cha) {
		if (_calcType == PC_PC) { // 対象がPC
			return true;
		}

		if (_calcType == PC_NPC) {
			if (cha instanceof L1SummonInstance) { // 対象がサモン
				L1SummonInstance summon = (L1SummonInstance) cha;
				if (summon.isExsistMaster()) { // マスターが居る
					return true;
				}
			}
			if (cha instanceof L1PetInstance) { // 対象がペット
				return true;
			}
		}
		return false;
	}

	// ターゲットに対して必ず失敗になるか返す
	private boolean isTargetFailure(L1Character cha) {
		boolean isTU = false;
		boolean isErase = false;
		boolean isManaDrain = false;
		int undeadType = 0;

		if (cha instanceof L1TowerInstance || cha instanceof L1DoorInstance) { // ガーディアンタワー、ドアには確率系スキル無効
			return true;
		}

		if (cha instanceof L1PcInstance) { // 対PCの場合
			if (_calcType == PC_PC && _player.checkNonPvP(_player, cha)) { // Non-PvP設定
				L1PcInstance pc = (L1PcInstance) cha;
				if (_player.getId() == pc.getId()
						|| (pc.getClanid() != 0 && _player.getClanid() == pc
								.getClanid())) {
					return false;
				}
				return true;
			}
			return false;
		}

		if (cha instanceof L1MonsterInstance) { // ターンアンデット可能か判定
			isTU = ((L1MonsterInstance) cha).getNpcTemplate().get_IsTU();
		}

		if (cha instanceof L1MonsterInstance) { // イレースマジック可能か判定
			isErase = ((L1MonsterInstance) cha).getNpcTemplate().get_IsErase();
		}

		if (cha instanceof L1MonsterInstance) { // アンデットの判定
			undeadType = ((L1MonsterInstance) cha).getNpcTemplate()
					.get_undead();
		}

		// マナドレインが可能か？
		if (cha instanceof L1MonsterInstance) {
			isManaDrain = true;
		}
		/*
		 * 成功除外条件１：T-Uが成功したが、対象がアンデットではない。 成功除外条件２：T-Uが成功したが、対象にはターンアンデット無効。
		 * 成功除外条件３：スロー、マススロー、マナドレイン、エンタングル、イレースマジック、ウィンドシャックル無効
		 * 成功除外条件４：マナドレインが成功したが、モンスター以外の場合
		 */
		if ((_skillId == TURN_UNDEAD && (undeadType == 0 || undeadType == 2))
				|| (_skillId == TURN_UNDEAD && isTU == false)
				|| ((_skillId == ERASE_MAGIC || _skillId == SLOW
						|| _skillId == MANA_DRAIN || _skillId == MASS_SLOW
						|| _skillId == ENTANGLE || _skillId == WIND_SHACKLE)
								&& isErase == false)
				|| (_skillId == MANA_DRAIN && isManaDrain == false)) {
			return true;
		}
		return false;
	}

	// カウンターマジックが発動したか返す
	private boolean isUseCounterMagic(L1Character cha) {
		// カウンターマジック有効なスキルでカウンターマジック中
		if (_isCounterMagic && cha.hasSkillEffect(COUNTER_MAGIC)) {
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
	private void useDragonBraveSkill(L1PcInstance pc,int time) {
		pc.sendPackets(new S_SkillBrave(pc.getId(), 6, time));
		pc.broadcastPacket(new S_SkillBrave(pc.getId(), 6, 0));
		pc.setSkillEffect(STATUS_BRAVE, time * 1000);
		pc.setBraveSpeed(1);
	}
}
