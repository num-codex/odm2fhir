package de.difuture.uds.odm2fhir.odm.reader;

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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.UpperCamelCaseStrategy;

import de.difuture.uds.odm2fhir.odm.model.ODM;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.dataformat.xml.XmlMapper.xmlBuilder;

import static de.difuture.uds.odm2fhir.util.HTTPHelper.HTTP_CLIENT;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.client.methods.RequestBuilder.get;
import static org.apache.http.client.methods.RequestBuilder.post;
import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.newInputStream;

@Service
@Slf4j
public class ODMReader {

  @Value("${odm.file.path:}")
  private Path filePath;

  @Value("${odm.redcap.api.url:}")
  private URL redcapAPIURL;

  @Value("${odm.redcap.api.token:}")
  private String redcapAPIToken;

  @Value("${odm.dis.rest.url:}")
  private URL disRESTURL;

  @Value("${odm.dis.rest.studyname:}")
  private String disRESTStudyname;

  @Value("${odm.dis.rest.username:}")
  private String disRESTUsername;

  @Value("${odm.dis.rest.password:}")
  private String disRESTPassword;

  public ODM read() throws Exception {
    InputStream inputStream;

    if (filePath != null && exists(filePath)) {
      log.info("Reading ODM from file '{}'", filePath);
      inputStream = newInputStream(filePath);
    } else if (redcapAPIURL != null) {
      log.info("Reading ODM via REDCap API at '{}'", redcapAPIURL);
      inputStream = readFromREDCapAPI(redcapAPIURL, redcapAPIToken);
    } else if (disRESTURL != null) {
      log.info("Reading ODM via DIS REST at '{}'", disRESTURL);
      inputStream = readFromDISREST(disRESTURL, disRESTStudyname, disRESTUsername, disRESTPassword);
    } else {
      throw new IllegalArgumentException("Neither (existing) 'odm.file.path' nor " +
          "'odm.redcap.api.url' and 'odm.redcap.api.token' or " +
          "'odm.dis.rest.url', 'odm.dis.rest.studyname', 'odm.dis.rest.username' and 'odm.dis.rest.password' specified");
    }

    var odm = new ODM();

    try {
      odm = xmlBuilder().propertyNamingStrategy(new UpperCamelCaseStrategy())
                        .defaultUseWrapper(false)
                        .disable(FAIL_ON_UNKNOWN_PROPERTIES)
                        .build()
                        .readValue(inputStream, ODM.class);
    } catch (JsonParseException jsonParseException) {
      // Do nothing...
    }

    if (odm.isEmpty()) {
      throw new IllegalArgumentException("Input is not in valid ODM format");
    }

    return odm;
  }

  private InputStream readFromREDCapAPI(URL url, String token) throws Exception {
    var httpPost = post(url.toURI())
        .addHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED.getMimeType())
        .addParameter("token", token)
        .addParameter("content", "record")
        .addParameter("format", "odm")
        .addParameter("type", "flat")
        .addParameter("csvDelimiter", "")
        .addParameter("rawOrLabel", "raw")
        .addParameter("rawOrLabelHeaders", "raw")
        .addParameter("exportCheckboxLabel", "false")
        .addParameter("exportSurveyFields", "false")
        .addParameter("exportDataAccessGroups", "false")
        .build();

    return HTTP_CLIENT.execute(httpPost).getEntity().getContent();
  }

  private InputStream readFromDISREST(URL url, String studyname, String username, String password) throws Exception {
    var httpGet = get(url.toURI())
        .addParameter("studyname", studyname)
        .addParameter("username", username)
        .addParameter("password", password)
        .addParameter("exportformat", "odm")
        .addParameter("sasname", "true")
        .build();

    return HTTP_CLIENT.execute(httpGet).getEntity().getContent();
  }

}