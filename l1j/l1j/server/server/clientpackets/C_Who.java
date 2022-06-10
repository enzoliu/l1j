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

package l1j.server.server.clientpackets;

import l1j.server.Config;
import l1j.server.server.ClientThread;
import l1j.server.server.GetNowTime;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.gametime.L1GameRestart;
import l1j.server.server.serverpackets.S_WhoAmount;
import l1j.server.server.serverpackets.S_WhoCharinfo;

//伊川
import l1j.server.server.serverpackets.S_SystemMessage;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_Who extends ClientBasePacket {

	private static final String C_WHO = "[C] C_Who";

	public C_Who(byte[] decrypt, ClientThread client) {
		super(decrypt);
		String s = readS();
		L1PcInstance find = L1World.getInstance().getPlayer(s);
		L1PcInstance pc = client.getActiveChar();

		if (find != null) {
			S_WhoCharinfo s_whocharinfo = new S_WhoCharinfo(find);
			pc.sendPackets(s_whocharinfo);
		} else {
			if (Config.ALT_WHO_COMMAND || pc.isGm() ) {
				String amount = String.valueOf(L1World.getInstance().getAllPlayers().size());
				S_WhoAmount s_whoamount = new S_WhoAmount(amount);
				pc.sendPackets(s_whoamount);
				pc.sendPackets(new S_SystemMessage("【經驗】"+Config.RATE_XP+"倍【正義】"+Config.RATE_LA+"倍【金錢】"+Config.RATE_DROP_ADENA+"倍"));
                pc.sendPackets(new S_SystemMessage("【好友】"+Config.RATE_KARMA+"倍【角色負重】"+Config.RATE_WEIGHT_LIMIT+"倍【掉寶】"+Config.RATE_DROP_ITEMS+"倍"));
                pc.sendPackets(new S_SystemMessage("【武器】"+Config.ENCHANT_CHANCE_WEAPON+"倍【防具】"+Config.ENCHANT_CHANCE_ARMOR+"倍【寵物負重】"+Config.RATE_WEIGHT_LIMIT_PET+"倍"));
                pc.sendPackets(new S_SystemMessage("【寵物經驗】"+Config.RATE_XP_PET+"倍【販賣】"+Config.RATE_SHOP_SELLING_PRICE+"倍【收購】"+Config.RATE_SHOP_PURCHASING_PRICE+"倍"));
                pc.sendPackets(new S_SystemMessage("【吸血吸魔】"+Config.GET_HPMP_MAX_NUM+"%【吸血Max】"+Config.DrainHP+"滴【吸魔Max】"+Config.DrainMP+"滴"));
				pc.sendPackets( new S_SystemMessage("☆"+"今天是 " + GetNowTime.GetNowYear() + " 年 "	+ (GetNowTime.GetNowMonth()+1)+ " 月 " + GetNowTime.GetNowDay() + " 日☆"));
				pc.sendPackets( new S_SystemMessage("☆"+"現在時刻" + GetNowTime.GetNowHour() + " 時 " + GetNowTime.GetNowMinute() + " 分 " + GetNowTime.GetNowSecond() + " 秒☆"));
				int second = L1GameRestart.getWillRestartTime();
				String part = "";
				if(pc.getKarmaLevel() >= 1){
					part = "原生魔族";
				}else if(pc.getKarmaLevel() <= -1){
					part = "不死魔族";
				}else{
					part = "目前尚未達到雙方陣營最低階層";
				}
				pc.sendPackets(new S_SystemMessage("【友好度】"+pc.getKarma()+"【陣營】"+part));
				
				pc.sendPackets(new S_SystemMessage("☆距離伺服器重開時間還有 " + ( second / 60 ) / 60 + " 小時 " + ( second / 60 ) % 60+ " 分 " + second % 60 + " 秒☆"));
		        int i=1;
	            for (L1PcInstance pc1 : L1World.getInstance().getAllPlayers()) {
	            	if (pc.isGm() == true) {
	            		pc.sendPackets(new S_SystemMessage(i+"【玩家】【"+pc1.getName()+"】【血盟】【"+pc1.getClanname()+"】【等級【"+pc1.getLevel()+"】"));
	               	} else {
	               		pc.sendPackets(new S_SystemMessage(i+"【玩家】【"+pc1.getName()+"】【血盟】【"+pc1.getClanname()+"】"));
	                }
	                i++;
	            }
	            //add by sdcom 2010.7.28
	            if(Config.exp_combo_time){
	            	pc.sendPackets(new S_SystemMessage("目前為經驗值加倍時間，加倍時間至"+Config.EXP_COMBO_END_HOUR+"點結束(24小時制)。"));
	            }    
			}
			// 対象が居ない場合はメッセージ表示する？わかる方修正お願いします。
		}
	}

	@Override
	public String getType() {
		return C_WHO;
	}
}
