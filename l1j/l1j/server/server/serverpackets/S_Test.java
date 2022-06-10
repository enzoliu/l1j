package l1j.server.server.serverpackets;

import l1j.server.server.Opcodes;

public class S_Test extends ServerBasePacket {

	public S_Test(int num, boolean flag) {
		writeC(Opcodes.S_OPCODE_PARALYSIS);
		// 0 1
		// 6 7
		// 8 9
		// 14 15
		// 16 17
		// 18 19
		// 20 21
		// 26 27
		if (flag == true) {
			writeC(num);
		} else {
			writeC(num+1);
		}
	}

	@Override
	public byte[] getContent() {
		return getBytes();
	}
}
