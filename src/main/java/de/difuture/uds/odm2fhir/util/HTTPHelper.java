package de.difuture.uds.odm2fhir.util;

/*
 * Copyright (C) 2021 DIFUTURE (https://difuture.de)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.apache.commons.lang3.function.Failable.asConsumer;

@Service
public class HTTPHelper {

  private HTTPHelper() {}

  public static HttpClientBuilder HTTP_CLIENT_BUILDER;

  @Autowired
  public void setHTTPClientBuilder(Environment environment) throws Exception {
    var sslContextBuilder = SSLContextBuilder.create()
                                             .loadTrustMaterial(null, (certificate, authType) -> true);

    List.of("odm.redcap.api", "odm.dis.rest", "fhir.server", "fhir.terminologyserver")
        .forEach(asConsumer(entry -> {
          var keyFile = environment.getProperty(entry + ".key.file.path", File.class);
          var keyPassword = environment.getProperty(entry + ".key.password", "").toCharArray();
          if (keyFile != null) {
            sslContextBuilder.loadKeyMaterial(keyFile, keyPassword, keyPassword);
          }
        }));

    HTTP_CLIENT_BUILDER = HttpClientBuilder.create()
                                           .useSystemProperties()
                                           .setSSLContext(sslContextBuilder.build())
                                           .setSSLHostnameVerifier(new NoopHostnameVerifier());
  }

  public static IClientInterceptor createAuthInterceptor(String basicauthUsername, String basicauthPassword,
                                                         String oauth2TokenURL, String oauth2ClientId, String oauth2ClientSecret)
                                                        throws IOException {
    IClientInterceptor clientInterceptor = new BasicAuthInterceptor(basicauthUsername, basicauthPassword);

    if (isNoneBlank(oauth2TokenURL, oauth2ClientId, oauth2ClientSecret)) {
      var httpPost = RequestBuilder.post(oauth2TokenURL)
                                   .addParameter("grant_type", "client_credentials")
                                   .addParameter("client_id", oauth2ClientId)
                                   .addParameter("client_secret", oauth2ClientSecret)
                                   .build();

      var content = HTTP_CLIENT_BUILDER.build().execute(httpPost).getEntity().getContent();

      var token = new ObjectMapper().readTree(content).get("access_token").textValue();

      clientInterceptor = new BearerTokenAuthInterceptor(token);
    }

    return clientInterceptor;
  }

}