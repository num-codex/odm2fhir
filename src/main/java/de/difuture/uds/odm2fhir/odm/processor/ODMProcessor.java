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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.UpperCamelCaseStrategy;

import de.difuture.uds.odm2fhir.fhir.mapper.Subject;
import de.difuture.uds.odm2fhir.fhir.writer.FHIRBundleWriter;
import de.difuture.uds.odm2fhir.fhir.writer.FHIRBundler;
import de.difuture.uds.odm2fhir.odm.model.SubjectData;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.dataformat.xml.XmlMapper.xmlBuilder;

import static de.difuture.uds.odm2fhir.fhir.writer.FHIRBundleWriter.BUNDLES_NUMBER;
import static de.difuture.uds.odm2fhir.fhir.writer.FHIRBundleWriter.RESOURCES_NUMBER;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.function.Failable.asConsumer;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isWritable;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;

@Service
@Slf4j
public abstract class ODMProcessor {

  @Autowired
  private FHIRBundler fhirBundler;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private FHIRBundleWriter fhirBundleWriter;

  @Value("${odm.cache.folder.path:}")
  private Path cacheFolderPath;

  private Map<String,Integer> subjectODMHashes;

  LocalDateTime previousRunDateTime;

  boolean initialODMInRun = true;

  protected abstract Stream<InputStream> read() throws Exception;

  public void process() throws Exception {
    ObjectMapper objectMapper = null;
    Path subjectODMHashesFile = null;
    Path previousRunDateTimeFile = null;

    if (cacheFolderPath == null || !isWritable(cacheFolderPath)) {
      log.info("'odm.cache.folder.path' not specified or not writable - filtering disabled");
    } else {
      createDirectories(cacheFolderPath);
      subjectODMHashesFile = cacheFolderPath.resolve("subject-odm-hashes");
      previousRunDateTimeFile = cacheFolderPath.resolve("previous-run-date-time");
      objectMapper = new ObjectMapper();
      subjectODMHashes = !exists(subjectODMHashesFile) ? new HashMap<>() :
                                  objectMapper.readValue(subjectODMHashesFile.toFile(), new TypeReference<HashMap<String,Integer>>() {});
      if (exists(previousRunDateTimeFile)) {
        previousRunDateTime = LocalDateTime.parse(readString(previousRunDateTimeFile));
      }
    }

    read().toList().forEach(asConsumer(this::process));

    if (subjectODMHashes != null) {
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(subjectODMHashesFile.toFile(), subjectODMHashes);
        writeString(previousRunDateTimeFile, previousRunDateTime.toString());
      }

    log.info("{} bundles with {} resources written", BUNDLES_NUMBER.getAndSet(0), RESOURCES_NUMBER.getAndSet(0));
  }

  private void process(InputStream inputStream) throws Exception {
    var xmlMapper = xmlBuilder().propertyNamingStrategy(new UpperCamelCaseStrategy())
                                .defaultUseWrapper(false)
                                .disable(FAIL_ON_UNKNOWN_PROPERTIES)
                                .build();

    var xmlStreamReader =  xmlMapper.getFactory().getXMLInputFactory().createXMLStreamReader(inputStream);

    while (xmlStreamReader.hasNext()) {
      xmlStreamReader.next();

      if (xmlStreamReader.isStartElement()) {
        switch (xmlStreamReader.getLocalName()) {
          case "ODM":
            if (subjectODMHashes != null && initialODMInRun) {
              var creationDateTime = xmlStreamReader.getAttributeValue(null, "CreationDateTime");
              try {
                previousRunDateTime = LocalDateTime.parse(creationDateTime);
              } catch (DateTimeParseException dateTimeParseException) {
                previousRunDateTime = ZonedDateTime.parse(creationDateTime).toLocalDateTime();
              }
            }
            break;
          case "SubjectData":
            var subjectData = xmlMapper.readValue(xmlStreamReader, SubjectData.class);

            var subjectKey = subjectData.getSubjectKey();

            if (isBlank(subjectKey)) {
              log.warn("Empty subject key for patient");
            } else {
              var hash = subjectODMHashes != null ? subjectData.hashCode() : null;

              if (hash == null || !hash.equals(subjectODMHashes.get(subjectKey))) {
                var bundle = fhirBundler.bundle(new Subject().map(subjectData));
                if (bundle.isEmpty()) {
                  log.warn("Empty bundle for patient '{}'", subjectKey);
                } else {
                  fhirBundleWriter.write(bundle);
                  if (subjectODMHashes != null) {
                    subjectODMHashes.put(subjectKey, hash);
                  }
                }
              }
            }
        }
      }
    }
  }

}