package de.difuture.uds.odm2fhir.odm.processor;

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

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.util.HTTPHelper.HTTP_CLIENT;

import static org.apache.commons.io.IOUtils.readLines;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.function.Failable.asFunction;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.client.methods.RequestBuilder.post;
import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;

import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;

@ConditionalOnExpression("!'${odm.redcap.api.url:}'.empty")
@Service
@Slf4j
public class REDCapODMProcessor extends ODMProcessor {

  @Value("${odm.redcap.api.url}")
  private URL url;

  @Value("${odm.redcap.api.token}")
  private String token;

  @Value("${odm.redcap.api.patientspercall:1}")
  private int patientspercall;

  public Stream<InputStream> read() throws Exception {
    log.info("Reading ODM via REDCap API at '{}'", url);

    var counter = new AtomicInteger();

    return readPatientIDs().collect(groupingBy(patientID -> counter.getAndIncrement() / patientspercall))
                           .values()
                           .stream()
                           .map(asFunction(this::read));
  }

  private InputStream read(List<String> patientIDs) throws Exception {
    var dateRangeBegin = ofNullable(previousRunDateTime).map(LocalDateTime::toString).orElse(null);

    if (initialODMInRun && isNotBlank(dateRangeBegin)) {
      log.info("Getting data changed since {}", dateRangeBegin);
      initialODMInRun = false;
    }

    var httpPost = post(url.toURI()).addHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED.getMimeType())
                                    .addParameter("token", token)
                                    .addParameter("content", "record")
                                    .addParameter("format", "odm")
                                    .addParameter("exportCheckboxLabel", "false")
                                    .addParameter("records", join(",", patientIDs))
                                    .addParameter("dateRangeBegin", dateRangeBegin)
                                    .build();

    return HTTP_CLIENT.execute(httpPost).getEntity().getContent();
  }

  private Stream<String> readPatientIDs() throws Exception {
    var httpPost = post(url.toURI()).addHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED.getMimeType())
                                    .addParameter("token", token)
                                    .addParameter("content", "record")
                                    .addParameter("format", "csv")
                                    .addParameter("fields", "record_id")
                                    .addParameter("events", "basisdaten_arm_1,1_fall_arm_1,2_fall_arm_1,3_fall_arm_1")
                                    .build();

    return readLines(HTTP_CLIENT.execute(httpPost).getEntity().getContent(), UTF_8)
        .stream()
        .skip(1)
        .map(line -> substringBefore(line, ","))
        .distinct();
  }

}