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

package l1j.server.server.serverpackets;

import java.util.ArrayList;
import java.util.logging.Logger;

import l1j.server.server.Opcodes;
import l1j.server.server.model.Instance.L1PcInstance;

/**
 * 屬於PacketBox的封包 只是抓出來另外寫
 * GameStart	進入賽跑的畫面
 * GameEnd		離開賽跑的畫面
 */

public class Expand_S_Race extends ServerBasePacket {
	
	public static final int GameStart = 0x40;
	public static final int CountDown = 0x41;
	public static final int PlayerInfo = 0x42;
	public static final int Lap = 0x43;
	public static final int Winner = 0x44;
	public static final int GameOver = 0x45;
	public static final int GameEnd = 0x46;
	
	
	//GameStart// CountDown// GameOver// GameEnd
	public Expand_S_Race(int type){
		writeC(Opcodes.S_OPCODE_PACKETBOX);
		writeC(type);
		if(type == GameStart){
			writeC(0x05);	//倒數5秒
		}
	}
	
	public Expand_S_Race(ArrayList<L1PcInstance> playerList,L1PcInstance pc){
		writeC(Opcodes.S_OPCODE_PACKETBOX);
		writeC(PlayerInfo);
		writeH(playerList.size()); //參賽者人數
		writeH(playerList.indexOf(pc)); //名次
		for(L1PcInstance player : playerList){
			if(player == null){
				continue;
			}
			writeS(player.getName());
		}
	}
	public Expand_S_Race(int maxLap,int lap){
		writeC(Opcodes.S_OPCODE_PACKETBOX);
		writeC(Lap);
		writeH(maxLap);	//最大圈術
		writeH(lap); 	//目前圈術
	}
	public Expand_S_Race(String winnerName,int time){
		writeC(Opcodes.S_OPCODE_PACKETBOX);
		writeC(Winner);
		writeS(winnerName);
		writeD(time * 1000);
	}

	

	private static Logger _log = Logger.getLogger(Expand_S_Race.class.getName());
	private static final String Expand_S_Race = "[S] Expand_S_Race";
	private byte[] _byte = null;
	
	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = getBytes();
		}
		return _byte;
	}
	@Override
	public String getType() {
		return Expand_S_Race;
	}
}
