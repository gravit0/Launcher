package launcher.agent;

import java.lang.instrument.Instrumentation;

public class Agent {
	public static Instrumentation inst = null;
	public static void premain(String agentArgument, Instrumentation instrumentation) {
		System.out.println("Launcher is preparing for start.");
		instrumentation.addTransformer(new ClassTransformer());
		inst = instrumentation;
	}
	
	public static long getSize(Object obj) {
		if (inst == null) {
			throw new IllegalStateException("Agent not initialised");
		}
		return inst.getObjectSize(obj);
	}
}