"""Verdict parsing and workbook generation for the experiment results."""

from __future__ import annotations

import csv
from collections.abc import Iterable, Sequence
from dataclasses import dataclass
from pathlib import Path
from typing import Any, List, Tuple, Union

ACCEPTED_VERDICT = "A"
IGNORED_VERDICTS = {"", "**"}
COMPARISON_PROMPTS = ("CoT", "CoT-ADV", "PC")


@dataclass(frozen=True)
class VerdictRecord:
    problem_id: str
    prompt_type: str
    verdict: str
    rating: str = ""

    @property
    def is_valid(self) -> bool:
        return self.verdict not in IGNORED_VERDICTS

    @property
    def solved(self) -> bool:
        return self.is_valid and self.verdict == ACCEPTED_VERDICT


def parse_verdict_rows(lines: Iterable[str]) -> Tuple[List[VerdictRecord], int]:
    """Parse three- or four-column CSV rows and count malformed input rows."""
    records: List[VerdictRecord] = []
    malformed_count = 0
    for row in csv.reader(lines, skipinitialspace=True):
        if not row or all(not value.strip() for value in row):
            continue
        if len(row) not in (3, 4):
            malformed_count += 1
            continue
        problem_id, prompt_type, verdict = (value.strip() for value in row[:3])
        rating = row[3].strip() if len(row) == 4 else ""
        if not problem_id or not prompt_type:
            malformed_count += 1
            continue
        records.append(VerdictRecord(problem_id, prompt_type, verdict, rating))
    return records, malformed_count


def read_verdict_file(path: Union[str, Path]) -> Tuple[List[VerdictRecord], int]:
    with Path(path).open("r", encoding="utf-8", newline="") as handle:
        return parse_verdict_rows(handle)


def _mcnemar_rows(solved_matrix: Any, mcnemar: Any) -> List[List[object]]:
    columns = list(solved_matrix.columns)
    if "NP" not in columns:
        return []

    results: List[List[object]] = []
    for prompt in COMPARISON_PROMPTS:
        if prompt not in columns:
            continue
        baseline_only = int((solved_matrix["NP"] & ~solved_matrix[prompt]).sum())
        prompt_only = int((solved_matrix[prompt] & ~solved_matrix["NP"]).sum())
        if baseline_only >= prompt_only:
            prompt_a, prompt_b = "NP", prompt
            a_wins, b_wins = baseline_only, prompt_only
        else:
            prompt_a, prompt_b = prompt, "NP"
            a_wins, b_wins = prompt_only, baseline_only

        if a_wins + b_wins == 0:
            statistic, p_value = 0.0, 1.0
        else:
            test_result = mcnemar([[0, b_wins], [a_wins, 0]], exact=False, correction=False)
            statistic = float(test_result.statistic)
            p_value = float(test_result.pvalue) / 2
        results.append([prompt_a, prompt_b, a_wins, b_wins, round(statistic, 4), round(p_value, 4)])
    return results


def create_analysis_workbook(
    records: Sequence[VerdictRecord], output_path: Union[str, Path]
) -> None:
    """Create the historical four-sheet Excel analysis workbook."""
    try:
        import pandas as pd
        from statsmodels.stats.contingency_tables import mcnemar
    except ImportError as exc:
        raise RuntimeError(
            "Workbook generation requires pandas, statsmodels, and openpyxl."
        ) from exc

    raw_rows = [
        {
            "Problem ID": record.problem_id,
            "Prompt Type": record.prompt_type,
            "Verdict": record.verdict,
            "Correct": "✅" if record.solved else "❌" if record.is_valid else "❌ (Ignored)",
            "Counts for Accuracy": record.is_valid,
            "Rating": record.rating,
        }
        for record in records
    ]
    dataframe = pd.DataFrame(
        raw_rows,
        columns=[
            "Problem ID",
            "Prompt Type",
            "Verdict",
            "Correct",
            "Counts for Accuracy",
            "Rating",
        ],
    )
    valid = dataframe[dataframe["Counts for Accuracy"]]

    if valid.empty:
        accuracy = pd.DataFrame(columns=["Prompt Type", "Accuracy (%)"])
        solved_matrix = pd.DataFrame()
    else:
        accuracy = (
            valid.assign(Solved=valid["Verdict"].eq(ACCEPTED_VERDICT))
            .groupby("Prompt Type", as_index=False)["Solved"]
            .mean()
            .rename(columns={"Solved": "Accuracy (%)"})
        )
        accuracy["Accuracy (%)"] = (accuracy["Accuracy (%)"] * 100).round(2)
        aggregated = (
            valid.assign(Solved=valid["Verdict"].eq(ACCEPTED_VERDICT))
            .groupby(["Problem ID", "Prompt Type"], as_index=False)["Solved"]
            .any()
        )
        solved_matrix = (
            aggregated.pivot(index="Problem ID", columns="Prompt Type", values="Solved")
            .fillna(False)
            .astype(bool)
        )

    prompt_types = list(solved_matrix.columns)
    if prompt_types:
        patterns = solved_matrix.apply(
            lambda row: "".join(prompt if row[prompt] else "-" for prompt in prompt_types), axis=1
        )
        combinations = (
            patterns.value_counts()
            .rename_axis("Solved Combination")
            .reset_index(name="Problem Count")
        )
    else:
        combinations = pd.DataFrame(columns=["Solved Combination", "Problem Count"])

    mcnemar_results = pd.DataFrame(
        _mcnemar_rows(solved_matrix, mcnemar),
        columns=[
            "Prompt A",
            "Prompt B",
            "A solved only",
            "B solved only",
            "Test Statistic",
            "P-Value",
        ],
    )
    with pd.ExcelWriter(output_path, engine="openpyxl") as writer:
        dataframe.drop(columns=["Counts for Accuracy"]).to_excel(
            writer, index=False, sheet_name="Raw Data"
        )
        accuracy.to_excel(writer, index=False, sheet_name="Prompt Type Accuracy")
        combinations.to_excel(writer, index=False, sheet_name="Solved Combinations")
        mcnemar_results.to_excel(writer, index=False, sheet_name="McNemar Test")
