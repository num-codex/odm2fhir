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

import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.codesystems.ResourceTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static de.difuture.uds.odm2fhir.fhir.util.IdentifierHelper.getIdentifierSystem;

@ConditionalOnExpression("'${fhir.server.url:}'.empty and !'${kafka.broker.url:}'.empty")
@Service
@Slf4j
public class KafkaFHIRBundleWriter extends FHIRBundleWriter {

  @Value("${spring.kafka.template.default-topic:}")
  private String topicName;

  @Autowired
  private KafkaTemplate<String, Bundle> kafkaTemplate;

  /**
   * @param bundle the Bundle whose Patient resource should be retrieved.
   * @return the patient resource or null, if bundle was null or no patient id was found
   */
  protected Patient getPatientResourceFromBundle(Bundle bundle) {
    if (bundle == null) {
      log.warn("Received Bundle is null. Discarding.");
      return null;
    }
    var resource =
        bundle.getEntry().stream()
            .filter(
                bundleEntryComponent ->
                    bundleEntryComponent.getResource().getResourceType() == ResourceType.Patient)
            .findFirst();
    if (resource.isEmpty()) {
      log.warn(
          "Found GECCO FHIR Bundle without Patient resource: {}. Discarding.", bundle.getId());
      return null;
    }
    return (Patient) resource.get().getResource();
  }

  /**
   * @param patient the Patient resource whose id should be retrieved.
   * @return the patient id or null, if none was found
   */
  protected String getPidFromPatientResource(Patient patient) {
    var pid =
        patient.getIdentifier().stream()
            .filter(
                identifier -> identifier.getSystem().equals(getIdentifierSystem(ResourceTypes.PATIENT)))
            .findFirst();
    if (pid.isEmpty()) {
      log.warn("Did not find PID in Patient resource. Discarding.");
      return null;
    }
    return pid.get().getValue();
  }

  @Override
  public void write(Bundle bundle) {
    var patientResource = getPatientResourceFromBundle(bundle);
    if (patientResource == null) {
      return;
    }
    var pid = getPidFromPatientResource(patientResource);
    if (pid == null) {
      return;
    }
    this.kafkaTemplate.send(topicName, pid, bundle);

    BUNDLES_NUMBER.incrementAndGet();
    RESOURCES_NUMBER.addAndGet(bundle.getEntry().size());
  }

}
