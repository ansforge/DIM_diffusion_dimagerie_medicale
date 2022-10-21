/*
 *  DMPConnect.java - DRIMBox
 *
 * MIT License
 *
 * Copyright (c) 2022 b<>com
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.bcom.drimbox.dmp;

import com.bcom.drimbox.dmp.request.BaseRequest;
import com.bcom.drimbox.dmp.security.DMPKeyStore;
import io.quarkus.logging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * Class used to connect to the DMP and make requests
 */
@Singleton
public class DMPConnect {

    // Charset used for request
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private HttpsURLConnection urlConn = null;

    @Inject
    DMPKeyStore dmpKeyStore;

    /**
     * Connect to given host in TLS 1.2
     *
     * @param host Host to connect
     * @return true if success, false otherwise
     */
    private Boolean connect(String host) {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(dmpKeyStore.getTrustStore());

            TrustManager[] tms = tmf.getTrustManagers();

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(dmpKeyStore.getKeyManagers(), tms, new SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            URL url = new URL(host);

            urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setRequestProperty("Accept-Charset", String.valueOf(CHARSET));
            urlConn.setRequestProperty("Content-Type", "application/soap+xml");
            urlConn.setRequestProperty("Access-Control-Allow-Origin", "*");
            urlConn.connect();

            return true;
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        return false;
    }


    /**
     * Used to parse the response given by the DMP (string response)
     */
    public class DMPResponse {
        public int statusCode;
        public String message;
    }

    /**
     * Send a request to the DMP
     *
     * @param request Request
     * @return DMP response (string response)
     */
    public DMPResponse sendRequest(BaseRequest request) {
        // Try to connect
        if (!connect(request.getServiceURL())) {
            throw new RuntimeException("Can't connect to DMP.");
        }

        try (OutputStream output = urlConn.getOutputStream()) {

            DMPResponse response = new DMPResponse();
            String requestStr = request.getRequest();

            output.write(requestStr.getBytes(CHARSET));
            response.statusCode = urlConn.getResponseCode();

            BufferedReader in;

            if (response.statusCode == 500 ) {
                in = new BufferedReader(new InputStreamReader(urlConn.getErrorStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            }


            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            response.message = content.toString();

            if (response.statusCode == 500 ) {
                Log.error("DMP returned error 500 : " + response.message);
            }

            return response;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Used to parse the response given by the DMP (byte response)
     */
    public class DMPResponseBytes {
        public int statusCode;
        public byte[] rawMessage;
    }

    /***
     * Send a KOS request to the DMP. The response will not be parsed a string and will be it in raw bytes
     * @see #DMPResponseBytes
     *
     * @param request Request
     *
     * @return DMP response (byte response)
     */
    // Allow to retrieve byte as message directly for KOS request
    public DMPResponseBytes sendKOSRequest(BaseRequest request) {
        // Try to connect
        if (!connect(request.getServiceURL())) {
            throw new RuntimeException("Can't connect to DMP.");
        }

        try (OutputStream output = urlConn.getOutputStream()) {

            DMPResponseBytes response = new DMPResponseBytes();
            String requestStr = request.getRequest();

            output.write(requestStr.getBytes(CHARSET));
            response.statusCode = urlConn.getResponseCode();

            response.rawMessage = urlConn.getInputStream().readAllBytes();

            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
