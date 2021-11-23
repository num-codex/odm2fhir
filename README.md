# ODM2FHIR

This tool maps study/patient data in CDISC ODM based on the [GECCO data dictionary](https://confluence.imi.med.fau.de/display/MIIC/30+EDC+System+REDCap) to HL7 FHIR which adheres to the [GECCO profiles, value sets and code systems](https://simplifier.net/ForschungsnetzCovid-19).

Details about the actual mapping can be found [here](docs/mappings.md).

## Command
```sh
docker run **VOLUMES** ghcr.io/num-codex/odm2fhir **ARGUMENTS**
```

### Arguments

(Note: Replace all `**VARIABLE**` references with their actual values.)

* `--help` Print `README`.

* `--odm.redcap.datadictionary` Print current data dictionary.

* `--odm.redcap.mapping` Print current mapping.

* `--fhir.identifier.system.**TYPE**=**TYPE_IDENTIFIER_SYSTEM**` Add an identifier system with `**TYPE**` of `condition`, `consent`, `diagnosticreport`, `encounter`, `immunization`, `medicationstatement`, `observation`, `patient` or `procedure` (see [here](https://simplifier.net/guide/GermanCoronaConsensusDataSet-ImplementationGuide/TransactionBundle)).

* `--fhir.identifier.assigner=**IDENTIFIER_ASSIGNER**` Add an identifier assigner.

* `--fhir.updateascreate.enabled=true` Enable update-as-create (see [here](https://www.hl7.org/fhir/http.html#upsert)).

* `--fhir.validation.enabled=true` Enable FHIR resource validation (see [here](#validation)).

* `--cron="**CRON_PATTERN**"` Enable timed execution (see [here](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html)).

## Input

***Either*** enable *local* input by adding the volume for the [local file](#local-file) to `**VOLUMES**` ***or*** enable *remote* input by adding the arguments for either [REDCap](#redcap) or [DIS](#dis) to `**ARGUMENTS**`.

### Local File
```sh
-v **ODM_FILE_PATH**:/workspace/input/ODM.xml
```

### REDCap
```sh
--odm.redcap.api.url=**ODM_REDCAP_API_URL**
--odm.redcap.api.token=**ODM_REDCAP_API_TOKEN**
```

#### PKCS12 Certificate
```sh
--odm.redcap.api.key.file.path=**ODM_REDCAP_API_KEY_FILE_PATH**
--odm.redcap.api.key.password=**ODM_REDCAP_API_KEY_PASSWORD**
```

#### Export Chunking
The ODM export from REDCap is by default divided into exports for data of single patients to avoid connection issues and timeouts but can be changed by adding the argument:
```sh
--odm.redcap.api.patientspercall=**PATIENTS_PER_CALL**
```

#### Filtering
By default, all subjects within an ODM are processed. To process only subjects with changed values since the last run add a volume with `**CACHE_FOLDER**` pointing to a local folder:
```sh
-v **CACHE_FOLDER**:/workspace/cache
```
(Note: To reset the filtering, empty the folder `**CACHE_FOLDER**`.)

### DIS
```sh
--odm.dis.rest.url=**ODM_DIS_REST_URL**
--odm.dis.rest.studyname=**ODM_DIS_REST_STUDYNAME**
--odm.dis.rest.username=**ODM_DIS_REST_USERNAME**
--odm.dis.rest.password=**ODM_DIS_REST_PASSWORD**
```

#### PKCS12 Certificate
```sh
--odm.dis.rest.key.file.path=**ODM_DIS_REST_KEY_FILE_PATH**
--odm.dis.rest.key.password=**ODM_DIS_REST_KEY_PASSWORD**
```

## Output

There are three alternatives for the output of the FHIR Bundles:

1. Output to local file system
1. Ouput to a FHIR API (e.g. FHIR-Server)
1. Ouput to a Kafka Topic

> **_NOTE:_**: There is currently only one output method supported at a time.

### 1. Output to Local File System

To enable output to a directory in the local file system, mount a volume into the container:

```sh
-v **FHIR_BUNDLES_FOLDER_PATH**:/workspace/output
```

### 2. Output to FHIR API

To enable ouput to a FHIR API add the following argument:

```sh
--fhir.server.url=**FHIR_SERVER_URL**
```

If you need to use HTTP Basic Auth, OAuth2 or PKCS12 for the communication with the FHIR-Server, add the following arguments:

#### BasicAuth

```sh
--fhir.server.basicauth.username=**FHIR_SERVER_BASICAUTH_USERNAME**
--fhir.server.basicauth.password=**FHIR_SERVER_BASICAUTH_PASSWORD**
```

#### OAuth2 (Client Credentials)

```sh
--fhir.server.oauth2.token.url=**FHIR_SERVER_OAUTH2_TOKEN_URL**
--fhir.server.oauth2.client.id=**FHIR_SERVER_OAUTH2_CLIENT_ID**
--fhir.server.oauth2.client.secret=**FHIR_SERVER_OAUTH2_CLIENT_SECRET**
```

#### PKCS12 Certificate

```sh
--fhir.server.key.file.path=**FHIR_SERVER_KEY_FILE_PATH**
--fhir.server.key.password=**FHIR_SERVER_KEY_PASSWORD**
```

### 3. Output to Kafka Topic

To enable output to a Kafka Topic, the following environment variables can be set:

| Environment Variable | Description                                                |
|----------------------|------------------------------------------------------------|
| KAFKA_BROKER_URL     | URL of the Kafka Broker                                    |
| KAFKA_OUTPUT_TOPIC   | Name of the target Kafka Topic (default: `fhir.odm-gecco`) |

## Validation

Enable validation of the generated FHIR resources - and filtering out of all invalid ones - according to mentioned profiles by adding the argument `--fhir.validation.enabled=true`.

Enable the use of an external terminology server by adding the argument(s) below - together with BasicAuth or OAuth2 (Client Credentials), if applicable.

### FHIR Terminology Server

```sh
--fhir.terminologyserver.url=**FHIR_TERMINOLOGYSERVER_URL**
```

#### BasicAuth

```sh
--fhir.terminologyserver.basicauth.username=**FHIR_TERMINOLOGYSERVER_BASICAUTH_USERNAME**
--fhir.terminologyserver.basicauth.password=**FHIR_TERMINOLOGYSERVER_BASICAUTH_PASSWORD**
```

#### OAuth2 (Client Credentials)

```sh
--fhir.terminologyserver.oauth2.token.url=**FHIR_TERMINOLOGYSERVER_OAUTH2_TOKEN_URL**
--fhir.terminologyserver.oauth2.client.id=**FHIR_TERMINOLOGYSERVER_OAUTH2_CLIENT_ID**
--fhir.terminologyserver.oauth2.client.secret=**FHIR_TERMINOLOGYSERVER_OAUTH2_CLIENT_SECRET**
```

#### PKCS12 Certificate

```sh
--fhir.terminologyserver.key.file.path=**FHIR_TERMINOLOGYSERVER_KEY_FILE_PATH**
--fhir.terminologyserver.key.password=**FHIR_TERMINOLOGYSERVER_KEY_PASSWORD**
```

## License

Copyright &copy; 2021 DIFUTURE (https://difuture.de)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.