import pytest

from cf_prompting.configuration import (
    ConfigurationError,
    load_azure_credentials,
    load_prompt_configuration,
    validate_prompt_configuration,
)


def test_loads_repository_prompt_configuration():
    configuration = load_prompt_configuration("prompts.json")

    assert configuration["prompts"] == ["CoT-ADV", "NP", "CoT", "PC"]
    assert configuration["text"][-1][-1] == 5


@pytest.mark.parametrize(
    "configuration",
    [
        {},
        {"prompts": [], "text": []},
        {"prompts": ["NP"], "text": []},
        {"prompts": ["NP", "NP"], "text": ["one", "two"]},
        {"prompts": ["../escape"], "text": ["unsafe"]},
        {"prompts": ["PC"], "text": [["primary", "critic", 0]]},
    ],
)
def test_rejects_invalid_prompt_configurations(configuration):
    with pytest.raises(ConfigurationError):
        validate_prompt_configuration(configuration)


def test_credentials_require_https_and_a_key(tmp_path):
    valid = tmp_path / "valid.txt"
    valid.write_text("https://example.openai.azure.com/\nsecret\n", encoding="utf-8")
    credentials = load_azure_credentials(valid)
    assert credentials.endpoint == "https://example.openai.azure.com"
    assert credentials.api_key == "secret"

    invalid = tmp_path / "invalid.txt"
    invalid.write_text("http://example.test\nsecret\n", encoding="utf-8")
    with pytest.raises(ConfigurationError, match="HTTPS"):
        load_azure_credentials(invalid)
