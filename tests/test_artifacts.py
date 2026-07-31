from cf_prompting.artifacts import (
    atomic_write_json,
    extract_java_code,
    find_java_file,
    find_unparseable_java,
    load_status_map,
    prompt_output_is_complete,
    serialize_status_map,
)


def test_extract_java_code_supports_case_and_multiple_blocks():
    response = "before```Java\nclass A {}\n```middle``` java\nclass B {}\n```after"
    assert extract_java_code(response) == "class A {}\n\nclass B {}"
    assert extract_java_code(None) == ""


def test_status_round_trip_and_completion_check(tmp_path):
    status = {(1600, "1A"): {"NP": {"java_code_exists": True}}}
    status_path = tmp_path / "status.json"
    atomic_write_json(status_path, serialize_status_map(status))
    assert load_status_map(status_path) == status
    assert not list(tmp_path.glob("*.tmp"))

    output = tmp_path / "NP"
    output.mkdir()
    for name in ("NP-1A.java", "NP-response.txt", "NP-interaction.json"):
        (output / name).write_text("content", encoding="utf-8")
    assert prompt_output_is_complete(output, "NP", "1A")
    (output / "NP-1A.java").write_text("", encoding="utf-8")
    assert not prompt_output_is_complete(output, "NP", "1A")


def test_historical_underscore_java_name_and_unparseable_detection(tmp_path):
    pc_dir = tmp_path / "1600" / "1A" / "PC"
    pc_dir.mkdir(parents=True)
    historical = pc_dir / "PC_1A.java"
    historical.write_text("class Main {}", encoding="utf-8")
    assert find_java_file(pc_dir, "PC", "1A") == historical

    np_dir = tmp_path / "1600" / "1A" / "NP"
    np_dir.mkdir()
    (np_dir / "NP-response.txt").write_text("```java\npublic class Main {}\n```", encoding="utf-8")
    issues = find_unparseable_java(tmp_path)
    assert [(issue["problem_id"], issue["prompt_name"]) for issue in issues] == [("1A", "NP")]
