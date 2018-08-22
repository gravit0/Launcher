package launchserver.auth.hwid;

import java.io.IOException;

import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public class HWID {
	private long hwid_bios;
	private long hwid_hdd;
	private long hwid_cpu;

	private HWID(long hwid_hdd, long hwid_bios, long hwid_cpu) {
		this.hwid_hdd = hwid_hdd;
		this.hwid_bios = hwid_bios;
		this.hwid_cpu = hwid_cpu;
	}

	public static HWID gen(long hwid_hdd, long hwid_bios, long hwid_cpu) {
		return new HWID(hwid_hdd, hwid_bios, hwid_cpu);
	}

	public long getHwid_hdd() {
		return hwid_hdd;
	}
	
	public long getHwid_bios() {
		return hwid_bios;
	}
	
	public long getHwid_cpu() {
		return hwid_cpu;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (hwid_bios ^ (hwid_bios >>> 32));
		result = prime * result + (int) (hwid_cpu ^ (hwid_cpu >>> 32));
		result = prime * result + (int) (hwid_hdd ^ (hwid_hdd >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof HWID)) {
			return false;
		}
		HWID other = (HWID) obj;
		if (hwid_bios != other.hwid_bios) {
			return false;
		}
		if (hwid_cpu != other.hwid_cpu) {
			return false;
		}
		if (hwid_hdd != other.hwid_hdd) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("HWID {hwid_bios=%s, hwid_hdd=%s, hwid_cpu=%s}", hwid_bios, hwid_hdd, hwid_cpu);
	}
	
	public void toData(HOutput out) throws IOException {
		out.writeLong(hwid_hdd);
		out.writeLong(hwid_bios);
		out.writeLong(hwid_cpu);
	}
	
	public static HWID fromData(HInput in) throws IOException {
		return gen(in.readLong(), in.readLong(), in.readLong());
	}
}
