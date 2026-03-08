# 🫙 Panela

> An extendable ETL runner powered by Lua job definitions.

Panela lets you define data pipelines as **Lua scripts**. Each job declares its inputs, a chain of transformations, and its outputs. The runtime executes them in dependency order (DAG).

---

## Table of Contents

- [Installation](#installation)
  - [With Nix (recommended)](#with-nix-recommended)
  - [With Maven](#with-maven)
- [Quick Start](#quick-start)
- [CLI Reference](#cli-reference)
- [Job Structure](#job-structure)
- [Inputs](#inputs)
- [Transforms](#transforms)
  - [Built-in: echo](#echo)
  - [Table operations](#table-operations)
    - [trim](#trim)
    - [drop\_columns](#drop_columns)
    - [filter\_rows](#filter_rows)
    - [select\_columns](#select_columns)
    - [rename\_columns](#rename_columns)
    - [add\_column](#add_column)
    - [sort\_rows](#sort_rows)
    - [deduplicate](#deduplicate)
    - [fill\_nulls](#fill_nulls)
    - [cast\_column](#cast_column)
  - [Custom: Lua](#custom-lua-transform)
  - [Custom: Java](#custom-java-transform)
- [Outputs](#outputs)
- [Directory Layout](#directory-layout)
- [Environment Variables](#environment-variables)

---

## Installation

### With Nix (recommended)

Panela ships a `flake.nix`. You need [Nix](https://nixos.org/download) with flakes enabled.

**Run directly without installing:**
```bash
nix run github:fpedrazav02/panela -- <command>
```

**Install into your profile:**
```bash
nix profile install github:fpedrazav02/panela
```

**Build locally from source:**
```bash
git clone https://github.com/fpedrazav02/panela
cd panela
nix build          # produces ./result/bin/panela
./result/bin/panela --help
```

**Enter a dev shell** (JDK 21 + Maven ready):
```bash
nix develop
```

### With Maven

Requires **JDK 21** and **Maven 3.8+**.

```bash
git clone https://github.com/fpedrazav02/panela
cd panela
make build         # mvn package -DskipTests
java -jar target/panela.jar --help
```

---

## Quick Start

```bash
# 1. Create a new job
panela new my-job

# 2. Edit ~/.panela/jobs/my-job/job.lua

# 3. Preview the execution plan
panela show my-job

# 4. Run it
panela run my-job
```

Output files are written to `~/.panela/jobs/<job-name>/build/`.

---

## CLI Reference

| Command | Description |
|---|---|
| `panela new <name>` | Scaffold a new job under `$PANELA_PATH/jobs/<name>/` |
| `panela list` | List all available jobs |
| `panela run <name>` | Parse and execute a job |
| `panela show <name>` | Print the resolved DAG without executing |
| `panela delete <name>` | Delete a job and all its files (prompts for confirmation) |
| `panela delete <name> -f` | Delete without confirmation prompt |

---

## Job Structure

A job is a single `job.lua` file that returns a call to `job.define`:

```lua
local job       = require("job")
local input     = require("input")
local transform = require("transform")
local output    = require("output")

local inputs = {
  raw = input.file { path = "data/sales.csv", type = "csv" }
}

local transforms = {
  cleaned = transform.fill_nulls   { from = "raw",     value = "N/A" },
  trimmed = transform.trim         { from = "cleaned" },
  final   = transform.select_columns { from = "trimmed", columns = { "id", "name", "amount" } },
}

local outputs = {
  result = output.file { from = "final", format = "csv" }
}

return job.define {
  name    = "my-job",
  version = "0.1.0",
  inputs     = inputs,
  transforms = transforms,
  outputs    = outputs,
}
```

---

## Inputs

### `input.file`
Read a file from the job directory.

```lua
input.file { path = "data/file.csv", type = "csv" }
```

| Field | Type | Description |
|---|---|---|
| `path` | `string` | Path relative to the job folder (or absolute) |
| `type` | `"csv"` \| `"json"` \| `"txt"` | File format |

> CSV files are decoded into a **Table** (column-oriented, immutable rows).

### `input.value`
Inline constant value.

```lua
input.value { data = "hello", type = "string" }
```

### `input.lua` / `input.java`
Load data from a custom Lua script or Java class.

```lua
input.lua  { script = "inputs/fetch.lua",  config = { url = "..." } }
input.java { class  = "com.example.MyInput", config = { ... } }
```

---

## Transforms

Every transform takes a `from` field referencing a previously defined input or transform name.

---

### `echo`
Pass the input through unchanged (useful for debugging).

```lua
transform.echo { from = "myInput", params = {} }
```

---

### Table Operations

All operations below work on **Table** data (produced by `input.file` with `type = "csv"`).

---

#### `trim`
Strip leading/trailing whitespace from every cell in every row.

```lua
transform.trim { from = "raw" }
```

---

#### `drop_columns`
Remove one or more columns.

```lua
transform.drop_columns {
  from          = "trimmed",
  columns       = { "internal_id", "debug_flag" },
  failIfMissing = true,   -- default: true
}
```

| Field | Type | Default | Description |
|---|---|---|---|
| `columns` | `string[]` | — | Columns to remove |
| `failIfMissing` | `boolean` | `true` | Throw if a column doesn't exist |

---

#### `filter_rows`
Keep only rows where a column satisfies a condition.

```lua
transform.filter_rows {
  from   = "raw",
  column = "status",
  value  = "active",
  op     = "eq",   -- default: "eq"
}
```

| `op` value | Meaning |
|---|---|
| `eq` | equal (default) |
| `neq` | not equal |
| `contains` | substring match |
| `starts_with` | prefix match |
| `ends_with` | suffix match |
| `gt` | numerically greater than |
| `lt` | numerically less than |

---

#### `select_columns`
Keep only the specified columns and reorder them.

```lua
transform.select_columns {
  from    = "raw",
  columns = { "id", "name", "amount" },
}
```

---

#### `rename_columns`
Rename one or more columns.

```lua
transform.rename_columns {
  from   = "raw",
  rename = { user_id = "id", full_name = "name" },
}
```

| Field | Type | Description |
|---|---|---|
| `rename` | `table<string,string>` | Map of `old_name = "new_name"` pairs |

---

#### `add_column`
Add a new column using a constant value or an expression over an existing column.

```lua
-- Constant value
transform.add_column { from = "raw", name = "source", value = "internal" }

-- Derived from another column
transform.add_column {
  from        = "raw",
  name        = "name_upper",
  from_column = "name",
  expression  = "upper",
}
```

| `expression` | Result |
|---|---|
| `copy` (default) | same value as source column |
| `upper` | uppercase |
| `lower` | lowercase |
| `length` | string length as integer |

---

#### `sort_rows`
Sort all rows by a column value.

```lua
transform.sort_rows {
  from    = "raw",
  column  = "amount",
  order   = "desc",    -- "asc" (default) | "desc"
  numeric = true,      -- default: false
}
```

---

#### `deduplicate`
Remove duplicate rows. The **first** occurrence is kept.

```lua
-- Deduplicate on all columns
transform.deduplicate { from = "raw" }

-- Deduplicate on specific key columns
transform.deduplicate { from = "raw", columns = { "id", "date" } }
```

---

#### `fill_nulls`
Replace `null` or empty string values with a default.

```lua
-- Fill all columns
transform.fill_nulls { from = "raw", value = "N/A" }

-- Fill specific columns only
transform.fill_nulls { from = "raw", value = "0", columns = { "amount", "qty" } }
```

---

#### `cast_column`
Reformat the string values of a single column.

```lua
transform.cast_column { from = "raw", column = "amount", type = "float" }
```

| `type` | Result |
|---|---|
| `string` (default) | no change |
| `integer` | parse as number, drop decimals |
| `float` | parse as floating-point number |
| `boolean` | `"true"` / `"false"` |
| `uppercase` | uppercase string |
| `lowercase` | lowercase string |

---

### Custom Lua Transform

```lua
transform.lua {
  from   = "raw",
  script = "transforms/normalize.lua",
  config = { locale = "es" },
}
```

The script receives the input data and must return the transformed result.

---

### Custom Java Transform

```lua
transform.java {
  from   = "raw",
  class  = "com.example.MyTransform",
  config = { threshold = 100 },
}
```

---

## Outputs

### `output.file`
Write the result to a file inside the job's `build/` directory.

```lua
output.file {
  from   = "final",
  path   = "results/out.csv",  -- optional, defaults to <output-name>.<format>
  format = "csv",
}
```

| Field | Type | Description |
|---|---|---|
| `from` | `string` | Name of the transform/input to consume |
| `format` | `"csv"` \| `"txt"` | Output format |
| `path` | `string` | Optional relative path inside `build/` |

> Currently **Table** data can only be written as `csv`.

### `output.lua` / `output.java`
Write using a custom Lua script or Java class.

```lua
output.lua  { from = "final", script = "outputs/push.lua", config = { ... } }
output.java { from = "final", class  = "com.example.MyOutput", config = { ... } }
```

---

## Directory Layout

```
~/.panela/                  ← PANELA_PATH (default)
└── jobs/
    └── <job-name>/
        ├── job.lua         ← job definition
        ├── data/           ← input files
        └── build/          ← output files (auto-created)
```

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `PANELA_PATH` | `~/.panela` | Root directory for all Panela data |
