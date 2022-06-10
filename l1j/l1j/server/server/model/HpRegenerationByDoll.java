package l1j.server.server.model;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_SkillSound; //娃娃回血效果

public class HpRegenerationByDoll extends TimerTask {
    private static Logger _log = Logger.getLogger(HpRegenerationByDoll.class
            .getName());

    private final L1PcInstance _pc;

    public HpRegenerationByDoll(L1PcInstance pc) {
        _pc = pc;
    }

    @Override
    public void run() {
        try {
            if (_pc.isDead()) {
                return;
            }
            regenHp();
        } catch (Throwable e) {
            _log.log(Level.WARNING, e.getLocalizedMessage(), e);
        }
    }

    public void regenHp() {
        int mpr = 40;
        if (_pc.get_food() < 3 || isOverWeight(_pc)) {
            mpr = 0;
        }
        int newHp = _pc.getCurrentHp() + mpr;
        if (newHp < 0) {
            newHp = 0;
        }
//add 娃娃回血效果
        _pc.sendPackets(new S_SkillSound(_pc.getId(), 6321));//修正希爾黛絲特效編號 by0968026609
        _pc.broadcastPacket(new S_SkillSound(_pc.getId(), 6321));//修正希爾黛絲特效編號 by0968026609
//end
        _pc.setCurrentHp(newHp);

    }

    private boolean isOverWeight(L1PcInstance pc) {
    	// エキゾチックバイタライズ状態、アディショナルファイアー状態か
		// ゴールデンウィング装備時であれば、重量オーバーでは無いとみなす。
		if (pc.hasSkillEffect(L1SkillId.EXOTIC_VITALIZE)
                || pc.hasSkillEffect(L1SkillId.ADDITIONAL_FIRE)) {
            return false;
        }

        return (120 <= pc.getInventory().getWeight240()) ? true : false;
    }
}