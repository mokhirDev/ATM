package com.mokhir.dev.ATM.service.network;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * @project: middleware-service
 * @Date: 05.09.2022
 * @author: H_Urunov
 **/
@Service
public class NetworkDataService implements NetworkServices {
    //
    private final Logger logger = LoggerFactory.getLogger(NetworkDataService.class);
    private final String LOCALHOST_IPV4 = "127.0.0.1";
    private final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private final String UNKNOWN = "unknown";

    @Override
    public String getClientIPv4Address(HttpServletRequest request) {

        String ipAddress = request.getHeader("X-Forwarded-For");

        if (StringUtils.isEmpty(ipAddress) || UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }

        if (StringUtils.isEmpty(ipAddress) || UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        if (StringUtils.isEmpty(ipAddress) || UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (LOCALHOST_IPV4.equals(ipAddress) || LOCALHOST_IPV6.equals(ipAddress)) {
                try {
                    InetAddress inetAddress = InetAddress.getLocalHost();
                    ipAddress = inetAddress.getHostAddress();
                } catch (UnknownHostException e) {
                    //e.printStackTrace();
                    logger.error(e.getMessage(), e);
                }
            }
        }

        if (!StringUtils.isEmpty(ipAddress)
                && ipAddress.length() > 15
                && ipAddress.indexOf(",") > 0) {
            ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
        }
        //    System.out.println(ipAddress);

        return ipAddress;
    }

    @Override
    public String getClientMACAddress(String IPAddress) throws SocketException {

        // Host computer MAC Address Only
        Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
        NetworkInterface inter;
        System.out.println("MAC address: ");
        while (networks.hasMoreElements()) {
            inter = networks.nextElement();
            byte[] mac = inter.getHardwareAddress();
            if (mac != null) {
                for (int i = 0; i < mac.length; i++) {
                    System.out.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : "");

                }
                System.out.println();
            }
        }
        // TODO: Client Computer MAC Address.
        return networks.nextElement().toString();
    }

    @Override
    public String getClientHostAddress(HttpServletRequest requestHost) {

        String computerName = null;
        String remoteAddress = requestHost.getRemoteAddr();

        try {
            InetAddress inetAddress = InetAddress.getByName(remoteAddress);
            computerName = inetAddress.getHostName();
            if (computerName.equalsIgnoreCase("localhost")) {
                computerName = InetAddress.getLocalHost().getCanonicalHostName();
            }
        } catch (UnknownHostException e) {
            logger.error("UnknownHostException detected in StartAction. ", e);
        }

        assert computerName != null;
        if (StringUtils.trimWhitespace(computerName).length() > 0) computerName = computerName.toUpperCase();
        System.out.println("computerName: " + computerName);
        return computerName;
    }

    public String getRemoteUserInfo(HttpServletRequest requestRemoteUser) {

        if (requestRemoteUser.getHeader("X-Forwarded-Host") != null) {
            return requestRemoteUser.getHeader("X-Forwarded-Host");
        } else {
            return requestRemoteUser.getServerName();
        }
    }

    public String getUserPrincipal(HttpServletRequest requestPrincipal) {
        if (requestPrincipal.getUserPrincipal() != null) {
            return requestPrincipal.getUserPrincipal().toString();
        }
        return "User Principal information";
    }
}