import pytest

from cf_prompting.analysis import VerdictRecord, create_analysis_workbook


def test_workbook_handles_zero_discordance_and_missing_baseline(tmp_path):
    openpyxl = pytest.importorskip("openpyxl")
    pytest.importorskip("pandas")
    pytest.importorskip("statsmodels")

    output = tmp_path / "analysis.xlsx"
    create_analysis_workbook(
        [
            VerdictRecord("1A", "NP", "A", "1600"),
            VerdictRecord("1A", "CoT", "A", "1600"),
        ],
        output,
    )
    workbook = openpyxl.load_workbook(output, read_only=True, data_only=True)
    assert workbook.sheetnames == [
        "Raw Data",
        "Prompt Type Accuracy",
        "Solved Combinations",
        "McNemar Test",
    ]
    mcnemar_rows = list(workbook["McNemar Test"].iter_rows(values_only=True))
    assert mcnemar_rows[1][-2:] == (0, 1)

    no_baseline_output = tmp_path / "no-baseline.xlsx"
    create_analysis_workbook([VerdictRecord("1A", "CoT", "A")], no_baseline_output)
    no_baseline = openpyxl.load_workbook(no_baseline_output, read_only=True, data_only=True)
    assert list(no_baseline["McNemar Test"].iter_rows(values_only=True)) == [
        (
            "Prompt A",
            "Prompt B",
            "A solved only",
            "B solved only",
            "Test Statistic",
            "P-Value",
        )
    ]
