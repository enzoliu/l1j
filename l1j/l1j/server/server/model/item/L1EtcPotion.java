package l1j.server.server.model.item;

import static l1j.server.server.model.skill.L1SkillId.DECAY_POTION;
import static l1j.server.server.model.skill.L1SkillId.ENTANGLE;
import static l1j.server.server.model.skill.L1SkillId.GREATER_HASTE;
import static l1j.server.server.model.skill.L1SkillId.HASTE;
import static l1j.server.server.model.skill.L1SkillId.HOLY_WALK;
import static l1j.server.server.model.skill.L1SkillId.MASS_SLOW;
import static l1j.server.server.model.skill.L1SkillId.MOVING_ACCELERATION;
import static l1j.server.server.model.skill.L1SkillId.SLOW;
import static l1j.server.server.model.skill.L1SkillId.STATUS_BLUE_POTION;
import static l1j.server.server.model.skill.L1SkillId.STATUS_BRAVE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CURSE_BARLOG;
import static l1j.server.server.model.skill.L1SkillId.STATUS_ELFBRAVE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HASTE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_RIBRAVE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_UNDERWATER_BREATH;
import static l1j.server.server.model.skill.L1SkillId.STATUS_WISDOM_POTION;
import static l1j.server.server.model.skill.L1SkillId.WIND_WALK;

import java.util.Random;

import l1j.server.Config;
import l1j.server.server.ClientThread;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_OwnCharStatus2;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillBrave;
import l1j.server.server.serverpackets.S_SkillHaste;
import l1j.server.server.serverpackets.S_SkillIconBlessOfEva;
import l1j.server.server.serverpackets.S_SkillIconGFX;
import l1j.server.server.serverpackets.S_SkillIconWisdomPotion;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_SystemMessage;

public class L1EtcPotion extends L1Potion {
	private static Random _random = new Random();
	public L1EtcPotion(){
	}
	public void use(L1PcInstance pc,ClientThread client,L1ItemInstance l1iteminstance1,L1ItemInstance l1iteminstance,int itemId) throws Exception {
		if (itemId == 62006) { // 經驗加倍藥水(150%)
			if (!pc.hasSkillEffect(7000) && !pc.hasSkillEffect(7001) && !pc.hasSkillEffect(7002)) { // 確認經驗加倍狀態
				int time = 3600 * 1000; // 設置時間為1小時
				pc.setSkillEffect(7000, time); // 增加魔法狀態7000
				pc.getInventory().removeItem(l1iteminstance, 1); // 移除物品
				pc.sendPackets(new S_SystemMessage("受到經驗之神的祝福，狩獵後經驗提升1.5倍！"));
			}else{
				int time = pc.getSkillEffectTimeSec(7000)+
				pc.getSkillEffectTimeSec(7001)+
				pc.getSkillEffectTimeSec(7002);
				pc.sendPackets(new S_SystemMessage("經驗加倍藥水時間剩餘 "+time+" 秒"));
			}
		} else if (itemId == 62007) { // 經驗加倍藥水(200%)
			if (!pc.hasSkillEffect(7000) && !pc.hasSkillEffect(7001) && !pc.hasSkillEffect(7002)) { // 確認經驗加倍狀態
				int time = 3600 * 1000; // 設置時間為1小時
				pc.setSkillEffect(7001, time); // 增加魔法狀態7001
				pc.getInventory().removeItem(l1iteminstance, 1); // 移除物品
				pc.sendPackets(new S_SystemMessage("受到經驗之神的祝福，狩獵後經驗提升2.0倍！"));
			}else{
				int time = pc.getSkillEffectTimeSec(7000)+
				pc.getSkillEffectTimeSec(7001)+
				pc.getSkillEffectTimeSec(7002);
				pc.sendPackets(new S_SystemMessage("經驗加倍藥水時間剩餘 "+time+" 秒"));
			}
		} else if (itemId == 62008) { // 經驗加倍藥水(250%)
			if (!pc.hasSkillEffect(7000) && !pc.hasSkillEffect(7001) && !pc.hasSkillEffect(7002)) { // 確認經驗加倍狀態
				int time = 3600 * 1000; // 設置時間為1小時
				pc.setSkillEffect(7002, time); // 增加魔法狀態7002
				pc.getInventory().removeItem(l1iteminstance, 1); // 移除物品
				pc.sendPackets(new S_SystemMessage("受到經驗之神的祝福，狩獵後經驗提升2.5倍！"));
			}else{
				int time = pc.getSkillEffectTimeSec(7000)+
				pc.getSkillEffectTimeSec(7001)+
				pc.getSkillEffectTimeSec(7002);
				pc.sendPackets(new S_SystemMessage("經驗加倍藥水時間剩餘 "+time+" 秒"));
			}
		} else if (itemId == 62009) {	//調寶加倍藥水(500%)  2011.6.5 sdcom
			if (!pc.hasSkillEffect(7003)){
				int time = 3600 * 1000;
				pc.setSkillEffect(7003, time);
				pc.getInventory().removeItem(l1iteminstance, 1);
				pc.sendPackets(new S_SystemMessage("受到幸運之神的祝福，狩獵後掉寶機率提升5倍！"));
			}else{
				int time = pc.getSkillEffectTimeSec(7003);
				pc.sendPackets(new S_SystemMessage("掉寶加倍藥水時間剩餘 "+time+" 秒"));
			}
		} else if (itemId == 999989) { // HP+10 
			if(!pc.getInventory().checkItem(itemId,1)){
				return;
			}             
			pc.addBaseMaxHp((byte) 10); // hp+10 
			pc.getInventory().removeItem(l1iteminstance, 1); 
			pc.sendPackets(new S_SystemMessage("你的HP值永久+10")); 
			pc.save();
			pc.sendPackets(new S_OwnCharStatus(pc));                                
		} else if (itemId == 999987) { // HP+75 
			if(!pc.getInventory().checkItem(itemId,1)){
				return;
			}            
			pc.addBaseMaxHp((byte) 75); // hp+75 
			pc.getInventory().removeItem(l1iteminstance, 1); 
			pc.sendPackets(new S_SystemMessage("你的HP值永久+75")); 
			pc.save();
			pc.sendPackets(new S_OwnCharStatus(pc));                 
		} else if (itemId == 999988) { // MP+10 
			if(!pc.getInventory().checkItem(itemId,1)){
				return;
			}            
			pc.addBaseMaxMp((byte) 10); // Mp+10 
			pc.getInventory().removeItem(l1iteminstance, 1); 
			pc.sendPackets(new S_SystemMessage("你的MP值永久+10")); 
			pc.save();
			pc.sendPackets(new S_OwnCharStatus(pc));                 
		} else if (itemId == 999986) { // MP+75 
			if(!pc.getInventory().checkItem(itemId,1)){
				return;
			}       
			pc.addBaseMaxMp((byte) 75); // Mp+75 
			pc.getInventory().removeItem(l1iteminstance, 1); 
			pc.sendPackets(new S_SystemMessage("你的MP值永久+75")); 
			pc.save();
			pc.sendPackets(new S_OwnCharStatus(pc));      
		} else if (itemId == 43000) { //轉生藥水
			pc.setExp(1);
			pc.resetLevel();
			pc.setBonusStats(0);
			//轉生可設定血魔保留多少 by eric1300460
			pc.setBaseMaxHp((short)(pc.getBaseMaxHp()*Config.EVOLUTION_HP/100));
			pc.setBaseMaxMp((short)(pc.getBaseMaxMp()*Config.EVOLUTION_MP/100));
			pc.setMaxHp(pc.getBaseMaxHp());
			pc.setMaxMp(pc.getBaseMaxMp());
			pc.setCurrentHp(pc.getMaxHp());
			pc.setCurrentMp(pc.getMaxMp());
			pc.sendPackets(new S_SkillSound(pc.getId(), 191));
			pc.broadcastPacket(new S_SkillSound(pc.getId(), 191));
			pc.sendPackets(new S_OwnCharStatus(pc));
			pc.getInventory().removeItem(l1iteminstance, 1);
			pc.sendPackets(new S_ServerMessage(822)); // 独自アイテムですので、メッセージは適当です。
			pc.save(); // DBにキャラクター情報を書き込む
		} else if (itemId == 40033) { // エリクサー:腕力
			//可設定能力值上限 by srwh
			if (pc.getBaseStr() < Config.MAX_ABILITY3 && pc.getElixirStats() < Config.MAX_ABILITY2) {
				pc.addBaseStr((byte) 1); // 素のSTR値に+1
				pc.setElixirStats(pc.getElixirStats() + 1);
				pc.getInventory().removeItem(l1iteminstance, 1);
				pc.sendPackets(new S_OwnCharStatus2(pc));
				pc.save();
				// DBにキャラクター情報を書き込む
			} else {
				if(pc.getBaseStr() >= Config.MAX_ABILITY3){
					pc.sendPackets(new S_SystemMessage("能力值Str"+Config.MAX_ABILITY3+"以後不能喝萬能藥! "));
				}
				if(pc.getElixirStats() >= Config.MAX_ABILITY2){
					pc.sendPackets(new S_SystemMessage("萬能藥只能喝"+Config.MAX_ABILITY2+"瓶"));
				}
			}
		} else if (itemId == 40034) { // エリクサー:体力
			if (pc.getBaseCon() < Config.MAX_ABILITY3 && pc.getElixirStats() < Config.MAX_ABILITY2) {
				pc.addBaseCon((byte) 1); // 素のCON値に+1
				pc.setElixirStats(pc.getElixirStats() + 1);
				pc.getInventory().removeItem(l1iteminstance, 1);
				pc.sendPackets(new S_OwnCharStatus2(pc));
				pc.save();
			} else {
				if(pc.getBaseCon() >= Config.MAX_ABILITY3){
					pc.sendPackets(new S_SystemMessage("Con能力值"+Config.MAX_ABILITY3+"以後不能喝萬能藥! "));
				}if(pc.getElixirStats() >= Config.MAX_ABILITY2){
					pc.sendPackets(new S_SystemMessage("萬能藥只能喝"+Config.MAX_ABILITY2+"瓶"));
				}
			}
		} else if (itemId == 40035) { // エリクサー:機敏
			if (pc.getBaseDex() < Config.MAX_ABILITY3 && pc.getElixirStats() < Config.MAX_ABILITY2) {
				pc.addBaseDex((byte) 1); // 素のDEX値に+1
				pc.resetBaseAc();
				pc.setElixirStats(pc.getElixirStats() + 1);
				pc.getInventory().removeItem(l1iteminstance, 1);
				pc.sendPackets(new S_OwnCharStatus2(pc));
				pc.save();
			} else {
				if(pc.getBaseDex() >= Config.MAX_ABILITY3){
					pc.sendPackets(new S_SystemMessage("Dex能力值"+Config.MAX_ABILITY3+"以後不能喝萬能藥! "));
				}
				if(pc.getElixirStats() >= Config.MAX_ABILITY2){
					pc.sendPackets(new S_SystemMessage("萬能藥只能喝"+Config.MAX_ABILITY2+"瓶"));
				}
			}
		} else if (itemId == 40036) { // エリクサー:知力
			if (pc.getBaseInt() < Config.MAX_ABILITY3 && pc.getElixirStats() < Config.MAX_ABILITY2) {
				pc.addBaseInt((byte) 1); // 素のINT値に+1
				pc.setElixirStats(pc.getElixirStats() + 1);
				pc.getInventory().removeItem(l1iteminstance, 1);
				pc.sendPackets(new S_OwnCharStatus2(pc));
				pc.save();
			} else {
				if(pc.getBaseInt() >= Config.MAX_ABILITY3){
					pc.sendPackets(new S_SystemMessage("Int能力值"+Config.MAX_ABILITY3+"以後不能喝萬能藥! "));
				}
				if(pc.getElixirStats() >= Config.MAX_ABILITY2){
					pc.sendPackets(new S_SystemMessage("萬能藥只能喝"+Config.MAX_ABILITY2+"瓶"));
				}
			}
		} else if (itemId == 40037) { // エリクサー:精神
			if (pc.getBaseWis() < Config.MAX_ABILITY3 && pc.getElixirStats() < Config.MAX_ABILITY2) {
				pc.addBaseWis((byte) 1); // 素のWIS値に+1
				pc.resetBaseMr();
				pc.setElixirStats(pc.getElixirStats() + 1);
				pc.getInventory().removeItem(l1iteminstance, 1);
				pc.sendPackets(new S_OwnCharStatus2(pc));
				pc.save();
			} else {
				if(pc.getBaseWis() >= Config.MAX_ABILITY3){
					pc.sendPackets(new S_SystemMessage("Wis能力值"+Config.MAX_ABILITY3+"以後不能喝萬能藥! "));
				}
				if(pc.getElixirStats() >= Config.MAX_ABILITY2){
					pc.sendPackets(new S_SystemMessage("萬能藥只能喝"+Config.MAX_ABILITY2+"瓶"));
				}
			}
		} else if (itemId == 40038) { // エリクサー:魅力
			if (pc.getBaseCha() < Config.MAX_ABILITY3 && pc.getElixirStats() < Config.MAX_ABILITY2) {
				pc.addBaseCha((byte) 1); // 素のCHA値に+1
				pc.setElixirStats(pc.getElixirStats() + 1);
				pc.getInventory().removeItem(l1iteminstance, 1);
				pc.sendPackets(new S_OwnCharStatus2(pc));
				pc.save();
			} else {
				if(pc.getBaseCha() >= Config.MAX_ABILITY3){
					pc.sendPackets(new S_SystemMessage("Cha能力值"+Config.MAX_ABILITY3+"以後不能喝萬能藥! "));
				}
				if(pc.getElixirStats() >= Config.MAX_ABILITY2){
					pc.sendPackets(new S_SystemMessage("萬能藥只能喝"+Config.MAX_ABILITY2+"瓶"));
				}
			}
		} else if (itemId == 300007) { //重生藥水 
			if (pc.getLevel() <= 10) {
				pc.sendPackets(new S_ServerMessage(79));
				return;
			}
			pc.sendPackets(new S_SkillSound(pc.getId(), 6505));
			pc.broadcastPacket(new S_SkillSound(pc.getId(), 6505));
			pc.getInventory().takeoffEquip(945);//用來脫掉全身裝備
			pc.setExp(10000);
			pc.resetLevel();
			
			short adjustHp = (short)(pc.getBaseMaxHp()*Config.EVOLUTION_HP/200);
			short adjustMp = (short)(pc.getBaseMaxMp()*Config.EVOLUTION_MP/200);
			
			pc.setBaseMaxHp(adjustHp);
			pc.setBaseMaxMp(adjustMp);
			pc.setMaxHp(adjustHp);
			pc.setMaxMp(adjustMp);
			pc.setCurrentHp(adjustHp);
			pc.setCurrentMp(adjustMp);
			pc.resetBaseAc();
			pc.resetBaseMr();
			pc.resetBaseHitup();
			pc.resetBaseDmgup();
			pc.sendPackets(new S_OwnCharStatus(pc));
			pc.sendPackets(new S_ServerMessage(822));
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40017 || itemId == 40507) { // シアンポーション、エントの枝
			if (pc.hasSkillEffect(71) == true) { // ディケイポーションの状態
				pc.sendPackets(new S_ServerMessage(698)); // 魔力によって何も飲むことができません。
			} else {
				cancelAbsoluteBarrier(pc); // アブソルート バリアの解除
				pc.sendPackets(new S_SkillSound(pc.getId(), 192));
				pc.broadcastPacket(new S_SkillSound(pc.getId(), 192));
				if (itemId == L1ItemId.POTION_OF_CURE_POISON) {
					pc.getInventory().removeItem(l1iteminstance, 1);
				} else if (itemId == 40507) {
					pc.getInventory().removeItem(l1iteminstance, 1);
				}
				pc.curePoison();
			}
		} else if (itemId == 40013
				|| itemId == 140013
				|| itemId == 40018 // 強化グリーン ポーション
				|| itemId == 140018 // 祝福された強化グリーン ポーション
				|| itemId == 40030 // 象牙の塔のヘイスト ポーション
				|| itemId == 41338 // 祝福されたワイン
				|| itemId == 41261 // おむすび
				|| itemId == 41262 // 焼き鳥
				|| itemId == 41268 // ピザのピース
				|| itemId == 41269 // 焼きもろこし
				|| itemId == 41271 // ポップコーン
				|| itemId == 41272 // おでん
				|| itemId == 41273 // ワッフル
				|| itemId == 41342) { // メデューサの血
			useGreenPotion(pc, itemId);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40014 // ブレイブポーション
				|| itemId == 140014 // 祝福されたブレイブポーション
				|| itemId == 41415) { // 強化ブレイブポーション
			if (pc.isKnight()) {
				useBravePotion(pc, itemId);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			}
			pc.getInventory().removeItem(l1iteminstance, 1);
		}else if (itemId == 40039) { // 紅酒
			if (pc.isWizard()) {
				useBravePotion(pc, itemId);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			}
			pc.getInventory().removeItem(l1iteminstance, 1);
		}else if (itemId == 40040) { //黑妖  威士忌
			if (pc.isDarkelf()) {
				useBravePotion(pc, itemId);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			}
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 49158) { // 新增生命果實勇水效果
			if (pc.isDragonKnight() || pc.isIllusionist()) {
				useBravePotion(pc, itemId);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			}
			pc.getInventory().removeItem(l1iteminstance, 1); 
		} else if (itemId == 40068 || itemId == 140068) { // 祝福されたエルヴン ワッフル
			if (pc.isElf()) {
				useBravePotion(pc, itemId);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			}
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40031) { // イビル ブラッド
			if (pc.isCrown()) {
				useBravePotion(pc, itemId);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			}
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40733) { // 名誉のコイン
			useBravePotion(pc, itemId);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40066 || itemId == 41413) { // お餅、月餅
			pc.sendPackets(new S_ServerMessage(338, "$1084")); // あなたの%0が回復していきます。
			pc.setCurrentMp(pc.getCurrentMp() + (7 + _random.nextInt(6))); // 7~12
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40067 || itemId == 41414) { // よもぎ餅、福餅
			pc.sendPackets(new S_ServerMessage(338, "$1084")); // あなたの%0が回復していきます。
			pc.setCurrentMp(pc.getCurrentMp() + (15 + _random.nextInt(16))); // 15~30
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40735) { // 勇気のコイン
			pc.sendPackets(new S_ServerMessage(338, "$1084")); // あなたの%0が回復していきます。
			pc.setCurrentMp(pc.getCurrentMp() + 60);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40042) { // スピリットポーション
			pc.sendPackets(new S_ServerMessage(338, "$1084")); // あなたの%0が回復していきます。
			pc.setCurrentMp(pc.getCurrentMp() + 50);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 41404) { // クジャクの霊薬
			pc.sendPackets(new S_ServerMessage(338, "$1084")); // あなたの%0が回復していきます。
			pc.setCurrentMp(pc.getCurrentMp() + (80 + _random.nextInt(21))); // 80~100
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 41412) { // 金のチョンズ
			pc.sendPackets(new S_ServerMessage(338, "$1084")); // あなたの%0が回復していきます。
			pc.setCurrentMp(pc.getCurrentMp() + (5 + _random.nextInt(16))); // 5~20
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40032 || itemId == 40041 || itemId == 41344) { // エヴァの祝福、マーメイドの鱗、水の精粋
			useBlessOfEva(pc, itemId);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40015 || itemId == 140015 || itemId == 40736) { // 知恵のコイン
			useBluePotion(pc, itemId);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40016 || itemId == 140016) { // 祝福されたウィズダム
			if (pc.isWizard()) {
				useWisdomPotion(pc, itemId);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			}
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 41411) { 	// 銀のチョンズ
			UseHeallingPotion(pc, 10, 189);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 300018){	//魔族藥水 add by sdcom 2010.9.14
			pc.setSkillEffect(STATUS_CURSE_BARLOG, 1020 * 1000);
			pc.sendPackets(new S_SkillIconBlessOfEva(pc.getId(), 1020));
			pc.sendPackets(new S_SkillSound(pc.getId(), 750));
			pc.broadcastPacket(new S_SkillSound(pc.getId(), 750));
			pc.sendPackets(new S_ServerMessage(1127));
			pc.getInventory().removeItem(l1iteminstance, 1);
		}
	}
	private void useBlessOfEva(L1PcInstance pc, int item_id) {
		if (pc.hasSkillEffect(71) == true) { // ディケイポーションの状態
			pc.sendPackets(new S_ServerMessage(698)); // \f1魔力によって何も飲むことができません。
			return;
		}

		// アブソルート バリアの解除
		cancelAbsoluteBarrier(pc);

		int time = 0;
		if (item_id == 40032) { // エヴァの祝福
			time = 1800;
		} else if (item_id == 40041) { // マーメイドの鱗
			time = 300;
		} else if (item_id == 41344) { // 水の精粋
			time = 2100;
		} else {
			return;
		}

		if (pc.hasSkillEffect(STATUS_UNDERWATER_BREATH)) {
			int timeSec = pc.getSkillEffectTimeSec(STATUS_UNDERWATER_BREATH);
			time += timeSec;
			if (time > 3600) {
				time = 3600;
			}
		}
		pc.sendPackets(new S_SkillIconBlessOfEva(pc.getId(), time));
		pc.sendPackets(new S_SkillSound(pc.getId(), 190));
		pc.broadcastPacket(new S_SkillSound(pc.getId(), 190));
		pc.setSkillEffect(STATUS_UNDERWATER_BREATH, time * 1000);
	}
	private void useGreenPotion(L1PcInstance pc, int itemId) {
		if (pc.hasSkillEffect(71) == true) { // ディケイポーションの状態
			pc.sendPackets(new S_ServerMessage(698)); // \f1魔力によって何も飲むことができません。
			return;
		}

		// アブソルート バリアの解除
		cancelAbsoluteBarrier(pc);

		int time = 0;
		if (itemId == L1ItemId.POTION_OF_HASTE_SELF) { // グリーン ポーション
			time = 300;
		} else if (itemId == L1ItemId.B_POTION_OF_HASTE_SELF) { // 祝福されたグリーン
			// ポーション
			time = 350;
		} else if (itemId == 40018 || itemId == 41338 || itemId == 41342) { // 強化グリーンポーション、祝福されたワイン、メデューサの血
			time = 1800;
		} else if (itemId == 140018) { // 祝福された強化グリーン ポーション
			time = 2100;
		} else if (itemId == 40030) { // 象牙の塔のヘイスト ポーション
			time = 300;
		} else if (itemId == 41261 || itemId == 41262 || itemId == 41268
				|| itemId == 41269 || itemId == 41271 || itemId == 41272
				|| itemId == 41273) {
			time = 30;
		}

		pc.sendPackets(new S_SkillSound(pc.getId(), 191));
		pc.broadcastPacket(new S_SkillSound(pc.getId(), 191));
		// XXX:ヘイストアイテム装備時、酔った状態が解除されるのか不明
		if (pc.getHasteItemEquipped() > 0) {
			return;
		}
		// 酔った状態を解除
		pc.setDrink(false);

		// ヘイスト、グレーターヘイストとは重複しない
		if (pc.hasSkillEffect(HASTE)) {
			pc.killSkillEffectTimer(HASTE);
			pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
			pc.broadcastPacket(new S_SkillHaste(pc.getId(), 0, 0));
			pc.setMoveSpeed(0);
		} else if (pc.hasSkillEffect(GREATER_HASTE)) {
			pc.killSkillEffectTimer(GREATER_HASTE);
			pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
			pc.broadcastPacket(new S_SkillHaste(pc.getId(), 0, 0));
			pc.setMoveSpeed(0);
		} else if (pc.hasSkillEffect(STATUS_HASTE)) {
			pc.killSkillEffectTimer(STATUS_HASTE);
			pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
			pc.broadcastPacket(new S_SkillHaste(pc.getId(), 0, 0));
			pc.setMoveSpeed(0);
		}

		// スロー、マス スロー、エンタングル中はスロー状態を解除するだけ
		if (pc.hasSkillEffect(SLOW)) { // スロー
			pc.killSkillEffectTimer(SLOW);
			pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
			pc.broadcastPacket(new S_SkillHaste(pc.getId(), 0, 0));
		} else if (pc.hasSkillEffect(MASS_SLOW)) { // マス スロー
			pc.killSkillEffectTimer(MASS_SLOW);
			pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
			pc.broadcastPacket(new S_SkillHaste(pc.getId(), 0, 0));
		} else if (pc.hasSkillEffect(ENTANGLE)) { // エンタングル
			pc.killSkillEffectTimer(ENTANGLE);
			pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
			pc.broadcastPacket(new S_SkillHaste(pc.getId(), 0, 0));
		} else {
			pc.sendPackets(new S_SkillHaste(pc.getId(), 1, time));
			pc.broadcastPacket(new S_SkillHaste(pc.getId(), 1, 0));
			pc.setMoveSpeed(1);
			pc.setSkillEffect(STATUS_HASTE, time * 1000);
		}
	}

	private void useBravePotion(L1PcInstance pc, int item_id) {
		if (pc.hasSkillEffect(71) == true) { // ディケイポーションの状態
			pc.sendPackets(new S_ServerMessage(698)); // \f1魔力によって何も飲むことができません。
			return;
		}

		// アブソルート バリアの解除
		cancelAbsoluteBarrier(pc);

		int time = 0;
		if (item_id == L1ItemId.POTION_OF_EMOTION_BRAVERY) { // ブレイブ ポーション
			time = 300;
		} else if (item_id == L1ItemId.B_POTION_OF_EMOTION_BRAVERY) { // 祝福されたブレイブポーション
			time = 350;
		} else if (item_id == 49158) { // ユグドラの実
			time = 480;
			/*
			if (pc.hasSkillEffect(STATUS_BRAVE)) { // 名誉のコインとは重複しない
				pc.killSkillEffectTimer(STATUS_BRAVE);
				pc.setBraveSpeed(0);
			}*/
		} else if (item_id == 40039) { // 紅酒
			time = 600;
		} else if (item_id == 40040) { // 威士忌
			time = 900;
		} else if (item_id == 41415) { // 強化ブレイブ ポーション
			time = 1800;
		} else if (item_id == 40068) { // エルヴン ワッフル
			time = 600;
			if (pc.hasSkillEffect(STATUS_BRAVE)) { // 名誉のコインとは重複しない
				pc.killSkillEffectTimer(STATUS_BRAVE);
				pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
				pc.broadcastPacket(new S_SkillBrave(pc.getId(), 0, 0));
				pc.setBraveSpeed(0);
			}
			if (pc.hasSkillEffect(WIND_WALK)) { // ウィンドウォークとは重複しない
				pc.killSkillEffectTimer(WIND_WALK);
				pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
				pc.broadcastPacket(new S_SkillBrave(pc.getId(), 0, 0));
				pc.setBraveSpeed(0);
			}
		} else if (item_id == 140068) { // 祝福されたエルヴン ワッフル
			time = 700;
			if (pc.hasSkillEffect(STATUS_BRAVE)) { // 名誉のコインとは重複しない
				pc.killSkillEffectTimer(STATUS_BRAVE);
				pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
				pc.broadcastPacket(new S_SkillBrave(pc.getId(), 0, 0));
				pc.setBraveSpeed(0);
			}
			if (pc.hasSkillEffect(WIND_WALK)) { // ウィンドウォークとは重複しない
				pc.killSkillEffectTimer(WIND_WALK);
				pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
				pc.broadcastPacket(new S_SkillBrave(pc.getId(), 0, 0));
				pc.setBraveSpeed(0);
			}
		} else if (item_id == 40031) { // イビル ブラッド
			time = 600;
		} else if (item_id == 40733) { // 名誉のコイン
			time = 600;
			if (pc.hasSkillEffect(STATUS_ELFBRAVE)) { // エルヴンワッフルとは重複しない
				pc.killSkillEffectTimer(STATUS_ELFBRAVE);
				pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
				pc.broadcastPacket(new S_SkillBrave(pc.getId(), 0, 0));
				pc.setBraveSpeed(0);
			}
			if (pc.hasSkillEffect(HOLY_WALK)) { // ホーリーウォークとは重複しない
				pc.killSkillEffectTimer(HOLY_WALK);
				pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
				pc.broadcastPacket(new S_SkillBrave(pc.getId(), 0, 0));
				pc.setBraveSpeed(0);
			}
			if (pc.hasSkillEffect(MOVING_ACCELERATION)) { // ムービングアクセレーションとは重複しない
				pc.killSkillEffectTimer(MOVING_ACCELERATION);
				pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
				pc.broadcastPacket(new S_SkillBrave(pc.getId(), 0, 0));
				pc.setBraveSpeed(0);
			}
			if (pc.hasSkillEffect(WIND_WALK)) { // ウィンドウォークとは重複しない
				pc.killSkillEffectTimer(WIND_WALK);
				pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
				pc.broadcastPacket(new S_SkillBrave(pc.getId(), 0, 0));
				pc.setBraveSpeed(0);
			}
			if (pc.hasSkillEffect(STATUS_RIBRAVE)) { // ユグドラの実とは重複しない
				pc.killSkillEffectTimer(STATUS_RIBRAVE);
				// XXX ユグドラの実のアイコンを消す方法が不明
				pc.setBraveSpeed(0);
			}
		}

		if (item_id == 40068 || item_id == 140068) { // エルヴン ワッフル
			pc.sendPackets(new S_SkillBrave(pc.getId(), 3, time));
			pc.broadcastPacket(new S_SkillBrave(pc.getId(), 3, 0));
			pc.sendPackets(new S_SkillSound(pc.getId(), 751));
			pc.broadcastPacket(new S_SkillSound(pc.getId(), 751));
			pc.setSkillEffect(STATUS_ELFBRAVE, time * 1000);
		/*
		} else if (item_id == 49158) { // ユグドラの実
			pc.sendPackets(new S_SkillBrave(pc.getId(), 1, time));
			pc.broadcastPacket(new S_SkillBrave(pc.getId(), 1, 0));
			pc.sendPackets(new S_SkillSound(pc.getId(), 7110));
			pc.broadcastPacket(new S_SkillSound(pc.getId(), 7110));
			pc.setSkillEffect(STATUS_RIBRAVE, time * 1000);
			*/
		} else {
			pc.sendPackets(new S_SkillBrave(pc.getId(), 1, time));
			pc.broadcastPacket(new S_SkillBrave(pc.getId(), 1, 0));
			pc.sendPackets(new S_SkillSound(pc.getId(), 751));
			pc.broadcastPacket(new S_SkillSound(pc.getId(), 751));
			pc.setSkillEffect(STATUS_BRAVE, time * 1000);
		}
// pc.sendPackets(new S_SkillSound(pc.getId(), 751));
// pc.broadcastPacket(new S_SkillSound(pc.getId(), 751));
		pc.setBraveSpeed(1);
	}

	private void useBluePotion(L1PcInstance pc, int item_id) {
		if (pc.hasSkillEffect(DECAY_POTION)) { // ディケイポーションの状態
			pc.sendPackets(new S_ServerMessage(698)); // \f1魔力によって何も飲むことができません。
			return;
		}

		// アブソルート バリアの解除
		cancelAbsoluteBarrier(pc);

		int time = 0;
		if (item_id == 40015 || item_id == 40736) { // ブルーポーション、知恵のコイン
			time = 600;
		} else if (item_id == 140015) { // 祝福されたブルー ポーション
			time = 700;
		} else {
			return;
		}

		pc.sendPackets(new S_SkillIconGFX(34, time));
		pc.sendPackets(new S_SkillSound(pc.getId(), 190));
		pc.broadcastPacket(new S_SkillSound(pc.getId(), 190));

		pc.setSkillEffect(STATUS_BLUE_POTION, time * 1000);

		pc.sendPackets(new S_ServerMessage(1007)); // MPの回復速度が速まります。
	}

	private void useWisdomPotion(L1PcInstance pc, int item_id) {
		if (pc.hasSkillEffect(71) == true) { // ディケイポーションの状態
			pc.sendPackets(new S_ServerMessage(698)); // \f1魔力によって何も飲むことができません。
			return;
		}

		// アブソルート バリアの解除
		cancelAbsoluteBarrier(pc);

		int time = 0; // 時間は4の倍数にすること
		if (item_id == L1ItemId.POTION_OF_EMOTION_WISDOM) { // ウィズダム ポーション
			time = 300;
		} else if (item_id == L1ItemId.B_POTION_OF_EMOTION_WISDOM) { // 祝福されたウィズダム
			// ポーション
			time = 360;
		}

		if (!pc.hasSkillEffect(STATUS_WISDOM_POTION)) {
			pc.addSp(2);
		}

		pc.sendPackets(new S_SkillIconWisdomPotion((time / 4)));
		pc.sendPackets(new S_SkillSound(pc.getId(), 750));
		pc.broadcastPacket(new S_SkillSound(pc.getId(), 750));

		pc.setSkillEffect(STATUS_WISDOM_POTION, time * 1000);
	}
}
