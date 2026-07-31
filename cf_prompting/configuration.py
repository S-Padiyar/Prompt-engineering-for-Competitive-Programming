"""Validation and loading for local credentials and prompt definitions."""

from __future__ import annotations

import json
from collections.abc import Mapping, Sequence
from dataclasses import dataclass
from pathlib import Path
from typing import Any, List, TypedDict, Union
from urllib.parse import urlparse

PromptDefinition = Union[str, List[Union[str, int]]]


class PromptConfiguration(TypedDict):
    prompts: List[str]
    text: List[PromptDefinition]


class ConfigurationError(ValueError):
    """Raised when a user-supplied configuration file is malformed."""


@dataclass(frozen=True)
class AzureCredentials:
    endpoint: str
    api_key: str


def load_azure_credentials(path: Union[str, Path]) -> AzureCredentials:
    """Load and validate an Azure endpoint and key from a two-line text file."""
    config_path = Path(path)
    try:
        lines = config_path.read_text(encoding="utf-8").splitlines()
    except OSError as exc:
        raise ConfigurationError(f"Could not read credential file '{config_path}': {exc}") from exc

    if len(lines) < 2:
        raise ConfigurationError("Credential file must contain an endpoint and API key.")

    endpoint, api_key = lines[0].strip(), lines[1].strip()
    parsed_endpoint = urlparse(endpoint)
    if parsed_endpoint.scheme != "https" or not parsed_endpoint.netloc:
        raise ConfigurationError("Azure endpoint must be a valid HTTPS URL.")
    if not api_key:
        raise ConfigurationError("Azure API key cannot be empty.")
    return AzureCredentials(endpoint=endpoint.rstrip("/"), api_key=api_key)


def _validate_chained_prompt(name: str, value: Sequence[Any]) -> List[Union[str, int]]:
    if len(value) not in (2, 3):
        raise ConfigurationError(
            f"Chained prompt '{name}' must have primary text, critic text, and optionally rounds."
        )
    if not all(isinstance(item, str) and item.strip() for item in value[:2]):
        raise ConfigurationError(
            f"Chained prompt '{name}' contains empty or non-text instructions."
        )
    if len(value) == 3 and (
        isinstance(value[2], bool) or not isinstance(value[2], int) or value[2] < 1
    ):
        raise ConfigurationError(f"Chained prompt '{name}' rounds must be a positive integer.")
    return list(value)


def validate_prompt_configuration(data: Any) -> PromptConfiguration:
    """Validate the parallel ``prompts`` and ``text`` arrays used by the experiment."""
    if not isinstance(data, Mapping):
        raise ConfigurationError("Prompt configuration must be a JSON object.")

    names = data.get("prompts")
    definitions = data.get("text")
    if not isinstance(names, list) or not isinstance(definitions, list):
        raise ConfigurationError("Prompt configuration needs 'prompts' and 'text' arrays.")
    if len(names) != len(definitions):
        raise ConfigurationError("The 'prompts' and 'text' arrays must have equal lengths.")
    if not names:
        raise ConfigurationError("At least one prompt must be configured.")

    normalized_names: List[str] = []
    normalized_definitions: List[PromptDefinition] = []
    for index, (name, definition) in enumerate(zip(names, definitions)):
        if not isinstance(name, str) or not name.strip():
            raise ConfigurationError(f"Prompt name at index {index} must be non-empty text.")
        normalized_name = name.strip()
        if normalized_name in {".", ".."} or "/" in normalized_name or "\\" in normalized_name:
            raise ConfigurationError(
                f"Prompt name '{normalized_name}' cannot contain path separators."
            )
        if normalized_name in normalized_names:
            raise ConfigurationError(f"Duplicate prompt name: '{normalized_name}'.")

        if isinstance(definition, str):
            if not definition.strip():
                raise ConfigurationError(f"Prompt '{normalized_name}' cannot be empty.")
            normalized_definition: PromptDefinition = definition
        elif isinstance(definition, list):
            normalized_definition = _validate_chained_prompt(normalized_name, definition)
        else:
            raise ConfigurationError(
                f"Prompt '{normalized_name}' must be text or a chained-prompt array."
            )

        normalized_names.append(normalized_name)
        normalized_definitions.append(normalized_definition)

    return {"prompts": normalized_names, "text": normalized_definitions}


def load_prompt_configuration(path: Union[str, Path]) -> PromptConfiguration:
    """Read a UTF-8 JSON prompt file and validate its public schema."""
    config_path = Path(path)
    try:
        raw_data = json.loads(config_path.read_text(encoding="utf-8"))
    except OSError as exc:
        raise ConfigurationError(f"Could not read prompt file '{config_path}': {exc}") from exc
    except json.JSONDecodeError as exc:
        raise ConfigurationError(f"Prompt file '{config_path}' is not valid JSON: {exc}") from exc
    return validate_prompt_configuration(raw_data)
