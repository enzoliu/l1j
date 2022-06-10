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

import java.util.ArrayList;
import java.util.logging.Logger;

import l1j.server.server.ActionCodes;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.datatables.DropTable;
import l1j.server.server.datatables.NPCTalkDataTable;
import l1j.server.server.model.L1Attack;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1NpcTalkData;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_AttackPacket;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_NPCTalkReturn;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.types.Point;
import static l1j.server.server.model.skill.L1SkillId.*;
import l1j.server.server.utils.CalcExp;

public class L1GuardInstance extends L1NpcInstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger _log = Logger.getLogger(L1GuardInstance.class
			.getName());

	// ターゲットを探す
	@Override
	public void searchTarget() {
		// ターゲット捜索
		L1PcInstance targetPlayer = null;
		for (L1PcInstance pc : L1World.getInstance().getVisiblePlayer(this)) {
			if (pc.getCurrentHp() <= 0 || pc.isDead() || pc.isGm()
					|| pc.isGhost()) {
				continue;
			}
			if (!pc.isInvisble() || getNpcTemplate().is_agrocoi()) // インビジチェック
			{
				if (pc.isWanted()) { // PKで手配中か
					targetPlayer = pc;
					break;
				}
			}
		}
		if (targetPlayer != null) {
			_hateList.add(targetPlayer, 0);
			_target = targetPlayer;
		}
	}

	public void setTarget(L1PcInstance targetPlayer) {
		if (targetPlayer != null) {
			_hateList.add(targetPlayer, 0);
			_target = targetPlayer;
		}
	}

	// ターゲットがいない場合の処理
	@Override
	public boolean noTarget() {
		if (getLocation()
				.getTileLineDistance(new Point(getHomeX(), getHomeY())) > 0) {
			int dir = moveDirection(getHomeX(), getHomeY());
			if (dir != -1) {
				setDirectionMove(dir);
				setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
			} else // 遠すぎるor経路が見つからない場合はテレポートして帰る
			{
				teleport(getHomeX(), getHomeY(), 1);
			}
		} else {
			if (L1World.getInstance().getRecognizePlayer(this).size() == 0) {
				return true; // 周りにプレイヤーがいなくなったらＡＩ処理終了
			}
		}
		return false;
	}

	public L1GuardInstance(L1Npc template) {
		super(template);
	}

	@Override
	public void onNpcAI() {
		if (isAiRunning()) {
			return;
		}
		setActived(false);
		startAI();
	}

	@Override
	public void onAction(L1PcInstance pc) {
		if (!isDead()) {
			if (getCurrentHp() > 0) {
				L1Attack attack = new L1Attack(pc, this);
				if (attack.calcHit()) {
					attack.calcDamage();
					attack.calcStaffOfMana();
					attack.addPcPoisonAttack(pc, this);
				}
				attack.action();
				attack.commit();
			} else {
				L1Attack attack = new L1Attack(pc, this);
				attack.calcHit();
				attack.action();
			}
		}
	}

	@Override
	public void onTalkAction(L1PcInstance player) {
		int objid = getId();
		L1NpcTalkData talking = NPCTalkDataTable.getInstance().getTemplate(
				getNpcTemplate().get_npcId());
		int npcid = getNpcTemplate().get_npcId();
		String htmlid = null;
		String[] htmldata = null;
		boolean hascastle = false;
		String clan_name = "";
		String pri_name = "";

		if (talking != null) {
			// キーパー
			//变更成 switch 
			switch (npcid)
			{
				case 70549: // ケント城左外门キーパー
				case 70985: {// ケント城右外门キーパー
				hascastle = checkHasCastle(player,
					L1CastleLocation.KENT_CASTLE_ID);
				if (hascastle) { // 城主クラン员
					htmlid = "gateokeeper";
					htmldata = new String[] { player.getName() };
				} else {
					htmlid = "gatekeeperop";
				}
				}
				break;
				case 70656: { // ケント城内门キーパー
				hascastle = checkHasCastle(player,
						L1CastleLocation.KENT_CASTLE_ID);
				if (hascastle) { // 城主クラン员
					htmlid = "gatekeeper";
					htmldata = new String[] { player.getName() };
				} else {
					htmlid = "gatekeeperop";
				}	
				}
				break;
				case 70600: // オークの森外门キーパー
				case 70986: {
				hascastle = checkHasCastle(player,
						L1CastleLocation.OT_CASTLE_ID);
				if (hascastle) { // 城主クラン员
					htmlid = "orckeeper";
				} else {
					htmlid = "orckeeperop";
				}
					
				}
				break;
				case 70687: // ウィンダウッド城外门キーパー
				case 70987: {
				hascastle = checkHasCastle(player,
						L1CastleLocation.WW_CASTLE_ID);
				if (hascastle) { // 城主クラン员
					htmlid = "gateokeeper";
					htmldata = new String[] { player.getName() };
				} else {
					htmlid = "gatekeeperop";
				}
				}
				break;
				case 70778: { // ウィンダウッド城内门キーパー
				hascastle = checkHasCastle(player,
						L1CastleLocation.WW_CASTLE_ID);
				if (hascastle) { // 城主クラン员
					htmlid = "gatekeeper";
					htmldata = new String[] { player.getName() };
				} else {
					htmlid = "gatekeeperop";
				}	
				}
				break;
				case 70800: case 70988: case 70989: case 70990: case 70991: {
				hascastle = checkHasCastle(player,
						L1CastleLocation.GIRAN_CASTLE_ID);
				if (hascastle) { // 城主クラン员
					htmlid = "gateokeeper";
					htmldata = new String[] { player.getName() };
				} else {
					htmlid = "gatekeeperop";
				}
				}
				break;
				case 70817: { // ギラン城内门キーパー
				hascastle = checkHasCastle(player,
						L1CastleLocation.GIRAN_CASTLE_ID);
				if (hascastle) { // 城主クラン员
					htmlid = "gatekeeper";
					htmldata = new String[] { player.getName() };
				} else {
					htmlid = "gatekeeperop";
				}	
				}
				break;
				case 70862: // ハイネ城外门キーパー
				case 70992: {
				hascastle = checkHasCastle(player,
						L1CastleLocation.HEINE_CASTLE_ID);
				if (hascastle) { // 城主クラン员
					htmlid = "gateokeeper";
					htmldata = new String[] { player.getName() };
				} else {
					htmlid = "gatekeeperop";
				}
				}
				break;
				case 70863: { // ハイネ城内门キーパー
				hascastle = checkHasCastle(player,
						L1CastleLocation.HEINE_CASTLE_ID);
				if (hascastle) { // 城主クラン员
					htmlid = "gatekeeper";
					htmldata = new String[] { player.getName() };
				} else {
					htmlid = "gatekeeperop";
				}	
				}
				break;
				case 70993: case 70994: {
				hascastle = checkHasCastle(player,
						L1CastleLocation.DOWA_CASTLE_ID);
				if (hascastle) { // 城主クラン员
					htmlid = "gateokeeper";
					htmldata = new String[] { player.getName() };
				} else {
					htmlid = "gatekeeperop";
				}
				}
				break;
				case 70995: { // ドワーフ城内门キーパー
				hascastle = checkHasCastle(player,
						L1CastleLocation.DOWA_CASTLE_ID);
				if (hascastle) { // 城主クラン员
					htmlid = "gatekeeper";
					htmldata = new String[] { player.getName() };
				} else {
					htmlid = "gatekeeperop";
				}	
				}
				break;
				case 70996: { // アデン城内门キーパー
				hascastle = checkHasCastle(player,
						L1CastleLocation.ADEN_CASTLE_ID);
				if (hascastle) { // 城主クラン员
					htmlid = "gatekeeper";
					htmldata = new String[] { player.getName() };
				} else {
					htmlid = "gatekeeperop";
				}
				}
				break;
				case 60514: { // ケント城近卫兵
				for (L1Clan clan : L1World.getInstance().getAllClans()) {
					if (clan.getCastleId() // 城主クラン
					== L1CastleLocation.KENT_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "ktguard6";
				htmldata = new String[] { getName(), clan_name, pri_name };
				}
				break;
				case 60560: { // オーク近卫兵
				for (L1Clan clan : L1World.getInstance().getAllClans()) {
					if (clan.getCastleId() // 城主クラン
					== L1CastleLocation.OT_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "orcguard6";
				htmldata = new String[] { getName(), clan_name, pri_name };	
				}
				break;
				case 60552: { // ウィンダウッド城近卫兵
				for (L1Clan clan : L1World.getInstance().getAllClans()) {
					if (clan.getCastleId() // 城主クラン
					== L1CastleLocation.WW_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "wdguard6";
				htmldata = new String[] { getName(), clan_name, pri_name };	
				}
				break;
				case 60524: // ギラン街入り口近卫兵(弓)
				case 60525: // ギラン街入り口近卫兵
				case 60529: { // ギラン城近卫兵
				for (L1Clan clan : L1World.getInstance().getAllClans()) {
					if (clan.getCastleId() // 城主クラン
					== L1CastleLocation.GIRAN_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "grguard6";
				htmldata = new String[] { getName(), clan_name, pri_name };
				}
				break;
				case 70857: { // ハイネ城ハイネ ガード
				for (L1Clan clan : L1World.getInstance().getAllClans()) {
					if (clan.getCastleId() // 城主クラン
					== L1CastleLocation.HEINE_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "heguard6";
				htmldata = new String[] { getName(), clan_name, pri_name };
				}
				break;
				case 60530: // ドワーフ城ドワーフ ガード
				case 60531: {
				for (L1Clan clan : L1World.getInstance().getAllClans()) {
					if (clan.getCastleId() // 城主クラン
					== L1CastleLocation.DOWA_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "dcguard6";
				htmldata = new String[] { getName(), clan_name, pri_name };
				}
				break;
				case 60533: // アデン城 ガード
				case 60534: {
				for (L1Clan clan : L1World.getInstance().getAllClans()) {
					if (clan.getCastleId() // 城主クラン
					== L1CastleLocation.ADEN_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "adguard6";
				htmldata = new String[] { getName(), clan_name, pri_name };
				}
				break;
				case 81156: { // アデン侦察兵（ディアド要塞）
				for (L1Clan clan : L1World.getInstance().getAllClans()) {
					if (clan.getCastleId() // 城主クラン
					== L1CastleLocation.DIAD_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "ktguard6";
				htmldata = new String[] { getName(), clan_name, pri_name };
				}
				break;
			}
			//变更成 switch  end

			// html表示パケット送信
			if (htmlid != null) { // htmlidが指定されている场合
				if (htmldata != null) { // html指定がある场合は表示
					player.sendPackets(new S_NPCTalkReturn(objid, htmlid,
							htmldata));
				} else {
					player.sendPackets(new S_NPCTalkReturn(objid, htmlid));
				}
			} else {
				if (player.getLawful() < -1000) { // プレイヤーがカオティック
					player.sendPackets(new S_NPCTalkReturn(talking, objid, 2));
				} else {
					player.sendPackets(new S_NPCTalkReturn(talking, objid, 1));
				}
			}
		}
	}

	public void onFinalAction() {

	}

	public void doFinalAction() {

	}
	
	// TODO 警衛可以殺死 & 幫打 by 阿傑
	public void ReceiveManaDamage(L1Character attacker, int mpDamage) {
		if (mpDamage > 0 && !isDead()) {

			setHate(attacker, mpDamage);
			onNpcAI();

			if (attacker instanceof L1PcInstance) {
				serchLink((L1PcInstance) attacker, getNpcTemplate()
						.get_family());
			}

			int newMp = getCurrentMp() - mpDamage;
			if (newMp < 0) {
				newMp = 0;
			}
			setCurrentMp(newMp);
		}
	}

	// 警衛可以殺死 & 幫打 end

	@Override
	public void receiveDamage(L1Character attacker, int damage) // 攻擊でＨＰを減らすときはここを使用
	{
		// TODO 警衛可以殺死 & 幫打 by 阿傑
		if (getCurrentHp() > 0 && !isDead()) {
			// 警衛可以殺死 & 幫打 end
			if (damage >= 0) {
				// int Hate = damage / 10 + 10; // ダメージの１０分の１＋ヒットヘイト１０
				// setHate(attacker, Hate);
				setHate(attacker, damage);
				removeSkillEffect(FOG_OF_SLEEPING);
			}

			onNpcAI();
			// TODO 警衛可以殺死 & 幫打 by 阿傑
			if (attacker instanceof L1PcInstance) {
				serchLink((L1PcInstance) attacker, getNpcTemplate()
						.get_family());
			}
			// 警衛可以殺死 & 幫打 end
			if (attacker instanceof L1PcInstance && damage > 0) {
				L1PcInstance player = (L1PcInstance) attacker;
				player.setPetTarget(this);
			}
			// TODO 警衛可以殺死 & 幫打 by 阿傑
			int newHp = getCurrentHp() - damage;
			if (newHp <= 0 && !isDead()) {
				setCurrentHpDirect(0);
				setDead(true);
				_lastattacker = attacker;
				Death death = new Death();
				GeneralThreadPool.getInstance().execute(death);
				// Death(attacker);
			}
			if (newHp > 0)
				setCurrentHp(newHp);
		} else if (!isDead()) {
			setDead(true);
			System.out.println("警告：ＮＰＣ的ＨＰ減少處理發生異常，視為最初ＨＰ０處理。");
			_lastattacker = attacker;
			Death death = new Death();
			GeneralThreadPool.getInstance().execute(death);
			// Death(attacker);
		}
		// 警衛可以殺死 & 幫打 end
	}

	// TODO 警衛可以殺死 & 幫打 by 阿傑
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
	}

	// 警衛可以殺死 & 幫打 end

	// TODO 警衛可以殺死 & 幫打 by 阿傑
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

	// 警衛可以殺死 & 幫打 end

	// TODO 警衛可以殺死 & 幫打 by 阿傑
	private L1Character _lastattacker;

	class Death implements Runnable {
		L1Character lastAttacker = _lastattacker;
		L1Object object = L1World.getInstance().findObject(getId());
		L1GuardInstance npc = (L1GuardInstance) object;

		@Override
		public void run() {
			setDeathProcessing(true);
			setCurrentHpDirect(0);
			setDead(true);
			int targetobjid = getId();
			npc.getMap().setPassable(npc.getLocation(), true);

			broadcastPacket(new S_AttackPacket(lastAttacker,targetobjid, 8));

			L1PcInstance player = null;
			if (lastAttacker instanceof L1PcInstance)
				player = (L1PcInstance) lastAttacker;
			else if (lastAttacker instanceof L1PetInstance)
				player = (L1PcInstance) ((L1PetInstance) lastAttacker)
						.getMaster();
			else if (lastAttacker instanceof L1SummonInstance)
				player = (L1PcInstance) ((L1SummonInstance) lastAttacker)
						.getMaster();
						
			if (player != null) {
				ArrayList<L1Character> targetList = _hateList.toTargetArrayList();
				ArrayList<Integer> hateList = _hateList.toHateArrayList();
				int exp = getExp();
				CalcExp.calcExp(player, targetobjid, targetList, hateList, exp);

				try {
					DropTable.getInstance().dropShare(npc, targetList,
							hateList);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			setDeathProcessing(false);

			setExp(0);
			allTargetClear();
			DeleteNpc delete_timer = new DeleteNpc();
			GeneralThreadPool.getInstance().execute(delete_timer);
		}
	}
	
	class DeleteNpc implements Runnable {
		public void run() {
			try {
				Thread.sleep(10000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!isDead() || _destroyed)
				return;
			deleteMe();
		}
	}
	
	public void setLink(L1Character cha) {
		if (cha != null && _hateList.isEmpty()) {
			_hateList.add(cha, 0);
			checkTarget();
		}
	}
	//警衛可以殺死 & 幫打  end

	private boolean checkHasCastle(L1PcInstance pc, int castleId) {
		boolean isExistDefenseClan = false;
		for (L1Clan clan : L1World.getInstance().getAllClans()) {
			if (castleId == clan.getCastleId()) {
				isExistDefenseClan = true;
				break;
			}
		}
		if (!isExistDefenseClan) { // 城主クランが居ない
			return true;
		}

		if (pc.getClanid() != 0) { // クラン所属中
			L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
			if (clan != null) {
				if (clan.getCastleId() == castleId) {
					return true;
				}
			}
		}
		return false;
	}

}
