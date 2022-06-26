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

import ca.uhn.fhir.parser.IParser;

import lombok.extern.slf4j.Slf4j;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

import static ca.uhn.fhir.context.FhirContext.forR4Cached;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.writeString;

@ConditionalOnExpression("'${fhir.server.url:}'.empty")
@Service
@Slf4j
public class FileFHIRBundleWriter implements FHIRBundleWriter {

  @Value("${fhir.folder.path:}")
  private Path folderPath;

  private static final IParser JSON_PARSER = forR4Cached().newJsonParser().setPrettyPrint(true);

  @Override
  public void write(Bundle bundle) throws IOException {
    if (folderPath == null) {
      throw new IllegalArgumentException("'fhir.folder.path' not specified");
    }

    createDirectories(folderPath);

    BUNDLES_NUMBER.incrementAndGet();
    RESOURCES_NUMBER.addAndGet(bundle.getEntry().size());

    var json = JSON_PARSER.encodeResourceToString(bundle);
    var name = ((Patient) bundle.getEntryFirstRep().getResource()).getIdentifierFirstRep().getValue();
    writeString(folderPath.resolve(name + ".json"), json);
  }

}