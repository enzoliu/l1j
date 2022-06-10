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

package l1j.server.server.utils;

import java.util.ArrayList;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.datatables.ExpTable;
import l1j.server.server.datatables.PetTable;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_ItemName;
import l1j.server.server.serverpackets.S_PetPack;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Pet;
import static l1j.server.server.model.skill.L1SkillId.*;

// Referenced classes of package l1j.server.server.utils:
// CalcStat

public class CalcExp {

	private static final long serialVersionUID = 1L;

	private static Logger _log = Logger.getLogger(CalcExp.class.getName());

	public static final int MAX_EXP = ExpTable.getExpByLevel(100) - 1;

	private CalcExp() {
	}
	//fix by sdcom 2009.12.24
	public static void calcExp(L1PcInstance l1pcinstance, int targetid,
			ArrayList acquisitorList, ArrayList hateList, int exp) {

		int i = 0;
		double party_level = 0;
		double dist = 0;
		int member_exp = 0;
		int member_lawful = 0;
		L1Object l1object = L1World.getInstance().findObject(targetid);
		L1NpcInstance npc = (L1NpcInstance) l1object;
		
		// ヘイトの合計を取得
		L1Character acquisitor;
		int hate = 0;
		int acquire_exp = 0;
		int acquire_lawful = 0;
		int party_exp = 0;
		int party_lawful = 0;
		int totalHateExp = 0;
		int totalHateLawful = 0;
		int partyHateExp = 0;
		int partyHateLawful = 0;
		int ownHateExp = 0;
		if (acquisitorList.size() != hateList.size()) {
			return;
		}
		
		for (i = hateList.size() - 1; i >= 0; i--) {
			acquisitor = (L1Character) acquisitorList.get(i);
			hate = (Integer) hateList.get(i);
			if (acquisitor != null && !acquisitor.isDead()) {
				totalHateExp += hate;
				if (acquisitor instanceof L1PcInstance) {
					totalHateLawful += hate;
				}
			} else { // nullだったり死んでいたら排除
				acquisitorList.remove(i);
				hateList.remove(i);
			}
		}
		if (totalHateExp == 0) { // 取得者がいない場合
			return;
		}
		
		if (l1object != null && !(npc instanceof L1PetInstance)	&& !(npc instanceof L1SummonInstance)) {
			// 村莊貢獻度計算
			if (!L1World.getInstance().isProcessingContributionTotal() && l1pcinstance.getHomeTownId() > 0) {
				int contribution = npc.getLevel() / 10;
				l1pcinstance.addContribution(contribution);
			}
			int lawful = npc.getLawful();
			// 組隊情況計算
			if (l1pcinstance.isInParty()) {
				// パーティーのヘイトの合計を算出
				// パーティーメンバー以外にはそのまま配分
				partyHateExp = 0;
				partyHateLawful = 0;
				for (i = hateList.size() - 1; i >= 0; i--) {
					acquisitor = (L1Character) acquisitorList.get(i);
					hate = (Integer) hateList.get(i);
					if (acquisitor instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) acquisitor;
						if (pc == l1pcinstance) {
							partyHateExp += hate;
							partyHateLawful += hate;
						} else if (l1pcinstance.getParty().isMember(pc)) {
							partyHateExp += hate;
							partyHateLawful += hate;
						} else {
							if (totalHateExp > 0) {
								//fix by sdcom 2011.6.25
								//acquire_exp = (exp * (hate / totalHateExp));
								acquire_exp = exp * (1+hateList.size()/10);
							}
							if (totalHateLawful > 0) {
								//acquire_lawful = (lawful * (hate / totalHateLawful));
								acquire_lawful = lawful * (1+hateList.size()/10);
							}
							AddExp(pc, acquire_exp, acquire_lawful);
						}
					} else if (acquisitor instanceof L1PetInstance) {
						L1PetInstance pet = (L1PetInstance) acquisitor;
						L1PcInstance master = (L1PcInstance) pet.getMaster();
						if (master == l1pcinstance) {
							partyHateExp += hate;
						} else if (l1pcinstance.getParty().isMember(master)) {
							partyHateExp += hate;
						} else {
							if (totalHateExp > 0) {
								acquire_exp = (exp * (hate / totalHateExp));
							}
							AddExpPet(pet, acquire_exp);
						}
					} else if (acquisitor instanceof L1SummonInstance) {
						L1SummonInstance summon = (L1SummonInstance) acquisitor;
						L1PcInstance master = (L1PcInstance) summon.getMaster();
						if (master == l1pcinstance) {
							partyHateExp += hate;
						} else if (l1pcinstance.getParty().isMember(master)) {
							partyHateExp += hate;
						} else {
						}
					}
				}
				if (totalHateExp > 0) {
					party_exp = (exp * partyHateExp / totalHateExp);
				}
				if (totalHateLawful > 0) {
					party_lawful = (lawful * partyHateLawful / totalHateLawful);
				}

				// EXP、ロウフル配分

				// プリボーナス
				double pri_bonus = 0;
				L1PcInstance leader = l1pcinstance.getParty().getLeader();
				if (leader.isCrown()
						&& (l1pcinstance.knownsObject(leader)
								|| l1pcinstance.equals(leader))) {
					pri_bonus = 0.059;
				}

				// PT経験値の計算
				L1PcInstance[] ptMembers = l1pcinstance.getParty().getMembers();
				double pt_bonus = 0;
				for (L1PcInstance each : ptMembers) {
					if (l1pcinstance.knownsObject(each)
							|| l1pcinstance.equals(each)) {
						party_level += each.getLevel() * each.getLevel();
					}
					if (l1pcinstance.knownsObject(each)) {
						pt_bonus += 0.04;
					}
				}

				party_exp = (int) (party_exp * (1 + pt_bonus + pri_bonus));

				// 自キャラクターとそのペット・サモンのヘイトの合計を算出
				if (party_level > 0) {
					dist = ((l1pcinstance.getLevel() * l1pcinstance.getLevel()) / party_level);
				}
				member_exp = (int) (party_exp * dist);
				member_lawful = (int) (party_lawful * dist);

				ownHateExp = 0;
				for (i = hateList.size() - 1; i >= 0; i--) {
					acquisitor = (L1Character) acquisitorList.get(i);
					hate = (Integer) hateList.get(i);
					if (acquisitor instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) acquisitor;
						if (pc == l1pcinstance) {
							ownHateExp += hate;
						}
					} else if (acquisitor instanceof L1PetInstance) {
						L1PetInstance pet = (L1PetInstance) acquisitor;
						L1PcInstance master = (L1PcInstance) pet.getMaster();
						if (master == l1pcinstance) {
							ownHateExp += hate;
						}
					} else if (acquisitor instanceof L1SummonInstance) {
						L1SummonInstance summon = (L1SummonInstance) acquisitor;
						L1PcInstance master = (L1PcInstance) summon.getMaster();
						if (master == l1pcinstance) {
							ownHateExp += hate;
						}
					}
				}
				// 自キャラクターとそのペット・サモンに分配
				if (ownHateExp != 0) { // 攻撃に参加していた
					for (i = hateList.size() - 1; i >= 0; i--) {
						acquisitor = (L1Character) acquisitorList.get(i);
						hate = (Integer) hateList.get(i);
						if (acquisitor instanceof L1PcInstance) {
							L1PcInstance pc = (L1PcInstance) acquisitor;
							if (pc == l1pcinstance) {
								if (ownHateExp > 0) {
									acquire_exp = (member_exp * (hate / ownHateExp));
								}
								AddExp(pc, acquire_exp, member_lawful);
							}
						} else if (acquisitor instanceof L1PetInstance) {
							L1PetInstance pet = (L1PetInstance) acquisitor;
							L1PcInstance master = (L1PcInstance) pet
									.getMaster();
							if (master == l1pcinstance) {
								if (ownHateExp > 0) {
									acquire_exp = (member_exp * (hate / ownHateExp));
								}
								AddExpPet(pet, acquire_exp);
							}
						} else if (acquisitor instanceof L1SummonInstance) {
						}
					}
				} else { // 攻撃に参加していなかった
					// 自キャラクターのみに分配
					AddExp(l1pcinstance, member_exp, member_lawful);
				}

				// パーティーメンバーとそのペット・サモンのヘイトの合計を算出
				for (int cnt = 0; cnt < ptMembers.length; cnt++) {
					if (l1pcinstance.knownsObject(ptMembers[cnt])) {
						if (party_level > 0) {
							dist = ((ptMembers[cnt].getLevel() * ptMembers[cnt]
									.getLevel()) / party_level);
						}
						member_exp = (int) (party_exp * dist);
						member_lawful = (int) (party_lawful * dist);

						ownHateExp = 0;
						for (i = hateList.size() - 1; i >= 0; i--) {
							acquisitor = (L1Character) acquisitorList.get(i);
							hate = (Integer) hateList.get(i);
							if (acquisitor instanceof L1PcInstance) {
								L1PcInstance pc = (L1PcInstance) acquisitor;
								if (pc == ptMembers[cnt]) {
									ownHateExp += hate;
								}
							} else if (acquisitor instanceof L1PetInstance) {
								L1PetInstance pet = (L1PetInstance) acquisitor;
								L1PcInstance master = (L1PcInstance) pet
										.getMaster();
								if (master == ptMembers[cnt]) {
									ownHateExp += hate;
								}
							} else if (acquisitor instanceof L1SummonInstance) {
								L1SummonInstance summon = (L1SummonInstance) acquisitor;
								L1PcInstance master = (L1PcInstance) summon
										.getMaster();
								if (master == ptMembers[cnt]) {
									ownHateExp += hate;
								}
							}
						}
						// パーティーメンバーとそのペット・サモンに分配
						if (ownHateExp != 0) { // 攻撃に参加していた
							for (i = hateList.size() - 1; i >= 0; i--) {
								acquisitor = (L1Character) acquisitorList
										.get(i);
								hate = (Integer) hateList.get(i);
								if (acquisitor instanceof L1PcInstance) {
									L1PcInstance pc = (L1PcInstance) acquisitor;
									if (pc == ptMembers[cnt]) {
										if (ownHateExp > 0) {
											acquire_exp = (member_exp * (hate / ownHateExp));
										}
										AddExp(pc, acquire_exp, member_lawful);
									}
								} else if (acquisitor instanceof L1PetInstance) {
									L1PetInstance pet = (L1PetInstance) acquisitor;
									L1PcInstance master = (L1PcInstance) pet
											.getMaster();
									if (master == ptMembers[cnt]) {
										if (ownHateExp > 0) {
											acquire_exp = (member_exp * (hate / ownHateExp));
										}
										AddExpPet(pet, acquire_exp);
									}
								} else if (acquisitor instanceof L1SummonInstance) {
								}
							}
						} else { // 攻撃に参加していなかった
							// パーティーメンバーのみに分配
							AddExp(ptMembers[cnt], member_exp, member_lawful);
						}
					}
				}
			} else { 
				// 非組隊情況計算
				// EXP、ロウフルの分配
				for (i = hateList.size() - 1; i >= 0; i--) {
					acquisitor = (L1Character) acquisitorList.get(i);
					hate = (Integer) hateList.get(i);
					acquire_exp = exp * hate / totalHateExp;
					if (acquisitor instanceof L1PcInstance) {
						if (totalHateLawful > 0) {
							acquire_lawful = lawful * hate / totalHateLawful;
						}
					}

					if (acquisitor instanceof L1PcInstance) {
						L1PcInstance pc = (L1PcInstance) acquisitor;
						AddExp(pc, acquire_exp, acquire_lawful);
					} else if (acquisitor instanceof L1PetInstance) {
						L1PetInstance pet = (L1PetInstance) acquisitor;
						AddExpPet(pet, acquire_exp);
					}
				}
			}
		}
	}

	private static void AddExp(L1PcInstance pc, int exp, int lawful) {

		int add_lawful = (int) (lawful * Config.RATE_LA) * -1;
		pc.addLawful(add_lawful);

		double exppenalty = ExpTable.getPenaltyRate(pc.getLevel());
		double foodBonus = 1.0;
		if (pc.hasSkillEffect(COOKING_1_7_N)
				|| pc.hasSkillEffect(COOKING_1_7_S)) {
			foodBonus = 1.01;
		}
		if (pc.hasSkillEffect(COOKING_2_7_N)
				|| pc.hasSkillEffect(COOKING_2_7_S)) {
			foodBonus = 1.02;
		}
		if (pc.hasSkillEffect(COOKING_3_7_N)
				|| pc.hasSkillEffect(COOKING_3_7_S)) {
			foodBonus = 1.03;
		}
		//add 經驗加倍藥水
		if (pc.hasSkillEffect(L1SkillId.EXP_UP_A)) {
			foodBonus = 1.50;	// 食物效果經驗1.5倍
		}else if (pc.hasSkillEffect(L1SkillId.EXP_UP_B)) {
			foodBonus = 2.00;	// 食物效果經驗2.0倍
		}else if (pc.hasSkillEffect(L1SkillId.EXP_UP_C)) {
			foodBonus = 2.50;	// 食物效果經驗2.5倍
		}
		//end
		int add_exp = (int) (exp * exppenalty * Config.RATE_XP * foodBonus);
		//add by sdcom 2010.7.28
		if(Config.EXP_COMBO_ENABLE && Config.exp_combo_time){
			add_exp *= Config.EXP_COMBO_RATE;	
		}
		pc.addExp(add_exp);
	}

	private static void AddExpPet(L1PetInstance pet, int exp) {
		L1PcInstance pc = (L1PcInstance) pet.getMaster();
		
		int petNpcId = pet.getNpcTemplate().get_npcId();
		int petItemObjId = pet.getItemObjId();

		int levelBefore = pet.getLevel();
		//TODO 寵物經驗倍率
		int totalExp = (int) (exp * Config.RATE_XP_PET + pet.getExp());
		//end
		//TODO 寵物最高等級
		/*if (totalExp >= ExpTable.getExpByLevel(51)) {
			totalExp = ExpTable.getExpByLevel(51) - 1;
		}*/
		if (totalExp >= ExpTable.getExpByLevel(Config.Pet_Max_LV + 1)) {
			totalExp = ExpTable.getExpByLevel(Config.Pet_Max_LV + 1) - 1; 
		}
		//end
		pet.setExp(totalExp);

		pet.setLevel(ExpTable.getLevelByExp(totalExp));

		int expPercentage = ExpTable.getExpPercentage(pet.getLevel(), totalExp);

		int gap = pet.getLevel() - levelBefore;
		for (int i = 1; i <= gap; i++) {
			IntRange hpUpRange = pet.getPetType().getHpUpRange();
			IntRange mpUpRange = pet.getPetType().getMpUpRange();
			pet.addMaxHp(hpUpRange.randomValue());
			pet.addMaxMp(mpUpRange.randomValue());
		}

		pet.setExpPercent(expPercentage);
		pc.sendPackets(new S_PetPack(pet, pc));
		

		if (gap != 0) { // レベルアップしたらDBに書き込む
			L1Pet petTemplate = PetTable.getInstance().getTemplate(petItemObjId);
			if (petTemplate == null) { // PetTableにない
				_log.warning("L1Pet == null");
				return;
			}
			petTemplate.set_exp(pet.getExp());
			petTemplate.set_level(pet.getLevel());
			petTemplate.set_hp(pet.getMaxHp());
			petTemplate.set_mp(pet.getMaxMp());
			
			PetTable.getInstance().storePet(petTemplate); // DBに書き込み
			pc.sendPackets(new S_ServerMessage(320, pet.getName())); // \f1%0のレベルが上がりました。
			
			// 項圈名稱改變
			L1ItemInstance l1iteminstance = pc.getInventory().getItem(petItemObjId);
			pc.sendPackets(new S_ItemName(l1iteminstance));
		}
	}
}