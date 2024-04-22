package com.mokhir.dev.ATM.service.network;

import jakarta.servlet.http.HttpServletRequest;

import java.net.SocketException;

/**
 * @project: middleware-service
 * @Date: 05.09.2022
 * @author: H_Urunov
 **/

public interface NetworkServices {

    String getClientIPv4Address(HttpServletRequest request);

    String getClientMACAddress(String IPAddress) throws SocketException;

    String getClientHostAddress(HttpServletRequest httpServletRequest);
}
