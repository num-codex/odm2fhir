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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;

import org.hl7.fhir.r4.model.Bundle;

import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;

import static de.difuture.uds.odm2fhir.util.HTTPHelper.HTTP_CLIENT;

public abstract class FHIRBundleWriter {

  @Value("${fhir.errorhandling.strict:false}")
  protected boolean errorhandlingStrict;

  protected static final FhirContext FHIR_CONTEXT = FhirContext.forR4();
  protected static final IParser JSON_PARSER = FHIR_CONTEXT.newJsonParser().setPrettyPrint(true);

  public static final AtomicInteger RESOURCES_NUMBER = new AtomicInteger();
  public static final AtomicInteger BUNDLES_NUMBER = new AtomicInteger();

  public abstract void write(Bundle bundle) throws IOException;

  @PostConstruct
  private void init() {
    if (errorhandlingStrict) {
      FHIR_CONTEXT.setParserErrorHandler(new StrictErrorHandler());
    }

    FHIR_CONTEXT.getRestfulClientFactory().setHttpClient(HTTP_CLIENT);
  }

}