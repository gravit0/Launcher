package ru.zaxar163;

import launcher.helper.CommonHelper;
import launcher.helper.LogHelper;

public class Main {
    public static void main(String[] args) {
        GuardBind.avnRegisterThreatNotifier((int threatType) -> {
            System.err.println("Threat " + GuardBind.ThreatType.getThreat(threatType).name());
            return false;
        });
        GuardBind.avnStartDefence();
        GuardBind.setCheckTime(2000);
        CommonHelper.newThread("Security Thread",true,new SecurityThread()).start();
        //GuardBind.avnEliminateThreat(GuardBind.ThreatType.UNKNOWN_APC_DESTINATION.getValue());

        //GuardBind.avnRegisterThreatNotifier(null);
        //GuardBind.avnEliminateThreat(GuardBind.ThreatType.UNKNOWN_APC_DESTINATION.getValue());
    }
    static class SecurityThread implements Runnable
    {
        @Override
        public void run() {
            while (true) {
                try {
                    if (!GuardBind.avnIsStarted()) {
                        LogHelper.error("Avanguard stopped! Process stopped");
                        System.exit(5);
                    }
                } catch (NullPointerException e) {
                    LogHelper.error("Avanguard unloaded! Process stopped");
                    System.exit(6);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    if (!GuardBind.avnIsStarted()) {
                        LogHelper.error("Thread stopped! Process stopped");
                        System.exit(7);
                    }
                }
            }
        }
    }
}
