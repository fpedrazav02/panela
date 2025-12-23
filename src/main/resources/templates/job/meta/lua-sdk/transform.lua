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

return transform