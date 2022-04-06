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
import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.util.HTTPHelper.HTTP_CLIENT_BUILDER;

import static org.apache.http.client.methods.RequestBuilder.get;

@ConditionalOnExpression("!'${odm.dis.rest.url:}'.empty")
@Service
@Slf4j
public class DISODMProcessor extends ODMProcessor {

  @Value("${odm.dis.rest.url}")
  private URL url;

  @Value("${odm.dis.rest.studyname}")
  private String studyname;

  @Value("${odm.dis.rest.username}")
  private String username;

  @Value("${odm.dis.rest.password}")
  private String password;

  public Stream<InputStream> read() throws Exception {
    log.info("Reading ODM via DIS REST at '{}'", url);

    var httpGet = get(url.toURI()).addParameter("studyname", studyname)
                                  .addParameter("username", username)
                                  .addParameter("password", password)
                                  .addParameter("exportformat", "odm")
                                  .addParameter("sasname", "true")
                                  .build();

    return Stream.of(HTTP_CLIENT_BUILDER.build().execute(httpGet).getEntity().getContent());
  }

}