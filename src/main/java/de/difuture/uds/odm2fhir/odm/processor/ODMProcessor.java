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

import static org.apache.commons.lang3.function.Failable.asConsumer;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public abstract class ODMProcessor {

  @Autowired
  private FHIRBundler fhirBundler;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private FHIRBundleWriter fhirBundleWriter;

  @Value("${odm.filtering.enabled:false}")
  private boolean filteringEnabled;

  @Value("${odm.filtering.hashes.folder.path:}")
  private Path filteringHashesFolderPath;

  private Map<String,Integer> filteringHashes;

  LocalDateTime previousRunDateTime;

  boolean initialODMInRun = true;

  abstract Stream<InputStream> read() throws Exception;

  @SuppressWarnings("unchecked")
  public void process() throws Exception {
    ObjectMapper objectMapper = null;
    Path filteringHashesFile = null;

    if (filteringEnabled) {
      if (filteringHashesFolderPath == null) {
        throw new IllegalArgumentException("'odm.filtering.hashes.folder.path' not specified");
      }
      createDirectories(filteringHashesFolderPath);
      filteringHashesFile = filteringHashesFolderPath.resolve("hashes.json");
      objectMapper = new ObjectMapper();
      filteringHashes = !exists(filteringHashesFile) ? new HashMap<>() :
                        objectMapper.readValue(filteringHashesFile.toFile(), new TypeReference<HashMap<String,Integer>>() {});
    }

    read().collect(toList()).forEach(asConsumer(this::process));

    if (filteringEnabled) {
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(filteringHashesFile.toFile(), filteringHashes);
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
            if (initialODMInRun) {
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

            var hash = filteringEnabled ? subjectData.hashCode() : null;

            if (hash == null || !hash.equals(filteringHashes.get(subjectData.getSubjectKey()))) {
              fhirBundleWriter.write(fhirBundler.bundle(new Subject().map(subjectData)));

              if (filteringEnabled) {
                filteringHashes.put(subjectData.getSubjectKey(), hash);
              }
            }
        }
      }
    }
  }

}