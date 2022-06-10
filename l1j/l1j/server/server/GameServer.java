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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.datatables.CastleTable;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.datatables.ChatLogTable;
import l1j.server.server.datatables.ClanTable;
import l1j.server.server.datatables.DoorSpawnTable;
import l1j.server.server.datatables.DropTable;
import l1j.server.server.datatables.DropItemTable;
import l1j.server.server.datatables.FurnitureSpawnTable;
import l1j.server.server.datatables.GetBackRestartTable;
import l1j.server.server.datatables.IpTable;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.datatables.MailTable;
import l1j.server.server.datatables.MapsTable;
import l1j.server.server.datatables.MobGroupTable;
import l1j.server.server.datatables.NpcActionTable;
import l1j.server.server.datatables.NpcChatTable;
import l1j.server.server.datatables.NpcSpawnTable;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.datatables.NPCTalkDataTable;
import l1j.server.server.datatables.PetTable;
import l1j.server.server.datatables.PetTypeTable;
import l1j.server.server.datatables.PolyTable;
import l1j.server.server.datatables.ResolventTable;
import l1j.server.server.datatables.ShopTable;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.datatables.SpawnTable;
import l1j.server.server.datatables.SprTable;
import l1j.server.server.datatables.UBSpawnTable;
import l1j.server.server.datatables.UbSupplies;
import l1j.server.server.datatables.WeaponSkillTable;
import l1j.server.server.model.Dungeon;
import l1j.server.server.model.ElementalStoneGenerator;
import l1j.server.server.model.Getback;
import l1j.server.server.model.L1BossCycle;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1DeleteItemOnGround;
import l1j.server.server.model.L1NpcRegenerationTimer;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.gametime.L1GameRestart;
import l1j.server.server.model.gametime.L1GameTimeClock;
import l1j.server.server.model.item.L1TreasureBox;
import l1j.server.server.model.map.L1WorldMap;
import l1j.server.server.model.trap.L1WorldTraps;
import l1j.server.server.utils.SystemUtil;
//新增怪物隨機地圖產生  by eric1300460
import l1j.eric.RandomMobTable;
//~新增怪物隨機地圖產生  by eric1300460

//修正攻城時間如果不是比現在時間久無法更新錯誤 by eric1300460
import l1j.eric.StartCheckWarTime;
//~修正攻城時間如果不是比現在時間久無法更新錯誤 by eric1300460

public class GameServer extends Thread {
	private ServerSocket _serverSocket;
	private static Logger _log = Logger.getLogger(GameServer.class.getName());
	private int _port;
	private LoginController _loginController;
	private int chatlvl;
	
	@Override
	public void run() {
		System.out.println("=================================================");
		System.out.println("使用記憶體：" + SystemUtil.getUsedMemoryMB() + "MB");
		System.out.println("等待玩家連線...");
		while (true) {
			try {
				Socket socket = _serverSocket.accept();
				System.out.println("嘗試連線的IP " + socket.getInetAddress());
				String host = socket.getInetAddress().getHostAddress();
				if (IpTable.getInstance().isBannedIp(host)) {
					_log.info("banned IP(" + host + ")");
				} else {
					ClientThread client = new ClientThread(socket);
					GeneralThreadPool.getInstance().execute(client);
				}
			} catch (IOException ioexception) {
			}
		}
	}

	private static GameServer _instance;

	private GameServer() {
		super("GameServer");
	}

	public static GameServer getInstance() {
		if (_instance == null) {
			_instance = new GameServer();
		}
		return _instance;
	}

	public void initialize() throws Exception {
		String s = Config.GAME_SERVER_HOST_NAME;
		int RATE_XP = (int)Config.RATE_XP; //經驗
		int RATE_LA = (int)Config.RATE_LA; //正義
		int RATE_KARMA = (int)Config.RATE_KARMA; //好友
		int RATE_WEIGHT = (int)Config.RATE_WEIGHT_LIMIT; //負重
		int RATE_WEIGHT_PET = (int)Config.RATE_WEIGHT_LIMIT_PET; //寵物負重
		int j = (int)Config.RATE_DROP_ITEMS; //掉寶
		int k = (int)Config.RATE_DROP_ADENA; //金錢
		int PetExp = (int)Config.RATE_XP_PET;//寵物經驗
		int l = Config.ENCHANT_CHANCE_WEAPON; //衝武
		int m = Config.ENCHANT_CHANCE_ARMOR; //衝防

		chatlvl = Config.GLOBAL_CHAT_LEVEL;
		_port = Config.GAME_SERVER_PORT;
		if (!"*".equals(s)) {
			InetAddress inetaddress = InetAddress.getByName(s);
			inetaddress.getHostAddress();
			_serverSocket = new ServerSocket(_port, 50, inetaddress);
		} else {
			_serverSocket = new ServerSocket(_port);
		}
		System.out.println("經驗【" + RATE_XP + "】倍");
		System.out.println("金幣【"+ k + "】倍");
		System.out.println("掉寶【" + j + "】倍");
		System.out.println("正義值【" + RATE_LA +"】倍");
		System.out.println("友好度【" + RATE_KARMA + "】倍");
		System.out.println("角色負重【" + RATE_WEIGHT +"】倍");
		System.out.println("寵物經驗【" + PetExp + "】倍");
		System.out.println("寵物負重【" + RATE_WEIGHT_PET + "】倍");
		System.out.println("衝武【" + l + "】%");
		System.out.println("衝防【" + m + "】%");
		System.out.println("=================================================");
		System.out.println("公頻可使用最低等級【" + chatlvl + "】");
		int maxOnlineUsers = Config.MAX_ONLINE_USERS;
		System.out.println("連接人數限制最多【" + (maxOnlineUsers) + "】人");
		System.out.println("伺服器可以PvP：【"+((Config.ALT_NONPVP)?"開啟":"關閉")+"】");
		System.out.println("寵物都可抓進化:【"+((Config.CE_ACTIVE)?"有效":"無效")+"】");
		System.out.println("同IP可開兩個以上的帳號：【"+((Config.ALLOW_2PC)?"開啟":"關閉")+"】");
		System.out.println("GM商店：【"+((Config.ALT_GMSHOP)?"開啟":"關閉")+"】");
		System.out.println("循環公告：【"+((Config.Use_Show_Announcecycle)?"開啟":"關閉")+"】");
		System.out.println("在線一段時間給予物品：【"+((Config.GITorF)?"開啟":"關閉")+"】");
		System.out.println("衝武防廣播：【"+((Config.SuccessBoard)?"開啟":"關閉")+"】");
		System.out.println("帳號可創多少角色: " + Config.DEFAULT_CHARACTER_SLOT);
		System.out.println("=================================================");
		System.out.println("                                  For All User...");
		System.out.println("=================================================");
		IdFactory.getInstance();
		L1WorldMap.getInstance();
		_loginController = LoginController.getInstance();
		_loginController.setMaxAllowedOnlinePlayers(maxOnlineUsers);

		// 全キャラクターネームロード
		CharacterTable.getInstance().loadAllCharName();

		// オンライン状態リセット
		CharacterTable.clearOnlineStatus();

		// ゲーム時間時計
		L1GameTimeClock.init();

		// UBタイムコントローラー
		UbTimeController ubTimeContoroller = UbTimeController.getInstance();
		GeneralThreadPool.getInstance().execute(ubTimeContoroller);

		// 戦争タイムコントローラー
		WarTimeController warTimeController = WarTimeController.getInstance();
		GeneralThreadPool.getInstance().execute(warTimeController);

		// 精霊の石生成
		if (Config.ELEMENTAL_STONE_AMOUNT > 0) {
			ElementalStoneGenerator elementalStoneGenerator = ElementalStoneGenerator.getInstance();
			GeneralThreadPool.getInstance().execute(elementalStoneGenerator);
		}

		// ホームタウン
		HomeTownTimeController.getInstance();

		// アジト競売タイムコントローラー
		AuctionTimeController auctionTimeController = AuctionTimeController.getInstance();
		GeneralThreadPool.getInstance().execute(auctionTimeController);

		// アジト税金タイムコントローラー
		HouseTaxTimeController houseTaxTimeController = HouseTaxTimeController.getInstance();
		GeneralThreadPool.getInstance().execute(houseTaxTimeController);

		// 釣りタイムコントローラー
		FishingTimeController fishingTimeController = FishingTimeController.getInstance();
		GeneralThreadPool.getInstance().execute(fishingTimeController);

		// NPCチャットタイムコントローラー
		NpcChatTimeController npcChatTimeController = NpcChatTimeController.getInstance();
		GeneralThreadPool.getInstance().execute(npcChatTimeController);

		// ライトタイムコントローラー
		LightTimeController lightTimeController = LightTimeController.getInstance();
		GeneralThreadPool.getInstance().execute(lightTimeController);
		
		Announcements.getInstance();
		NpcTable.getInstance();
		L1DeleteItemOnGround deleteitem = new L1DeleteItemOnGround();
		deleteitem.initialize();

		if (!NpcTable.getInstance().isInitialized()) {
			throw new Exception("Could not initialize the npc table");
		}
		SpawnTable.getInstance();
		MobGroupTable.getInstance();
		SkillsTable.getInstance();
		PolyTable.getInstance();
		ItemTable.getInstance();
		DropTable.getInstance();
		DropItemTable.getInstance();
		ShopTable.getInstance();
		NPCTalkDataTable.getInstance();
		L1World.getInstance();
		UbSupplies.getInstance();
		L1WorldTraps.getInstance();
		Dungeon.getInstance();
		NpcSpawnTable.getInstance();
		IpTable.getInstance();
		MapsTable.getInstance();
		UBSpawnTable.getInstance();
		PetTable.getInstance();
		ClanTable.getInstance();
		//修正攻城時間如果不是比現在時間久無法更新錯誤 by eric1300460
		StartCheckWarTime.getInstance();
		//~修正攻城時間如果不是比現在時間久無法更新錯誤 by eric1300460
		//add 自動重啟
		L1GameRestart.init();
		CastleTable.getInstance();
		L1CastleLocation.setCastleTaxRate(); // これはCastleTable初期化後でなければいけない
		GetBackRestartTable.getInstance();
		DoorSpawnTable.getInstance();
		GeneralThreadPool.getInstance();
		L1NpcRegenerationTimer.getInstance();
		ChatLogTable.getInstance();
		WeaponSkillTable.getInstance();
		NpcActionTable.load();
		GMCommandsConfig.load();
		Getback.loadGetBack();
		PetTypeTable.load();
		L1BossCycle.load();
		L1TreasureBox.load();
		SprTable.getInstance();
		ResolventTable.getInstance();
		FurnitureSpawnTable.getInstance();
		NpcChatTable.getInstance();
		MailTable.getInstance();
		//開始怪物隨機出生點
		RandomMobTable.getInstance().startRandomMob();
		//~開始怪物隨機出生點	
		//TODO 循環公告 by 雷公
		if (Config.Use_Show_Announcecycle) {
			Announcecycle.getInstance();
		}
		//end
		System.out.println("=================================================");
		System.out.println("《伺服器啟動完畢》");
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		this.start();
	}

	/**
	 * オンライン中のプレイヤー全てに対してkick、キャラクター情報の保存をする。
	 */
	public void disconnectAllCharacters() {
		Collection<L1PcInstance> players = L1World.getInstance().getAllPlayers();
		for (L1PcInstance pc : players) {
			pc.getNetConnection().setActiveChar(null);
			pc.getNetConnection().kick();
		}
		// 全員Kickした後に保存処理をする
		for (L1PcInstance pc : players) {
			ClientThread.quitGame(pc);
			L1World.getInstance().removeObject(pc);
		}
	}

	private class ServerShutdownThread extends Thread {
		private final int _secondsCount;

		public ServerShutdownThread(int secondsCount) {
			_secondsCount = secondsCount;
		}

		@Override
		public void run() {
			L1World world = L1World.getInstance();
			try {
				int secondsCount = _secondsCount;
				world.broadcastServerMessage("伺服器即將關閉。");
				world.broadcastServerMessage("請移動至安全區域。");
				while (0 < secondsCount) {
					if (secondsCount <= 30) {
						world.broadcastServerMessage("伺服器將會在" + secondsCount + "秒後關機。請玩家先行離線。");
					} else {
						if (secondsCount % 60 == 0) {
							world.broadcastServerMessage("伺服器將會在" + secondsCount / 60 + "分後關機。");
						}
					}
					Thread.sleep(1000);
					secondsCount--;
				}
				shutdown();
			} catch (InterruptedException e) {
				world.broadcastServerMessage("已取消伺服器關機。伺服器將會正常運作。");
				return;
			}
		}
	}

	private ServerShutdownThread _shutdownThread = null;

	public synchronized void shutdownWithCountdown(int secondsCount) {
		if (_shutdownThread != null) {
			// 既にシャットダウン要求が行われている
			// TODO エラー通知が必要かもしれない
			return;
		}
		_shutdownThread = new ServerShutdownThread(secondsCount);
		GeneralThreadPool.getInstance().execute(_shutdownThread);
	}

	public void shutdown() {
		disconnectAllCharacters();
		System.exit(0);
	}

	public synchronized void abortShutdown() {
		if (_shutdownThread == null) {
			// シャットダウン要求が行われていない
			// TODO エラー通知が必要かもしれない
			return;
		}

		_shutdownThread.interrupt();
		_shutdownThread = null;
	}
}
