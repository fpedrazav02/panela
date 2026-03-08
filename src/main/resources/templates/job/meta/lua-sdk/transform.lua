---@meta
---@module "transform"

---------------------------------------------------------
-- Transform SDK module
---------------------------------------------------------

local transform = {}

---Built-in: Echo transformation
---@param config { from: string, params: table }
---@return table
function transform.echo(config) end

---Custom Lua: Load transformation from a Lua script
---@param config { script: string, config: table, from: string }
---@return table
function transform.lua(config) end

---Custom Java: Load transformation from a Java class
---@param config { class: string, config: table, from: string }
---@return table
function transform.java(config) end

---------------------------------------------------------
-- TABLES
---------------------------------------------------------
---@class TransformNode
---@field type string
---@field op string|nil
---@field from string
---@field config table|nil
---@field script string|nil
---@field class string|nil

---Trim whitespace of all fields in all rows
---@param cfg { from: string }
---@return TransformNode
function transform.trim(cfg)
  return {
    type = "table",
    op = "trim_fields",
    from = cfg.from,
    config = {},
  }
end

---Drop one or more columns
---@param cfg { from: string, columns: string[], failIfMissing?: boolean }
---@return TransformNode
function transform.drop_columns(cfg)
  return {
    type = "table",
    op = "drop_columns",
    from = cfg.from,
    config = {
      columns = cfg.columns,
      failIfMissing = cfg.failIfMissing,
    },
  }
end

---Filter rows by comparing a column value.
---@param cfg { from: string, column: string, value: string, op?: "eq"|"neq"|"contains"|"starts_with"|"ends_with"|"gt"|"lt" }
---@return TransformNode
function transform.filter_rows(cfg)
  return {
    type = "table",
    from = cfg.from,
    config = {
      op     = "filter_rows",
      column = cfg.column,
      value  = cfg.value,
      cmp    = cfg.op,
    },
  }
end

---Keep only the listed columns (also reorders them).
---@param cfg { from: string, columns: string[] }
---@return TransformNode
function transform.select_columns(cfg)
  return {
    type = "table",
    from = cfg.from,
    config = {
      op      = "select_columns",
      columns = cfg.columns,
    },
  }
end

---Rename one or more columns.
---@param cfg { from: string, rename: table<string, string> }
---@return TransformNode
function transform.rename_columns(cfg)
  return {
    type = "table",
    from = cfg.from,
    config = {
      op     = "rename_columns",
      rename = cfg.rename,
    },
  }
end

---Add a new column with a constant value or derived from an existing column.
---@param cfg { from: string, name: string, value?: string, from_column?: string, expression?: "copy"|"upper"|"lower"|"length" }
---@return TransformNode
function transform.add_column(cfg)
  return {
    type = "table",
    from = cfg.from,
    config = {
      op          = "add_column",
      name        = cfg.name,
      value       = cfg.value,
      from_column = cfg.from_column,
      expression  = cfg.expression,
    },
  }
end

---Sort all rows by a column.
---@param cfg { from: string, column: string, order?: "asc"|"desc", numeric?: boolean }
---@return TransformNode
function transform.sort_rows(cfg)
  return {
    type = "table",
    from = cfg.from,
    config = {
      op      = "sort_rows",
      column  = cfg.column,
      order   = cfg.order,
      numeric = cfg.numeric,
    },
  }
end

---Remove duplicate rows. First occurrence is kept.
---@param cfg { from: string, columns?: string[] }
---@return TransformNode
function transform.deduplicate(cfg)
  return {
    type = "table",
    from = cfg.from,
    config = {
      op      = "deduplicate",
      columns = cfg.columns,
    },
  }
end

---Replace null / empty string values with a default.
---@param cfg { from: string, value: string, columns?: string[] }
---@return TransformNode
function transform.fill_nulls(cfg)
  return {
    type = "table",
    from = cfg.from,
    config = {
      op      = "fill_nulls",
      value   = cfg.value,
      columns = cfg.columns,
    },
  }
end

---Reformat the values of a single column.
---@param cfg { from: string, column: string, type?: "string"|"integer"|"float"|"boolean"|"uppercase"|"lowercase" }
---@return TransformNode
function transform.cast_column(cfg)
  return {
    type = "table",
    from = cfg.from,
    config = {
      op     = "cast_column",
      column = cfg.column,
      type   = cfg.type,
    },
  }
end


return transform