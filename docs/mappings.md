# ODM2FHIR Mappings

The following diagrams depict the mapping of study/patient data (items) in [CDISC ODM](https://www.cdisc.org/standards/data-exchange/odm) based on the [GECCO data dictionary](https://confluence.imi.med.fau.de/display/MIIC/30+EDC+System+REDCap) onto [HL7 FHIR](https://www.hl7.org/fhir) (resources) adhering to the [GECCO implementation guide](https://simplifier.net/ForschungsnetzCovid-19).

For static items the respective `ValueSet`, `CodeSystem` or `Extension` is shown along with the actually used value.

## Patient
![Patient](diagrams/svg/Patient.svg)

## Anamnesis

### Cardiovascular Diseases
![Cardiovascular Diseases](diagrams/svg/anamnesis/CardiovascularDiseases.svg)

### Chronic Kidney Diseases
![Chronic Kidney Diseases](diagrams/svg/anamnesis/ChronicKidneyDiseases.svg)

### Chronic Liver Diseases
![Chronic Liver Diseases](diagrams/svg/anamnesis/ChronicLiverDiseases.svg)

### Chronic Lung Diseases
![Chronic Lung Diseases](diagrams/svg/anamnesis/ChronicLungDiseases.svg)

### Chronic Neurological Mental Diseases
![Chronic Neurological Mental Diseases](diagrams/svg/anamnesis/ChronicNeurologicalMentalDiseases.svg)

### DNR Order
![DNR Order](diagrams/svg/anamnesis/DNROrder.svg)

### Diabetes Mellitus
![Diabetes Mellitus](diagrams/svg/anamnesis/DiabetesMellitus.svg)

### Gastrointestinal Ulcers
![Gastrointestinal Ulcers](diagrams/svg/anamnesis/GastrointestinalUlcers.svg)

### HIV Infection
![HIV Infection](diagrams/svg/anamnesis/HIVInfection.svg)

### History of Travel
![History of Travel](diagrams/svg/anamnesis/HistoryOfTravel.svg)

### Immunization Status
![Immunization Status](diagrams/svg/anamnesis/ImmunizationStatus.svg)

### Malignant Neoplastic Diseases
![Malignant Neoplastic Diseases](diagrams/svg/anamnesis/MalignantNeoplasticDiseases.svg)

### Organ Recipient
![Organ Recipient](diagrams/svg/anamnesis/OrganRecipient.svg)

### Respiratory Therapies
![Respiratory Therapies](diagrams/svg/anamnesis/RespiratoryTherapies.svg)

### Rheumatological Immunological Diseases
![Rheumatological Immunological Diseases](diagrams/svg/anamnesis/RheumatologicalImmunologicalDiseases.svg)

### Smoking Status
![Smoking Status](diagrams/svg/anamnesis/SmokingStatus.svg)

## Complications

### Complication Conditions
![Complication Conditions](diagrams/svg/complications/ComplicationConditions.svg)

## Demographics

### Age
![Age](diagrams/svg/demographics/Age.svg)

### Biological Sex
![Biological Sex](diagrams/svg/demographics/BiologicalSex.svg)

### Body Height
![Body Height](diagrams/svg/demographics/BodyHeight.svg)

### Body Weight
![Body Weight](diagrams/svg/demographics/BodyWeight.svg)

### Ethnic Group
![Ethnic Group](diagrams/svg/demographics/EthnicGroup.svg)

### Frailty Score
![Frailty Score](diagrams/svg/demographics/FrailtyScore.svg)

### Pregnancy Status
![Pregnancy Status](diagrams/svg/demographics/PregnancyStatus.svg)

## Epidemiological Factors

### Known Exposure
![Known Exposure](diagrams/svg/epidemiological_factors/KnownExposure.svg)

## Imaging

### Imaging Procedures
![Imaging Procedures](diagrams/svg/imaging/ImagingProcedures.svg)

## Laboratory Values

### Laboratory Values
![Laboratory Values](diagrams/svg/laboratory_values/LaboratoryValues.svg)

### SARS CoV2 Antibodies
![SARS CoV2 Antibodies](diagrams/svg/laboratory_values/SARSCoV2Antibodies.svg)

### SARS CoV2 RTPCR
![SARS CoV2 RTPCR](diagrams/svg/laboratory_values/SARSCoV2RTPCR.svg)

## Medication

### ACE Inhibitors
![ACE Inhibitors](diagrams/svg/medication/ACEInhibitors.svg)

### Anticoagulants
![Anticoagulants](diagrams/svg/medication/Anticoagulants.svg)

### COVID-19 Therapy
![COVID-19 Therapy](diagrams/svg/medication/COVID19Therapy.svg)

### Immunoglobulins
![Immunoglobulins](diagrams/svg/medication/Immunoglobulins.svg)

## Onset of Illness

### Stage at Diagnosis
![Stage at Diagnosis](diagrams/svg/onset_of_illness/StageAtDiagnosis.svg)

## Outcome at Discharge

### Follow-Up SwabResult
![Follow-Up SwabResult](diagrams/svg/outcome_at_discharge/FollowUpSwabResult.svg)

### Respiratoric Outcome
![Respiratoric Outcome](diagrams/svg/outcome_at_discharge/RespiratoricOutcome.svg)

### Type of Discharge
![Type of Discharge](diagrams/svg/outcome_at_discharge/TypeOfDischarge.svg)

## Study Enrollment

### Interventional Studies Participation
![Interventional Studies Participation](diagrams/svg/study_enrollment/InterventionalStudiesParticipation.svg)

### Study Enrollment COVID-19
![Study Enrollment COVID-19](diagrams/svg/study_enrollment/StudyEnrollmentCOVID19.svg)

## Symptoms

### Symptom Conditions
![Symptom Conditions](diagrams/svg/symptoms/SymptomConditions.svg)

## Therapy

### Apheresis
![Apheresis](diagrams/svg/therapy/Apheresis.svg)

### Dialysis Hemofiltration
![Dialysis Hemofiltration](diagrams/svg/therapy/DialysisHemofiltration.svg)

### ECMO
![ECMO](diagrams/svg/therapy/ECMO.svg)

### Patient in ICU
![Patient in ICU](diagrams/svg/therapy/PatientInICU.svg)

### Prone Position
![Prone Position](diagrams/svg/therapy/PronePosition.svg)

### Ventilation Type
![Ventilation Type](diagrams/svg/therapy/VentilationType.svg)

## Vital Signs

### Blood Pressure
![Blood Pressure](diagrams/svg/vital_signs/BloodPressure.svg)

### Body Temperature
![Body Temperature](diagrams/svg/vital_signs/BodyTemperature.svg)

### FiO2
![FiO2](diagrams/svg/vital_signs/FiO2.svg)

### Heart Rate
![Heart Rate](diagrams/svg/vital_signs/HeartRate.svg)

### PH Value
![PH Value](diagrams/svg/vital_signs/PHValue.svg)

### PaCO2
![PaCO2](diagrams/svg/vital_signs/PaCO2.svg)

### PaO2
![PaO2](diagrams/svg/vital_signs/PaO2.svg)

### Peripheral Oxygen Saturation
![Peripheral Oxygen Saturation](diagrams/svg/vital_signs/PeripheralOxygenSaturation.svg)

### Respiratory Rate
![Respiratory Rate](diagrams/svg/vital_signs/RespiratoryRate.svg)

### SOFA Score
![SOFA Score](diagrams/svg/vital_signs/SOFAScore.svg)