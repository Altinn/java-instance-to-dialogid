# Instance to Dialog ID

Small Java example for converting an Altinn `instance.id` UUID plus `instance.created` timestamp into the corresponding Dialogporten `dialog.id`.

## Files

- `InstanceToDialogId.java`: reusable conversion logic.
- `RunTests.java`: reads `testdata.json` and compares computed dialog IDs with expected values.
- `testdata.json`: Test-data to check against. Generated with the following command using [jq](https://jqlang.org/):

```bash
# instances.json is output from the storage API at /storage/api/v1/instances,
# see https://docs.altinn.studio/nb/api/storage/instances/

jq '.instances[] | {"instanceId": .id, created, "dialogId": .dataValues["dialog.id"] }' \
  instances.json > testdata.json
```


## Build

```bash
javac InstanceToDialogId.java RunTests.java
```

## Run

Run against `testdata.json`:

```bash
java -cp . RunTests
```

Run against a different file:

```bash
java -cp . RunTests some-other-testdata.json
```

The test runner reads `instanceId` and `created`, compares the computed value with `dialogId`, prints one line per mismatch, and ends with a summary line.
