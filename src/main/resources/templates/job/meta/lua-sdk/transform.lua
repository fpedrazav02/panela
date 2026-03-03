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
---@param cfg { from: string, columns: string[] , failIfMissing?: boolean }
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


return transform