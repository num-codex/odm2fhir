package de.difuture.uds.odm2fhir.fhir.mapper.anamnesis.item;

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

import de.difuture.uds.odm2fhir.fhir.mapper.Item;
import de.difuture.uds.odm2fhir.odm.model.FormData;
import de.difuture.uds.odm2fhir.odm.model.ItemData;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.LOINC;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.HISTORY_OF_TRAVEL;

import static org.hl7.fhir.r4.model.Observation.ObservationStatus.FINAL;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.OBSERVATION;

import static java.util.Locale.ENGLISH;
import static java.util.function.Predicate.not;

public class HistoryOfTravel extends Item {

  private static final Map<String, String> STATES;

  static {
    STATES = new HashMap<>();

    STATES.put("DE-BW", "Baden-Württemberg");
    STATES.put("DE-BY", "Bayern");
    STATES.put("DE-BE", "Berlin");
    STATES.put("DE-BB", "Brandenburg");
    STATES.put("DE-HB", "Bremen");
    STATES.put("DE-HH", "Hamburg");
    STATES.put("DE-HE", "Hessen");
    STATES.put("DE-MV", "Mecklenburg-Vorpommern");
    STATES.put("DE-NI", "Niedersachsen");
    STATES.put("DE-NW", "Nordrhein-Westfalen");
    STATES.put("DE-RP", "Rheinland-Pfalz");
    STATES.put("DE-SL", "Saarland");
    STATES.put("DE-SN", "Sachsen");
    STATES.put("DE-ST", "Sachsen-Anhalt");
    STATES.put("DE-SH", "Schleswig-Holstein");
    STATES.put("DE-TH", "Thüringen");
  }

  public Stream<DomainResource> map(FormData formData) {
    var answerCoding = formData.getItemData("reiseaktivitat");
    var generalCoding = formData.getItemData("reiseaktivitat_code");

    return generalCoding.isEmpty() ? Stream.empty() :
        IntStream.rangeClosed(1, 10)
            .mapToObj(i -> Map.of(
                "start", formData.getItemData("reiseaktivitat_start_date_" + i),
                "end", formData.getItemData("reiseaktivitat_end_date_" + i),
                "country", formData.getItemData("reiseaktivitat_country_" + i),
                "state", formData.getItemData("reiseaktivitat_state_" + i),
                "city", formData.getItemData("reiseaktivitat_city_" + i)))
            .filter(map -> map.values()
                .stream()
                .anyMatch(not(ItemData::isEmpty)))
            .map(travelActivity -> createObservation(generalCoding, answerCoding, travelActivity));
  }

  private Observation createObservation(ItemData generalCoding, ItemData answerCoding, Map<String, ItemData> travelActivity) {
    var observation = (Observation) new Observation()
        .addIdentifier(createIdentifier(OBSERVATION, travelActivity.get("country")).setType(OBI).setAssigner(getOrganizationReference()))
        .setStatus(FINAL)
        .setEffective(UNKNOWN_DATE_TIME) // TODO Set actual DateTime value
        .addCategory(SOCIAL_HISTORY)
        .setMeta(createMeta(HISTORY_OF_TRAVEL));

    var codeCodeableConcept = createCodeableConcept(generalCoding).setText("History of Travel");
    if (!codeCodeableConcept.isEmpty()) {
      observation.setCode(codeCodeableConcept);
    }

    var valueCodeableConcept = createCodeableConcept(answerCoding);
    if (!valueCodeableConcept.isEmpty()) {
      observation.setValue(valueCodeableConcept);
    }

    var startDate = travelActivity.get("start");
    if (!startDate.isEmpty()) {
      observation.addComponent(new ObservationComponentComponent()
          .setCode(createCodeableConcept(createCoding(LOINC, "82752-7", "Date travel started"))
              .setText("Travel start date"))
          .setValue(createDateTimeType(startDate)));
    }

    var endDate = travelActivity.get("end");
    if (!endDate.isEmpty()) {
      observation.addComponent(new ObservationComponentComponent()
          .setCode(createCodeableConcept(createCoding(LOINC, "91560-3", "Date of departure from travel destination"))
              .setText("Travel end date"))
          .setValue(createDateTimeType(endDate)));
    }

    var country = travelActivity.get("country");
    if (!country.isEmpty()) {
      var countryCoding = createCoding(country);
      countryCoding.setDisplay(new Locale("", countryCoding.getCode()).getDisplayCountry(ENGLISH));
      observation.addComponent(new ObservationComponentComponent()
          .setCode(createCodeableConcept(createCoding(LOINC, "94651-7", "Country of travel"))
              .setText("Country of travel"))
          .setValue(createCodeableConcept(countryCoding).setText(countryCoding.getDisplay())));
    }

    var state = travelActivity.get("state");
    if (!state.isEmpty()) {
      var stateCoding = createCoding(state);
      stateCoding.setDisplay(STATES.getOrDefault(stateCoding.getCode(), stateCoding.getCode()));
      observation.addComponent(new ObservationComponentComponent()
          .setCode(createCodeableConcept(createCoding(LOINC, "82754-3", "State of travel"))
              .setText("State of travel"))
          .setValue(createCodeableConcept(stateCoding).setText(stateCoding.getDisplay())));
    }

    var city = travelActivity.get("city");
    if (!city.isEmpty()) {
      observation.addComponent(new ObservationComponentComponent()
          .setCode(createCodeableConcept(createCoding(LOINC, "94653-3", "City of travel"))
              .setText("City of travel"))
          .setValue(createStringType(city)));
    }

    //no Components added = no travel activity
    return observation.getComponent().isEmpty() ? new Observation() : observation;
  }

}