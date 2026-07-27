# AI-Generated Solutions Dataset

This directory contains the saved artifacts from a prompt-engineering experiment on Codeforces problems. It is organized first by Codeforces rating, then problem ID, then prompting strategy.

## Snapshot

- 180 problem directories
- Rated buckets from 1600 through 2400, plus `unrated`
- Four treatment labels: `NP`, `CoT`, `CoT-ADV`, and `PC`
- 2,199 experiment files excluding this README at the time of documentation: 905 text files, 844 JSON files, and 450 Java files

Counts describe the repository snapshot and do not imply that every problem has all four treatments or every artifact type.

## Directory structure

```text
ai-gen-solutions/
└── <rating>/
    └── <problem-id>/
        └── <prompt>/
            ├── <problem-id>.txt
            ├── <prompt>-<problem-id>.java
            ├── <prompt>-response.txt
            ├── <prompt>-interaction.json
            ├── PC_Convo.json                 # PC runs only, when present
            ├── PC_primary_full_history.json  # PC runs only, when present
            └── convo_history/                # PC per-round logs, when present
```

## File meanings

| File | Contents |
| --- | --- |
| `<problem-id>.txt` | Plain-text copy of the Codeforces problem statement used as model input |
| `*.java` | Java code extracted from a fenced `java` block in the response; it may be empty or incorrect |
| `*-response.txt` or `response.txt` | Raw final model response |
| `*-interaction.json` | Serialized request/response interaction data |
| `PC_Convo.json` | Prompt-chain conversation state |
| `PC_primary_full_history.json` | Full primary-model history for a prompt-chain run |
| `convo_history/*.json` | Primary and critic exchanges from individual chain iterations |
| `error.txt` | Error information for an incomplete run, where present |

The `unrated` bucket holds collected runs for which the dataset directory did not assign a numeric rating. The same problem ID can appear in both `unrated` and a numeric bucket.

## Using the data

To inspect one experimental unit, select a problem ID and compare the sibling prompt directories. The Java files are model outputs rather than editorial solutions; compile, test, and validate them before any use.

Verdicts and aggregate analysis are stored at the repository root in [`Data_With_Ratings.txt`](../Data_With_Ratings.txt) and [`data.xlsx`](../data.xlsx). The scripts that created and processed these artifacts are documented in the [main README](../README.md).

## Caveats

- Coverage is uneven: not every prompt run has every expected output file.
- Interaction JSON may include model-generated reasoning and request metadata. Review it before redistributing or using it as training data.
- Problem statements originate from Codeforces; their inclusion here does not grant additional rights.
- Outputs reflect a particular model configuration and run, and should not be treated as deterministic benchmark answers.
