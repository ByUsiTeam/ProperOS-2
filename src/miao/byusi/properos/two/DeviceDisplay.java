package miao.byusi.properos.two;

import java.net.URLDecoder;
import org.teleal.cling.model.meta.Device;

public class DeviceDisplay {
    Device device;

    public DeviceDisplay(Device device) {
        this.device = device;
    }

    public Device getDevice() {
        return this.device;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return this.device.equals(((DeviceDisplay) obj).device);
    }

    public int hashCode() {
        return this.device.hashCode();
    }

    public String toString() {
        String decode;
        Exception exception;
        String friendlyName;
        if (this.device.getDetails().getFriendlyName() != null) {
            friendlyName = this.device.getDetails().getFriendlyName();
            try {
                friendlyName = URLDecoder.decode(friendlyName, "UTF-8");
                decode = URLDecoder.decode(friendlyName, "iso-8859-1");
            } catch (Exception e) {
                exception = e;
                decode = friendlyName;
                exception.printStackTrace();
            }
        } else {
            friendlyName = this.device.getDisplayString();
            try {
                friendlyName = URLDecoder.decode(friendlyName, "UTF-8");
                decode = URLDecoder.decode(friendlyName, "iso-8859-1");
            } catch (Exception e2) {
                exception = e2;
                decode = friendlyName;
                exception.printStackTrace();
            }
        }
        if (this.device.isFullyHydrated()) {
            return decode;
        }
        return decode + " *";
    }
}