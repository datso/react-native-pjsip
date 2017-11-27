package com.carusto.ReactNativePjSip;

import org.pjsip.pjsua2.SipHeader;
import org.pjsip.pjsua2.SipHeaderVector;

import java.util.Map;

public class PjSipUtils {

    public static SipHeaderVector mapToSipHeaderVector(Map<String, String> headers) {
        SipHeaderVector hdrsVector = new SipHeaderVector();

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            SipHeader hdr = new SipHeader();
            hdr.setHName(entry.getKey());
            hdr.setHValue(entry.getValue());

            hdrsVector.add(hdr);
        }

        return hdrsVector;
    }

}
