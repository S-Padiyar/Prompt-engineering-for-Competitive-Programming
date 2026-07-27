# AI-Generated Solutions Dataset

This directory contains the saved artifacts from a prompt-engineering experiment on Codeforces problems. It is organized first by Codeforces rating, then problem ID, then prompting strategy.

## Snapshot

- 90 unique Codeforces problem IDs
- 180 problem-directory instances because every problem is duplicated between its numeric rating bucket and `unrated`
- Rated buckets from 1600 through 2400, plus `unrated`
- Four treatment labels: `NP`, `CoT`, `CoT-ADV`, and `PC`
- 2,199 experiment files excluding this README at the time of documentation: 905 text files, 844 JSON files, and 450 Java files

Each numeric rating bucket contains 10 unique problems. The `unrated` directory contains copies of all 90 problems, so it should not be added to the rated buckets when calculating the number of unique problems. Counts describe the repository snapshot and do not imply that every problem has all four treatments or every artifact type.

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

The `unrated` bucket duplicates the same 90 problem IDs found across the numeric rating buckets. Treat it as a separate set of experimental runs, not as 90 additional problems.

## Using the data

To inspect one experimental unit, select a problem ID and compare the sibling prompt directories. The Java files are model outputs rather than editorial solutions; compile, test, and validate them before any use.

Verdicts and aggregate analysis are stored at the repository root in [`Data_With_Ratings.txt`](../Data_With_Ratings.txt) and [`data.xlsx`](../data.xlsx). The scripts that created and processed these artifacts are documented in the [main README](../README.md).

## Caveats

- Coverage is uneven: not every prompt run has every expected output file.
- Interaction JSON may include model-generated reasoning and request metadata. Review it before redistributing or using it as training data.
- Problem statements originate from Codeforces; their inclusion here does not grant additional rights.
- Outputs reflect a particular model configuration and run, and should not be treated as deterministic benchmark answers.

## License

The repository owner's original research data, experimental metadata, analysis outputs, and documentation are licensed under [CC BY 4.0](../LICENSE-DATA.md). Source code is separately licensed under the [MIT License](../LICENSE).

These licenses do not relicense Codeforces problem statements, third-party trademarks, or other material the repository owner does not own. Generated content is covered only to the extent that the repository owner holds rights in it.
