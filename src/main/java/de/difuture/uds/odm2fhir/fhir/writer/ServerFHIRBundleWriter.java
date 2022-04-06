package de.difuture.uds.odm2fhir.fhir.writer;

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

import ca.uhn.fhir.rest.client.api.IGenericClient;

import lombok.extern.slf4j.Slf4j;

import org.hl7.fhir.r4.model.Bundle;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;

import static de.difuture.uds.odm2fhir.util.HTTPHelper.createAuthInterceptor;

import static ca.uhn.fhir.context.FhirContext.forR4Cached;

@ConditionalOnExpression("!'${fhir.server.url:}'.empty")
@Service
@Slf4j
public class ServerFHIRBundleWriter extends FHIRBundleWriter {

  @Value("${fhir.server.url:}")
  private URI url;

  @Value("${fhir.server.basicauth.username:}")
  private String basicauthUsername;

  @Value("${fhir.server.basicauth.password:}")
  private String basicauthPassword;

  @Value("${fhir.server.oauth2.token.url:}")
  private String oauth2TokenURL;

  @Value("${fhir.server.oauth2.client.id:}")
  private String oauth2ClientId;

  @Value("${fhir.server.oauth2.client.secret:}")
  private String oauth2ClientSecret;

  private IGenericClient genericClient;
  private RetryTemplate retryTemplate;

  private void init() throws IOException {
    genericClient = forR4Cached().getRestfulClientFactory().newGenericClient(url.toString());
    genericClient.registerInterceptor(
        createAuthInterceptor(basicauthUsername, basicauthPassword, oauth2TokenURL, oauth2ClientId, oauth2ClientSecret));

    var backOffPolicy = new FixedBackOffPolicy();
    backOffPolicy.setBackOffPeriod(10000L);

    retryTemplate = new RetryTemplate();
    retryTemplate.setBackOffPolicy(backOffPolicy);
    retryTemplate.setRetryPolicy(new SimpleRetryPolicy(3));
    retryTemplate.registerListener(new RetryListenerSupport() {
      @Override
      public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        log.error("Attempt {} to send bundle failed with error", context.getRetryCount(), throwable);
      }
    });
  }

  @Override
  public void write(Bundle bundle) throws IOException {
    if (genericClient == null) {
      init();
    }

    retryTemplate.execute(context -> genericClient.transaction().withBundle(bundle).execute());
    BUNDLES_NUMBER.incrementAndGet();
    RESOURCES_NUMBER.addAndGet(bundle.getEntry().size());
  }

}